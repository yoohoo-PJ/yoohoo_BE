package com.example.yoohoo_be.dashboard.config;

import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.dashboard.repository.BookRepository;
import com.example.yoohoo_be.dashboard.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * "data/loan-history/" 폴더에 있는 "경기도교육청중앙도서관 장서 대출목록 (YYYY년 MM월).csv" 원본 파일들을 읽어
 * 도서별 월별 "누적 대출건수" 스냅샷을 book_snapshots 테이블에 적재한다.
 *
 * - 원본 CSV는 매달 시점의 스냅샷이며, "대출건수" 컬럼은 그 시점까지의 누적값이다 (드물게 시스템상 카운터가
 *   리셋되며 줄어드는 경우가 있는데, 이건 조회 시점(BookWearStatusService#getMonthlyLoanTrend)에서
 *   월별 증가분을 0 이상으로 방어 처리한다).
 * - 파일 하나 안에서 같은 ISBN이 여러 행(복본/세트)으로 나오면 대출건수를 합산한다.
 * - books.isbn(unique)을 기준으로, 실제 이 프로젝트 DB에 존재하는 도서만 매칭해서 저장한다.
 * - book_snapshots에 이미 데이터가 있으면(=1회 임포트가 끝난 상태) 다시 실행하지 않는다.
 * - 데이터 파일이 아예 없으면(예: 팀원 로컬 환경) 조용히 건너뛴다.
 */
@Component
@RequiredArgsConstructor
@Order(10)
public class BookSnapshotImportRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;

    @Value("${book-snapshot.import.dir:data/loan-history}")
    private String importDir;

    @Value("${book-snapshot.import.library-name:경기도교육청중앙도서관}")
    private String targetLibraryName;

    // 파일명(NFC 정규화 후)에서 "YYYY년 MM월"을 뽑아내기 위한 패턴
    private static final Pattern FILE_PATTERN = Pattern.compile("(\\d{4})년\\D*?(\\d{1,2})월");

    @Override
    public void run(String... args) {
        try {
            Integer existing = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM book_snapshots", Integer.class);
            if (existing != null && existing > 0) {
                System.out.println("ℹ️ [BookSnapshotImportRunner] book_snapshots에 이미 데이터(" + existing + "건)가 있어 임포트를 건너뜁니다.");
                return;
            }

            Path dir = Path.of(importDir);
            if (!Files.isDirectory(dir)) {
                System.out.println("ℹ️ [BookSnapshotImportRunner] 임포트 폴더가 없어 건너뜁니다: " + dir.toAbsolutePath());
                return;
            }

            Library library = libraryRepository.findByLibraryName(targetLibraryName).orElse(null);
            if (library == null) {
                System.out.println("⚠️ [BookSnapshotImportRunner] 대상 도서관(" + targetLibraryName + ")을 찾을 수 없어 건너뜁니다.");
                return;
            }
            Integer libraryId = library.getLibraryId();

            // ISBN -> bookId 맵을 한 번만 만들어두고 재사용 (파일마다 매번 DB 조회하지 않기 위함)
            Map<String, Integer> isbnToBookId = new HashMap<>();
            bookRepository.findAll().forEach(b -> {
                if (b.getIsbn() != null && !b.getIsbn().isBlank()) {
                    isbnToBookId.put(b.getIsbn().trim(), b.getBookId());
                }
            });
            System.out.println("🚀 [BookSnapshotImportRunner] books 테이블 " + isbnToBookId.size()
                    + "건 로딩 완료. CSV 임포트 시작: " + dir.toAbsolutePath());

            List<Path> files = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.csv")) {
                for (Path p : stream) {
                    files.add(p);
                }
            }
            files.sort(Comparator.comparing(p -> Normalizer.normalize(p.getFileName().toString(), Normalizer.Form.NFC)));

            if (files.isEmpty()) {
                System.out.println("ℹ️ [BookSnapshotImportRunner] " + dir.toAbsolutePath() + " 안에 csv 파일이 없어 건너뜁니다.");
                return;
            }

            int totalInserted = 0;
            List<Object[]> batchArgs = new ArrayList<>();

            for (Path file : files) {
                String name = Normalizer.normalize(file.getFileName().toString(), Normalizer.Form.NFC);
                Matcher m = FILE_PATTERN.matcher(name);
                if (!m.find()) {
                    System.out.println("⚠️ [BookSnapshotImportRunner] 파일명에서 연/월을 파싱할 수 없어 건너뜀: " + name);
                    continue;
                }
                short year = Short.parseShort(m.group(1));
                short month = Short.parseShort(m.group(2));

                Map<String, Long> isbnLoanSum = new HashMap<>();
                int rowCount = 0;

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
                    String header = reader.readLine();
                    if (header == null) {
                        continue;
                    }
                    List<String> headerCols = parseCsvLine(stripBom(header));
                    int isbnIdx = headerCols.indexOf("ISBN");
                    int loanIdx = headerCols.indexOf("대출건수");
                    if (isbnIdx < 0 || loanIdx < 0) {
                        System.out.println("⚠️ [BookSnapshotImportRunner] " + name + " 헤더에서 ISBN/대출건수 컬럼을 찾을 수 없어 건너뜀");
                        continue;
                    }

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isBlank()) {
                            continue;
                        }
                        List<String> cols = parseCsvLine(line);
                        if (cols.size() <= Math.max(isbnIdx, loanIdx)) {
                            continue;
                        }
                        String isbn = cols.get(isbnIdx).trim();
                        if (isbn.isEmpty()) {
                            continue;
                        }
                        long loan;
                        try {
                            loan = Long.parseLong(cols.get(loanIdx).trim());
                        } catch (NumberFormatException e) {
                            loan = 0;
                        }
                        isbnLoanSum.merge(isbn, loan, Long::sum);
                        rowCount++;
                    }
                }

                int matched = 0;
                for (Map.Entry<String, Long> e : isbnLoanSum.entrySet()) {
                    Integer bookId = isbnToBookId.get(e.getKey());
                    if (bookId == null) {
                        continue;
                    }
                    batchArgs.add(new Object[]{libraryId, bookId, year, month, e.getValue().intValue()});
                    matched++;
                    if (batchArgs.size() >= 2000) {
                        totalInserted += flushBatch(batchArgs);
                    }
                }
                System.out.println("✅ [BookSnapshotImportRunner] " + name + " 처리 완료 (원본 " + rowCount
                        + "행, ISBN " + isbnLoanSum.size() + "종, 도서 매칭 " + matched + "건)");
            }

            if (!batchArgs.isEmpty()) {
                totalInserted += flushBatch(batchArgs);
            }

            System.out.println("🎉 [BookSnapshotImportRunner] 임포트 완료: 총 " + totalInserted + "건 저장");
        } catch (IOException e) {
            System.err.println("❌ [BookSnapshotImportRunner] 임포트 실패: " + e.getMessage());
        }
    }

    private int flushBatch(List<Object[]> batchArgs) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO book_snapshots (library_id, book_id, snapshot_year, snapshot_month, cumulative_loan_count) VALUES (?, ?, ?, ?, ?)",
                batchArgs
        );
        int size = batchArgs.size();
        batchArgs.clear();
        return size;
    }

    private String stripBom(String s) {
        if (!s.isEmpty() && s.charAt(0) == '﻿') {
            return s.substring(1);
        }
        return s;
    }

    /**
     * 단순한 CSV 한 줄 파서. 이 데이터셋은 각 필드가 큰따옴표로 감싸져 있고(헤더 행 제외),
     * 필드 내부에 줄바꿈은 없다고 가정한다. 큰따옴표 두 개("")는 이스케이프된 큰따옴표 하나로 처리한다.
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    result.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        result.add(cur.toString());
        return result;
    }
}
