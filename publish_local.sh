#!/bin/sh

sbt clean \
  ++2.11.1 \
  common/publishLocal \
  assets/publishLocal \
  httpClient/publishLocal \
  orm/publishLocal \
  factoryGirl/publishLocal \
  validator/publishLocal \
  framework/publishLocal \
  mailer/publishLocal \
  standalone/publishLocal \
  task/publishLocal \
  scaldi/publishLocal \
  test/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal \
  ++2.10.4 \
  common/publishLocal \
  assets/publishLocal \
  httpClient/publishLocal \
  orm/publishLocal \
  factoryGirl/publishLocal \
  validator/publishLocal \
  framework/publishLocal \
  mailer/publishLocal \
  standalone/publishLocal \
  task/publishLocal \
  scaldi/publishLocal \
  test/publishLocal \
  freemarker/publishLocal \
  thymeleaf/publishLocal 


