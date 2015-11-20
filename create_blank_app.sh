#!/bin/sh

script_dir=`dirname $0`
rm -rf release/*
mkdir -p ${script_dir}/release
rm -rf skinny-blank-app/target
rm -rf skinny-blank-app/project/project
cp -pr skinny-blank-app ${script_dir}/release/.
./create_local_ivy2
cd ${script_dir}/release/skinny-blank-app
rm -rf target
rm -rf project/project
rm -rf src/main/webapp/WEB-INF/assets/target
rm -rf */target
cd ..
rm -f ./skinny-blank-app/.*.lock
zip -r skinny-blank-app-with-deps.zip ./skinny-blank-app
rm -rf ivy2
mv skinny-blank-app/ivy2 ivy2
zip -r skinny-blank-app.zip ./skinny-blank-app
