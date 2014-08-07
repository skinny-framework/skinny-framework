#!/bin/bash

HOME_DIR=`echo $HOME | sed -e s/\\\\/$//`
ROOT_DIR=${HOME_DIR}/bin/skinny-framework
mkdir -p ${ROOT_DIR}
SKINNY_COMMAND=${ROOT_DIR}/skinny
cd ${ROOT_DIR}

TEMPLATE_PATH=https://raw.githubusercontent.com/skinny-framework/skinny-framework/develop/yeoman-generator-skinny/app/templates
wget ${TEMPLATE_PATH}/skinny
wget ${TEMPLATE_PATH}/sbt
wget ${TEMPLATE_PATH}/sbt-debug
chmod +x *

mkdir -p bin
cd bin
wget ${TEMPLATE_PATH}/bin/sbt-launch.jar

cd -

SHELL_PROFILE=${HOME_DIR}/.bash_profile
if [[ "$SHELL" == *zsh* ]]; then
  SHELL_PROFILE=${HOME_DIR}/.zprofile
fi

if [ ! `grep 'PATH=${PATH}:${HOME}/bin/skinny-framework' ${SHELL_PROFILE}` ]; then
  echo "PATH=\${PATH}:\${HOME}/bin/skinny-framework" >> ${SHELL_PROFILE}
fi

echo "
command installed to ${SKINNY_COMMAND}

Please execute 'source ${SHELL_PROFILE}'
"

