#!/bin/sh

# publish-signed in sbt 0.13

sbt ++2.10.0 \
  orm/publish \
  validator/publish \
  framework/publish \
  freemarker/publish 

