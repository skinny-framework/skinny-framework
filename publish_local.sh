#!/bin/sh

sbt clean \
  ++2.11.2 \
  common/publishLocal \
  assets/publishLocal \
  httpClient/publishLocal \
  json/publishLocal \
  oauth2/publishLocal \
  oauth2Controller/publishLocal \
  twitterController/publishLocal \
  orm/publishLocal \
  factoryGirl/publishLocal \
  validator/publishLocal \
  framework/publishLocal \
  mailer/publishLocal \
  standalone/publishLocal \
  task/publishLocal \
  scaldi/publishLocal \
  test/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal \
  velocity/publishLocal \
  ++2.10.4 \
  common/publishLocal \
  assets/publishLocal \
  httpClient/publishLocal \
  json/publishLocal \
  oauth2/publishLocal \
  oauth2Controller/publishLocal \
  twitterController/publishLocal \
  orm/publishLocal \
  factoryGirl/publishLocal \
  validator/publishLocal \
  framework/publishLocal \
  mailer/publishLocal \
  standalone/publishLocal \
  task/publishLocal \
  scaldi/publishLocal \
  test/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal \
  velocity/publishLocal 


