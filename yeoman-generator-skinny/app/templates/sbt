#!/bin/bash 
cd `dirname $0`
current_dir=`pwd`
sbt_config=${current_dir}/.sbtconfig

if [ ! -f ${sbt_config} ]; then
  echo "#!/bin/bash
java_major_version=\$(java -version 2>&1 | awk -F '\"' '/version/ {print \$2}' | awk -F'.' '{ print \$2 }')
if [ \$java_major_version -ge 8 ]; then
  PERM_OPT=\"-XX:MaxMetaspaceSize=386M\"
else
  PERM_OPT=\"-XX:MaxPermSize=256M\"
fi
export SBT_OPTS=\"-XX:+CMSClassUnloadingEnabled \${PERM_OPT}\"
" > ${sbt_config}
fi

source ${sbt_config}
exec java -Xmx1024M ${SBT_OPTS} -jar ${current_dir}/bin/sbt-launch.jar "$@"

