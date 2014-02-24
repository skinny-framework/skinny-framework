#!/bin/sh

script_dir=`dirname $0`
mkdir -p ${script_dir}/release
rm -rf release/*
cd ${script_dir}/yeoman-generator-skinny
rm -rf target
rm -rf */target

npm link
cd -
cd ${script_dir}/release
mkdir skinny-blank-app
cd skinny-blank-app
yo skinny
rm -f package.json
cd ..
zip skinny-blank-app.zip -r ./skinny-blank-app/*


