#!/bin/sh

sbt \
  ++2.11.6 \
  clean \
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
  worker/publishLocal \
  ++2.10.4 \
  clean \
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
  worker/publishLocal

