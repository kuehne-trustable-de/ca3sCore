FROM eclipse-temurin:17.0.15_6-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY data/certs/ /trusted

# There are 2 options for test certs usage
# Option 1: Copy test-certs from git-repo
# Option 2: Download the test certs from server
# default is option 1

# Option 1
COPY data/certs.tar.gz /
# Option 2
#RUN wget -q https://wabisch.de/download/certs.tar.gz

RUN mkdir /untrusted
RUN tar xfz certs.tar.gz --directory=/untrusted/

ENTRYPOINT ["java","-jar","/app.jar", "--spring.profiles.active=prod"]
