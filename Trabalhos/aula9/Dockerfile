# base image - an image with openjdk 17
FROM openjdk:17

# working directory inside docker image
WORKDIR /home/sd

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd2122.jar

COPY tls/*.jks /home/sd/

# 
CMD ["java", "-Djavax.net.ssl.keyStore=users.jks", "-Djavax.net.ssl.keyStorePassword=123users", "-cp", "/home/sd/sd2122.jar", "sd2122.aula9.servers.rest.RestUsersServer", "token"]
#CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-Djavax.net.ssl.keyStore=users.jks", "-Djavax.net.ssl.keyStorePassword=123users", "-cp", "/home/sd/sd2122.jar", "sd2122.aula9.servers.soap.SoapUsersServer", "token"]

