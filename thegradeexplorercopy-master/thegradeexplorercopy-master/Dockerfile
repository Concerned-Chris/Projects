# The first layer of our image is debian buster
FROM debian:buster

# Set java version
ENV JAVA_VERSION 11

# update package management utility, then install jdk, nodejs, npm and maven
RUN apt update; \
    apt -y full-upgrade; \
    apt -y install openjdk-$JAVA_VERSION-jdk \
                   npm \
                   maven; \
    node -v; \
    npm -v; \
    mvn -v; \
    apt clean;

# set working directory
WORKDIR /usr/local/src/tge

# Copy files from local to container
COPY . .

# Deploy
CMD ["mvn", "spring-boot:run"]


