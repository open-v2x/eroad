FROM openjdk:8-jdk-alpine
WORKDIR /home
COPY target/*.jar /home
EXPOSE 53045
ENTRYPOINT java -jar *.jar --spring.config.location=/home/config/application.yml
