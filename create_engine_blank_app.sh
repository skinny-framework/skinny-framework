#!/bin/bash
rm -rf ./skinny-engine-blank-app/target
rm -rf ./skinny-engine-blank-app/project/project
rm -rf ./skinny-engine-blank-app/project/target
rm -rf ./skinny-engine-blank-app/standalone-app/sbt-project/target
rm -rf ./skinny-engine-blank-app/standalone-app/sbt-project/project/project
rm -rf ./skinny-engine-blank-app/standalone-app/sbt-project/project/target

zip -r skinny-engine-blank-app.zip ./skinny-engine-blank-app/
