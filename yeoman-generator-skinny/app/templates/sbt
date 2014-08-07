#!/bin/bash
current_dir=`pwd`
script_dir=`dirname $0`
sbt_config=${current_dir}/.sbtconfig

if [ ! -f ${sbt_config} ]; then
  echo "#!/bin/bash
java_major_version=\$(java -version 2>&1 | awk -F '\"' '/version/ {print \$2}' | awk -F'.' '{ print \$2 }')
if [ \$java_major_version -ge 8 ]; then
  PERM_OPT=\"-XX:MaxMetaspaceSize=386M\"
else
  PERM_OPT=\"-XX:MaxPermSize=256M\"
fi
export SBT_OPTS=\"-XX:+CMSClassUnloadingEnabled \${PERM_OPT} \${SBT_OPTS}\"
" > ${sbt_config}
fi

source ${sbt_config}
exec java -Xmx1024M ${SBT_OPTS} -jar ${script_dir}/bin/sbt-launch.jar "$@"

