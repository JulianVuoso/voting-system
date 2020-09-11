# POD - TPE1 - G6

./run-management -DserverAddress=127.0.0.1:1099 -Daction=open
./run-management -DserverAddress=127.0.0.1:1099 -Daction=state
./run-management -DserverAddress=127.0.0.1:1099 -Daction=close

./run-vote -DserverAddress=127.0.0.1:1099 -DvotesPath=../../../votes.csv
./run-vote -DserverAddress=127.0.0.1:1099 -DvotesPath=../../../votes2.csv

./run-fiscal -DserverAddress=127.0.0.1:1099 -Did=1001 -Dparty=TIGER

./run-query -DserverAddress=127.0.0.1:1099 [ -Dstate=stateName | -Did=pollingPlaceNumber ] -DoutPath=fileName.csv
