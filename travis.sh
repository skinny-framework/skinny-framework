#!/bin/bash
gem install sass && 
sbt "example/run db:migrate test" && 
sbt test && 
export SBT_OPTS="" && 
yes|./run_skinny-blank-app_test.sh

