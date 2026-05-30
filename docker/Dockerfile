# syntax=docker/dockerfile:1
# =============================================================================
# ResumAIner Backend — Production-oriented Multi-stage Docker Build
# =============================================================================
# Stage 1: Build WAR with Maven (using BuildKit cache mount)
# Stage 2: Minimal JRE runtime with Tomcat (no JDK, no build tools)
# =============================================================================

ARG MAVEN_VERSION=3.9.9
ARG JAVA_VERSION=21
ARG TOMCAT_VERSION=10.1.55

# ---------- Stage 1: Build ----------
FROM maven:${MAVEN_VERSION}-eclipse-temurin-${JAVA_VERSION} AS build

WORKDIR /app

# Cache Maven dependencies across builds (BuildKit)
COPY backend/pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -q dependency:go-offline

COPY backend/src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -q package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM tomcat:${TOMCAT_VERSION}-jre21-temurin-noble

# JVM tuning: use 75% of container memory, UTF-8 encoding
ENV CATALINA_OPTS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

# Create non-root user with fixed UID/GID, clean default webapps
RUN set -eux; \
    groupadd --system --gid 10001 app; \
    useradd --system --uid 10001 --gid app \
        --home-dir /usr/local/tomcat --shell /usr/sbin/nologin app; \
    rm -rf /usr/local/tomcat/webapps/*; \
    mkdir -p /usr/local/tomcat/webapps /usr/local/tomcat/logs \
             /usr/local/tomcat/temp /usr/local/tomcat/work; \
    chown -R app:app /usr/local/tomcat/webapps /usr/local/tomcat/logs \
                     /usr/local/tomcat/temp /usr/local/tomcat/work

# Copy WAR from build stage
COPY --from=build --chown=app:app /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Copy wait-for-it.sh (with executable permission — chmod via COPY + RUN as fallback)
COPY --chown=app:app docker/scripts/wait-for-it.sh /usr/local/tomcat/wait-for-it.sh
RUN chmod +x /usr/local/tomcat/wait-for-it.sh

USER 10001:10001

EXPOSE 8080

CMD ["/usr/local/tomcat/wait-for-it.sh", "db:5432", "--", "catalina.sh", "run"]
