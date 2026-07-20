# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

COPY api/gradlew ./gradlew
COPY api/gradle ./gradle
COPY api/settings.gradle.kts .
COPY api/build.gradle.kts .
COPY api/gradle.properties .

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY api/src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar \
      --no-daemon \
      --build-cache \
      -x test

FROM eclipse-temurin:25-jre-noble AS runtime

WORKDIR /app

RUN groupadd --system barkly \
    && useradd \
        --system \
        --gid barkly \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        barkly

COPY --from=builder --chown=barkly:barkly \
    /workspace/build/libs/*.jar \
    /app/app.jar

USER barkly

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]