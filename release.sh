#!/bin/sh

sbt ++2.10.0 \
  orm/publish-signed \
  validator/publish-signed \
  framework/publish-signed \
  freemarker/publish-signed 

