# syntax=docker/dockerfile:1
# =============================================================================
# ResumAIner Backend — Multi-stage Docker Build
# =============================================================================
# Stage 1: Build WAR with Maven
# Stage 2: Deploy WAR to Tomcat 10.1 (non-root user)
# =============================================================================

# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy POM first (layer caching — only re-download deps when pom.xml changes)
COPY backend/pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build WAR
COPY backend/src ./src
RUN mvn clean package -q -DskipTests

# ---------- Stage 2: Runtime ----------
FROM tomcat:10.1.28-jdk21-temurin-jammy

# Create non-root user (security best practice)
RUN groupadd -r app && \
    useradd -r -g app -d /usr/local/tomcat -s /sbin/nologin app

# Copy WAR from build stage as ROOT.war
COPY --from=build --chown=app:app /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Copy wait-for-it.sh for startup ordering
COPY --chown=app:app docker/scripts/wait-for-it.sh /usr/local/tomcat/wait-for-it.sh
RUN chmod +x /usr/local/tomcat/wait-for-it.sh

# Switch to non-root user
USER app

EXPOSE 8080

# Entrypoint: wait for DB, then start Tomcat
CMD ["/usr/local/tomcat/wait-for-it.sh", "db:5432", "--", "catalina.sh", "run"]
