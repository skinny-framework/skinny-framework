#!/bin/sh

sbt clean \
  ++2.10.4 \
  common/publishSigned \
  assets/publishSigned \
  orm/publishSigned \
  factoryGirl/publishSigned \
  validator/publishSigned \
  framework/publishSigned \
  mailer/publishSigned \
  standalone/publishSigned \
  task/publishSigned \
  test/publishSigned \
  freemarker/publishSigned \
  thymeleaf/publishSigned 

