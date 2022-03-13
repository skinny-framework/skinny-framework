#!/bin/bash

if [[ "$1" == "test" ]]; then

sbt ++2.13.8 \
  clean \
  common/publishLocal \
  assets/publishLocal \
  httpClient/publishLocal \
  json/publishLocal \
  orm/publishLocal \
  factoryGirl/publishLocal \
  validator/publishLocal \
  framework/publishLocal \
  mailer/publishLocal \
  task/publishLocal \
  test/publishLocal \
  worker/publishLocal \
  standalone/publishLocal

else

sbt ++2.13.8 \
  clean \
  common/publishLocal \
  assets/publishLocal \
  httpClient/publishLocal \
  json/publishLocal \
  orm/publishLocal \
  factoryGirl/publishLocal \
  validator/publishLocal \
  framework/publishLocal \
  mailer/publishLocal \
  task/publishLocal \
  test/publishLocal \
  worker/publishLocal \
  oauth2/publishLocal \
  oauth2Controller/publishLocal \
  twitterController/publishLocal \
  standalone/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal

fi
