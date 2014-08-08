#!/bin/bash

WORK_DIR=brew_work
mkdir -p ${WORK_DIR}

TEMPLATE_PATH=yeoman-generator-skinny/app/templates
cp -p ${TEMPLATE_PATH}/skinny ${WORK_DIR}/.
cp -p ${TEMPLATE_PATH}/sbt ${WORK_DIR}/.
cp -p ${TEMPLATE_PATH}/sbt-debug ${WORK_DIR}/.
chmod +x ${WORK_DIR}/*
cp -pr ${TEMPLATE_PATH}/bin ${WORK_DIR}/.

VERSION=`grep "val _version =" project/Build.scala | awk -F'"' '{print $2}'`

cd ${WORK_DIR}
tar cvfzp skinny-${VERSION}.tar.gz .
mv skinny-${VERSION}.tar.gz ../.
cd -
rm -rf ${WORK_DIR}
openssl sha1 skinny-${VERSION}.tar.gz

