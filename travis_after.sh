#!/bin/bash
if [[ "$TEST_TYPE" == "framework" ]]; then
  if [[ "$TRAVIS_SCALA_VERSION" == 2.13* ]]; then
    sbt ++$TRAVIS_SCALA_VERSION coveralls
  fi
fi

