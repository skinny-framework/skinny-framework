#!/bin/sh

sbt ++2.10.0 \
  common/publish-signed \
  assets/publish-signed \
  orm/publish-signed \
  validator/publish-signed \
  framework/publish-signed \
  test/publish-signed \
  freemarker/publish-signed \
  thymeleaf/publish-signed 

script_dir=`dirname $0`
${script_dir}/create_blank_app.sh

