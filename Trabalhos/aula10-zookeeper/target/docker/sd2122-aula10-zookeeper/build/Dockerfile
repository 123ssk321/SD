# base image - an image with openjdk  17
FROM openjdk:17

# working directory inside docker image
WORKDIR /home/sd

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd.jar

# run Discovery when starting the docker image
CMD ["java", "-cp", "/home/sd/sd.jar", "sd2122.aula10.zookeeper.Zookeeper", "zookeeper"]
