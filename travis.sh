#!/bin/bash
if [[ "$TEST_TYPE" == "framework" ]]; then
  export SKINNY_ENV=test
  export APP_ENV=test
  if [[ "$TRAVIS_SCALA_VERSION" == 2.11* ]]; then
    gem install sass &&
    sbt clean ++$TRAVIS_SCALA_VERSION "example/run db:migrate test" coverage test
  else
    # skip scoverage for Scala 2.10
    gem install sass &&
    sbt clean ++$TRAVIS_SCALA_VERSION "example/run db:migrate test" test
  fi
elif [[ "$TEST_TYPE" == "blank-app" && "$TRAVIS_SCALA_VERSION" == 2.11* ]]; then
  export SBT_OPTS="" &&  yes|./run_skinny-blank-app_test.sh
fi
