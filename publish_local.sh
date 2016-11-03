#!/bin/sh

if [[ "$1" == "test" ]]; then

sbt ++2.12.0 \
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

sbt ++2.12.0 \
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
  scaldi/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal \
  velocity/publishLocal \
  ++2.11.8 \
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
  scaldi/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal \
  velocity/publishLocal \
  ++2.10.6 \
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
  scaldi/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal \
  velocity/publishLocal

fi
