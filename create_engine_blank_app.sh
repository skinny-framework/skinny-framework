#!/bin/bash
rm -rf ./skinny-engine-blank-app/target
rm -rf ./skinny-engine-blank-app/project/project
rm -rf ./skinny-engine-blank-app/project/target
rm -rf ./skinny-engine-blank-app/standalone-app/target
rm -rf ./skinny-engine-blank-app/standalone-app/project/project
rm -rf ./skinny-engine-blank-app/standalone-app/project/target

zip -r skinny-engine-blank-app.zip ./skinny-engine-blank-app/
