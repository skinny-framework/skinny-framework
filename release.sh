#!/bin/sh

sbt ++2.10.0 \
  orm/publish \
  validator/publish \
  framework/publish \
  freemarker/publish \
  thymeleaf/publish 

