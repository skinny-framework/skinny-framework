#!/bin/sh

script_dir=`dirname $0`
mkdir -p ${script_dir}/release
rm -rf release/*
cd ${script_dir}/yeoman-generator-skinny
rm -rf app/templates/project/project
rm -rf app/templates/project/target
rm -rf app/templates/target
rm -rf app/templates/*/target

npm link
cd -
cd ${script_dir}/release
mkdir skinny-blank-app
cd skinny-blank-app
yo skinny
rm -f package.json
cd ..
zip skinny-blank-app.zip -r ./skinny-blank-app/*


