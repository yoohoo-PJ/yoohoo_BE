FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
# 윈도우에서 작성된 gradlew 파일의 줄바꿈 문자를 리눅스용으로 변경하고 실행 권한 부여
RUN sed -i 's/\r$//' gradlew
RUN chmod +x ./gradlew
# 테스트는 제외하고 빌드 수행
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# 빌드된 jar 파일을 복사 (빌드 결과물 이름 상관없이 하나만 복사)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# 서버 시작 시 사용할 포트 노출 (Render는 기본적으로 내부에서 포트를 자동 감지함)
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx300m", "-XX:MaxMetaspaceSize=128m", "-jar", "app.jar"]
