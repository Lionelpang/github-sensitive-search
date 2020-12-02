FROM openjdk:8-slim
VOLUME /tmp
ENV CONFIG_FILE classpath:tasks.yaml

ADD github-sensitive-search-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT eval exec "java -Djava.security.egd=file:/dev/./urandom -Dcom.sun.net.ssl.checkRevocation=false -Duser.timezone=GMT+08 $OPTS -jar /app.jar $CONFIG_FILE"