#!/bin/bash

NUM_LINES=1000
if [ ! -z "$1" ]; then
    NUM_LINES=$1
    echo "Number of lines of logs are set to $1"
fi
DATE=$(date +%Y%m%d-%T)
for dir in ./*/; do
    if [[ "$dir" =~ ^./flink ]]; then 
      if [ ! -d ./../logs/$dir ]; then
          mkdir ./../logs/$dir
      fi
      cd $dir
      sudo docker-compose logs --tail=$NUM_LINES >> ../../logs/$dir/log-$DATE.txt
      cd ..
    fi
done
