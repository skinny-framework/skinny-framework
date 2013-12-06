#!/bin/bash
SBT_OPTS="-XX:MaxPermSize=256M"
if [ ! -f $HOME/.sbtconfig ]; then
  echo "export SBT_OPTS=\"-XX:MaxPermSize=256M\"" > ${HOME}/.sbtconfig
fi
source $HOME/.sbtconfig
cd `dirname $0`
current_dir=`pwd`
exec java -Xmx1024M ${SBT_OPTS} -jar ${current_dir}/bin/sbt-launch.jar "$@"

