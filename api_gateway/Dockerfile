FROM maven:3.6.3-jdk-8

COPY . /app

WORKDIR /app

EXPOSE 8090

CMD ["java","-jar", "-Dspring.profiles.active=prod", "./target/api_gateway-0.0.1-SNAPSHOT.jar"]