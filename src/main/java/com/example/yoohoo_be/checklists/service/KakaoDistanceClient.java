package com.example.yoohoo_be.checklists.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 카카오 모빌리티 API를 사용한 도서관 간 실제 도로 거리 계산 클라이언트.
 * API 호출 실패 시 Haversine 공식으로 자동 폴백(fallback)합니다.
 */
@Slf4j
@Component
public class KakaoDistanceClient {

    private static final String DIRECTIONS_URL =
            "https://apis-navi.kakaomobility.com/v1/directions?origin=%s,%s&destination=%s,%s";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    /**
     * 두 도서관 간 실제 도로 거리(km)를 반환합니다.
     * 카카오 API 호출 실패 시 Haversine 직선 거리 × 1.3 보정을 적용합니다.
     */
    public double getDistanceKm(BigDecimal originLat, BigDecimal originLng,
                                BigDecimal destLat, BigDecimal destLng) {
        try {
            String url = String.format(DIRECTIONS_URL,
                    originLng.toPlainString(), originLat.toPlainString(),
                    destLng.toPlainString(), destLat.toPlainString());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> routes = (List<Map<String, Object>>) response.getBody().get("routes");
                if (routes != null && !routes.isEmpty()) {
                    Map<String, Object> summary = (Map<String, Object>) routes.get(0).get("summary");
                    if (summary != null && summary.get("distance") != null) {
                        int distanceMeters = ((Number) summary.get("distance")).intValue();
                        return distanceMeters / 1000.0;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("카카오 API 호출 실패, Haversine 폴백 적용: {}", e.getMessage());
        }

        // 폴백: Haversine 직선 거리 × 1.3 보정 (직선 → 도로 거리 근사)
        return haversineKm(originLat.doubleValue(), originLng.doubleValue(),
                destLat.doubleValue(), destLng.doubleValue()) * 1.3;
    }

    /**
     * Haversine 공식: 두 위경도 좌표 사이의 직선 거리(km)를 계산합니다.
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
