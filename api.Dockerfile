FROM eclipse-temurin:8-jre
LABEL maintainer="ahmad.zeeshaan@gmail.com"
COPY build/libs/url-shortener-0.0.1-SNAPSHOT.jar url-shortener.jar
EXPOSE 8080
CMD ["java","-jar","url-shortener.jar"]