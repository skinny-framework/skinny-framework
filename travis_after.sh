#!/bin/bash
if [[ "$TEST_TYPE" == "framework" ]]; then
  sbt coveralls
fi

