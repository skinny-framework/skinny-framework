#!/bin/sh

sbt clean \
  ++2.10.3 \
  common/publishSigned \
  assets/publishSigned \
  httpClient/publishSigned \
  orm/publishSigned \
  factoryGirl/publishSigned \
  validator/publishSigned \
  framework/publishSigned \
  mailer/publishSigned \
  standalone/publishSigned \
  task/publishSigned \
  test/publishSigned \
  freemarker/publishSigned \
  thymeleaf/publishSigned 

