#!/bin/sh

sbt ++2.10.0 \
  common/publish-local-signed \
  assets/publish-local-signed \
  orm/publish-local-signed \
  validator/publish-local-signed \
  framework/publish-local-signed \
  task/publish-local-signed \
  test/publish-local-signed \
  freemarker/publish-local-signed \
  thymeleaf/publish-local-signed 

