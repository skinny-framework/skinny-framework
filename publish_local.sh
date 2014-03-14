#!/bin/sh

sbt ++2.10.0 \
  common/publish-local \
  assets/publish-local \
  orm/publish-local \
  factoryGirl/publish-local \
  validator/publish-local \
  framework/publish-local \
  mailer/publish-local \
  standalone/publish-local \
  task/publish-local \
  test/publish-local \
  freemarker/publish-local \
  thymeleaf/publish-local 

