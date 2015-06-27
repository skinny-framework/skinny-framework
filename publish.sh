#!/bin/sh

sbt ++2.11.7 \
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
  skinnyScalatra/publishSigned \
  skinnyScalatraTest/publishSigned \
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
  ++2.10.5 \
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
  skinnyScalatra/publishSigned \
  skinnyScalatraTest/publishSigned \
  framework/publishSigned \
  mailer/publishSigned \
  standalone/publishSigned \
  task/publishSigned \
  test/publishSigned \
  freemarker/publishSigned \
  thymeleaf/publishSigned \
  velocity/publishSigned  \
  worker/publishSigned 

