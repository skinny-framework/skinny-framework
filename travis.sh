#!/bin/bash

if [[ "$TRAVIS_SCALA_VERSION" == "" ]]; then
  TRAVIS_SCALA_VERSION=2.11.7
fi
if [[ "$TEST_TYPE" == "" ]]; then
  TEST_TYPE=framework
fi

if [[ "$TEST_TYPE" == "framework" ]]; then
  export SKINNY_ENV=test
  export APP_ENV=test
  gem install sass &&
  sbt "example/run db:migrate test" &&
  sbt ++$TRAVIS_SCALA_VERSION test
elif [[ "$TEST_TYPE" == "blank-app" && "$TRAVIS_SCALA_VERSION" == 2.11* ]]; then
  export SBT_OPTS="" &&  yes|./run_skinny-blank-app_test.sh
fi
