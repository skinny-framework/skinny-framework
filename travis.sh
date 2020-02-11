#!/bin/bash

if [[ "$TRAVIS_SCALA_VERSION" == "" ]]; then
  TRAVIS_SCALA_VERSION=2.12.10
fi
if [[ "$TEST_TYPE" == "" ]]; then
  TEST_TYPE=framework
fi

if [[ "$TEST_TYPE" == "framework" ]]; then
  export SKINNY_ENV=test
  export APP_ENV=test
  # sass 3.5 requires Ruby 2.0+
  gem install sass -v 3.4.25 &&
  if [[ "$TRAVIS_SCALA_VERSION" == "2.13.1" ]]; then
    sbt "++ ${TRAVIS_SCALA_VERSION}!" test:compile \
          assets/test \
          common/test \
          factoryGirl/test \
          framework/test \
          freemarker/test \
          httpClient/test \
          json/test \
          mailer/test \
          oauth2/test \
          oauth2Controller/test \
          orm/test \
          standalone/test \
          task/test \
          thymeleaf/test \
          twitterController/test \
          validator/test \
          worker/test
  else
    sbt "example/run db:migrate test" &&
    sbt ++$TRAVIS_SCALA_VERSION scalafmtSbtCheck scalafmtCheck test:scalafmtCheck test
  fi
elif [[ "$TEST_TYPE" == "blank-app" && "$TRAVIS_SCALA_VERSION" == 2.12* ]]; then
  export SBT_OPTS="" &&  yes|./run_skinny-blank-app_test.sh
fi
