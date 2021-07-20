FROM registry.opensource.zalan.do/library/openjdk-11-jdk-slim:latest

MAINTAINER "http://zalando.github.io/"
LABEL MAINTAINER "http://zalando.github.io/"

COPY build/libs/zally-github-integration.jar /
COPY zally-server/src/main/resources/api/zally-api.yaml /zalando-apis/gh-integration-api.yaml

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /zally-github-integration.jar
