FROM openjdk:11-jre-slim

EXPOSE 7000
ENTRYPOINT ["java", "-jar", "/usr/share/javalin/my-javalin.jar"]

ARG JAR_FILE
ADD target/Matchmaking.jar /usr/share/javalin/my-javalin.jar
ADD target/files/EMBEDDINGS/embeddings_2L_NSC_Min2.txt.gz /files/EMBEDDINGS/embeddings_2L_NSC_Min2.txt.gz