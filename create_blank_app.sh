#!/bin/bash

script_dir=`dirname $0`
rm -rf ${script_dir}/release/*
mkdir -p ${script_dir}/release
rm -rf skinny-blank-app/target
rm -rf skinny-blank-app/project/project
rm -rf skinny-blank-app/src/main/webapp/WEB-INF/assets/target
cp -pr skinny-blank-app ${script_dir}/release/.
if [[ "$1" != "test" ]]; then
  # somehow, sbt ignores absent libs in local ivy home when they're already exists in global ivy home.
  rm -rf $HOME/.ivy2/cache/org.skinny-framework
  ./create_local_ivy2
fi
cd ${script_dir}/release/skinny-blank-app
rm -rf target
rm -rf project/project
rm -rf src/main/webapp/WEB-INF/assets/target
rm -rf */target
cd ..
rm -f ./skinny-blank-app/.*.lock
zip -r skinny-blank-app-with-deps.zip ./skinny-blank-app
rm -rf ivy2
if [[ -d "skinny-blank-app/ivy2" ]]; then
  mv skinny-blank-app/ivy2 ivy2
fi
zip -r skinny-blank-app.zip ./skinny-blank-app
