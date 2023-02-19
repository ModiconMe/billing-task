FROM openjdk:17-jdk-alpine
COPY /build/libs/billing-task.jar .
COPY https-billing.p12 .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "billing-task.jar"]