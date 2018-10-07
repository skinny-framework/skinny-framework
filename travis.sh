#!/bin/bash

if [[ "$TRAVIS_SCALA_VERSION" == "" ]]; then
  TRAVIS_SCALA_VERSION=2.12.7
fi
if [[ "$TEST_TYPE" == "" ]]; then
  TEST_TYPE=framework
fi

if [[ "$TEST_TYPE" == "framework" ]]; then
  export SKINNY_ENV=test
  export APP_ENV=test
  # sass 3.5 requires Ruby 2.0+
  gem install sass -v 3.4.25 &&
  sbt "example/run db:migrate test" &&
  sbt ++$TRAVIS_SCALA_VERSION scalafmtSbtCheck scalafmtCheck test
elif [[ "$TEST_TYPE" == "blank-app" && "$TRAVIS_SCALA_VERSION" == 2.12* ]]; then
  export SBT_OPTS="" &&  yes|./run_skinny-blank-app_test.sh
fi
