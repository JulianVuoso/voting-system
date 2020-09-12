#!/bin/bash
mvn clean install || { echo 'mvn clean install failed' ; exit 1 ; }
cd server/target
tar -xzf tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz
chmod u+x tpe1-g6-server-1.0-SNAPSHOT/run-*.sh
cd ../..
cd client/target
tar -xzf tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz
chmod u+x tpe1-g6-client-1.0-SNAPSHOT/run-*.sh
cd ../..

gnome-terminal -e "bash -c 'cd server/target/tpe1-g6-server-1.0-SNAPSHOT; ./run-registry.sh'"
sleep 1
gnome-terminal -e "bash -c 'cd server/target/tpe1-g6-server-1.0-SNAPSHOT; ./run-server.sh'"
sleep 1
# gnome-terminal -e "bash -c 'cd client/target/tpe1-g6-client-1.0-SNAPSHOT; ./run-fiscal-test.sh'"
# gnome-terminal -e "bash -c 'cd client/target/tpe1-g6-client-1.0-SNAPSHOT; ./run-fiscal.sh -DserverAddress=127.0.0.1:1099 -Did=123 -Dparty=TIGER'"
