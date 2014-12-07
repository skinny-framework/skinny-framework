#!/bin/bash
if [[ "$TEST_TYPE" == "framework" ]]; then
  gem install sass && 
  sbt "example/run db:migrate test" && 
  sbt clean coverage test
elif [[ "$TEST_TYPE" == "blank-app" ]]; then
  export SBT_OPTS="" &&  yes|./run_skinny-blank-app_test.sh
fi

