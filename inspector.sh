#!/bin/bash
mvn clean install
cd server/target
tar -xzf tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz
chmod u+x tpe1-g6-server-1.0-SNAPSHOT/run-*.sh
cd ../..
cd client/target
tar -xzf tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz
chmod u+x tpe1-g6-client-1.0-SNAPSHOT/run-*.sh
cd ../..

gnome-terminal -e "bash -c 'cd server/target/tpe1-g6-server-1.0-SNAPSHOT; ./run-registry.sh'"
gnome-terminal -e "bash -c 'cd server/target/tpe1-g6-server-1.0-SNAPSHOT; ./run-inspection-server.sh'"
sleep 1
gnome-terminal -e "bash -c 'cd client/target/tpe1-g6-client-1.0-SNAPSHOT; ./run-fiscal-test.sh'"
gnome-terminal -e "bash -c 'cd client/target/tpe1-g6-client-1.0-SNAPSHOT; ./run-fiscal.sh'"
