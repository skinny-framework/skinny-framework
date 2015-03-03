#!/bin/sh

sbt ++2.11.6 \
  clean \
  common/publishSigned \
  assets/publishSigned \
  httpClient/publishSigned \
  json/publishSigned \
  oauth2/publishSigned \
  oauth2Controller/publishSigned \
  twitterController/publishSigned \
  orm/publishSigned \
  factoryGirl/publishSigned \
  validator/publishSigned \
  framework/publishSigned \
  mailer/publishSigned \
  standalone/publishSigned \
  task/publishSigned \
  scaldi/publishSigned \
  test/publishSigned \
  freemarker/publishSigned \
  thymeleaf/publishSigned \
  velocity/publishSigned \
  worker/publishSigned \
  ++2.10.4 \
  clean \
  common/publishSigned \
  assets/publishSigned \
  httpClient/publishSigned \
  json/publishSigned \
  oauth2/publishSigned \
  oauth2Controller/publishSigned \
  twitterController/publishSigned \
  orm/publishSigned \
  factoryGirl/publishSigned \
  validator/publishSigned \
  framework/publishSigned \
  mailer/publishSigned \
  standalone/publishSigned \
  task/publishSigned \
  test/publishSigned \
  freemarker/publishSigned \
  thymeleaf/publishSigned \
  velocity/publishSigned  \
  worker/publishSigned 

