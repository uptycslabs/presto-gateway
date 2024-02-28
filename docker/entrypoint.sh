#!/bin/bash -e

if [[ -z $gid || -z $uid   ]]; then
    echo "ENV VARIABLE uid, gid or appuser is not set"
    exit 1
fi

echo " JAVA OPTS ${JAVA_OPTS}"

java  ${JAVA_OPTS} --add-opens java.base/java.lang=ALL-UNNAMED  --add-opens java.base/java.net=ALL-UNNAMED -jar /usr/app/gateway-ha-1.9.5-jar-with-dependencies.jar server /opt/uptycs/prestogateway/config/gateway-ha-config.yml