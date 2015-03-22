#!/bin/bash
if [[ "$TEST_TYPE" == "framework" ]]; then
  if [[ "$TRAVIS_SCALA_VERSION" == 2.11* ]]; then
    gem install sass &&
    sbt "example/run db:migrate test" &&
    sbt clean ++$TRAVIS_SCALA_VERSION coverage test
  else
    # skip converage for Scala 2.10
    # https://github.com/scoverage/sbt-scoverage#highlighting
    gem install sass &&
    sbt "example/run db:migrate test" &&
    sbt clean ++$TRAVIS_SCALA_VERSION test
  fi
elif [[ "$TEST_TYPE" == "blank-app" && "$TRAVIS_SCALA_VERSION" == 2.11* ]]; then
  export SBT_OPTS="" &&  yes|./run_skinny-blank-app_test.sh
fi
