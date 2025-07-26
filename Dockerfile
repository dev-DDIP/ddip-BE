FROM eclipse-temurin:21

WORKDIR /app

COPY app.jar .

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080
