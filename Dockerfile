# ===== 阶段1: 编译前端 =====
FROM node:18-alpine AS frontend
WORKDIR /app/frontend
COPY flower-frontend/ ./
RUN npm install && npm run build

# ===== 阶段2: 编译后端 =====
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml .
COPY flower-common/ flower-common/
COPY flower-security/ flower-security/
COPY flower-service/ flower-service/
COPY flower-web/ flower-web/
COPY --from=frontend /app/frontend/dist flower-web/src/main/resources/static/
RUN mvn package -DskipTests -q

# ===== 阶段3: 运行 =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend /app/flower-web/target/flower-web-*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]