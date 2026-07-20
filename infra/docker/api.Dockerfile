# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

# Najpierw pliki Gradle, które zmieniają się rzadziej.
# Dzięki temu pobrane zależności mogą zostać w cache warstw Dockera.
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle.kts .
COPY api/build.gradle.kts api/build.gradle.kts

RUN chmod +x gradlew

# Pobranie i przygotowanie zależności bez kopiowania kodu źródłowego.
# Ten krok może zostać odtworzony z cache, dopóki konfiguracja Gradle się nie zmieni.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :api:dependencies --no-daemon

# Dopiero teraz kopiujemy kod, który zmienia się najczęściej.
COPY api/src/ api/src/

# Budujemy wykonywalny JAR Spring Boot.
# Testy zakładamy jako wykonane wcześniej w jobie CI.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :api:bootJar \
      --no-daemon \
      --build-cache \
      -x test

FROM eclipse-temurin:25-jre-noble AS runtime

WORKDIR /app

# Osobny użytkownik zamiast uruchamiania aplikacji jako root.
RUN groupadd --system barkly \
    && useradd \
        --system \
        --gid barkly \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        barkly

COPY --from=builder --chown=barkly:barkly \
    /workspace/api/build/libs/*.jar \
    /app/app.jar

USER barkly

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]