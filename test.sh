#!/usr/bin/env bash

docker run -it --rm --name distributed-tacing-graph-test \
      -v ./pom.xml:/test/pom.xml \
      -v ./src:/test/src/ \
      -w /test \
      maven:3-eclipse-temurin-17 \
      mvn test

