FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
# Java must trust public CAs for outbound HTTPS (Unsplash, Picsum, etc.)
RUN apk add --no-cache ca-certificates \
    && update-ca-certificates \
    && if [ -f /etc/ssl/certs/java/cacerts ]; then \
         cp /etc/ssl/certs/java/cacerts "$JAVA_HOME/lib/security/cacerts"; \
       fi
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
