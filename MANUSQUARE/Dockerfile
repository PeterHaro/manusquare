FROM openjdk:8-jre-alpine

EXPOSE 7000
ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/javalin/my-javalin.jar"]

ARG JAR_FILE
ADD target/Matchmaking.jar /usr/share/javalin/my-javalin.jar