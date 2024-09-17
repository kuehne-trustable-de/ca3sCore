FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache python3 py3-pip

RUN pip3 install badkeys --break-system-packages

RUN badkeys --update-bl
