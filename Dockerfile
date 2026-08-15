FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -B && ls -la target/

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/code_sports_rui_and_fei-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
