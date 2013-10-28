#!/bin/sh

script_dir=`dirname $0`
mkdir -p ${script_dir}/release
cd ${script_dir}/yeoman-generator-skinny
npm link
cd -
cd ${script_dir}/release
mkdir skinny-blank-app
cd skinny-blank-app
yo skinny
rm -f package.json
cd ..
zip skinny-blank-app.zip -r ./skinny-blank-app/*

