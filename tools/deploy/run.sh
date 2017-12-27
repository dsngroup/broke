#!/bin/bash
for dir in ./*/ 
do 
  cd "$dir"
  DIR=${PWD##*/}
  CONTAINER_NAME=$DIR"_jobmanager_1"
  CONTAINER_ID="sudo docker ps --filter name=$CONTAINER_NAME --format={{.ID}}"
  BROKER_ADDR=172.17.0.1:8181
  sudo docker cp ../../../broke-flink-sample/target/MqttWordCount-jar-with-dependencies.jar $($CONTAINER_ID):/opt/flink/.
  sudo docker exec -it $($CONTAINER_ID) flink run -d /opt/flink/MqttWordCount-jar-with-dependencies.jar $BROKER_ADDR
  cd ..
done
