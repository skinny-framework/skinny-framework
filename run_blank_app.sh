#!/bin/sh

script_dir=`dirname $0`
mkdir -p ${script_dir}/release
rm -rf release/*
cd ${script_dir}/yeoman-generator-skinny
npm link
cd -
cd ${script_dir}/release
mkdir skinny-blank-app
cd skinny-blank-app
yo skinny
rm -f package.json
./skinny g scaffold:jade members member name:String birthday:LocalDate
./skinny db:migrate
./skinny db:migrate test
./skinny test

