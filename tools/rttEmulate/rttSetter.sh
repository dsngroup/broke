#!/bin/bash

DELAY=$1
DELAY="${DELAY}ms"
OPTION=$2

if [[ $OPTION == 'all' ]]
then
    INTERFACES=$(sudo sh ./dockerveth.sh | grep -n 'flink[0-9]*_taskmanager_[0-9]*$' | awk '{ print $2 }')
    for INTERFACE in $INTERFACES; do
        $(tc qdisc add dev $INTERFACE root netem delay $DELAY)
        if [[ $? != 0 ]]; then
          $(tc qdisc change dev $INTERFACE root netem delay $DELAY)
          if [[ $? != 0 ]]; then
              echo "Delay setting failed for interface: $INTERFACE"
          else
              echo "Delay of interface $INTERFACE set to $DELAY"
          fi
        fi
    done
else
    INTERFACE=$(sudo sh ./dockerveth.sh | grep $OPTION | awk '{ print $2 }')
    $(tc qdisc add dev $INTERFACE root netem delay $DELAY)
    if [[ $? != 0 ]]; then
      $(tc qdisc change dev $INTERFACE root netem delay $DELAY)
      if [[ $? != 0 ]]; then
          echo "DELAY setting failed for interface: $INTERFACE"
      else
          echo "Delay of interface $INTERFACE set to $DELAY"
      fi
    fi
fi
