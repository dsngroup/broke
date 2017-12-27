# RTT emulation setter for Flink veth interfaces

### Set delay for all veth interfaces of flink taskmanagers
```shell
sudo ./rttSetter [delay in ms] all
// eg. sudo ./rttSetter 100 all
```

### Set delay for a specific veth interface by specifying the container's name.
```shell
sudo ./rttSetter [delay in ms] [container's name]
// eg. sudo ./rttSetter 100 flink0_taskmanager_1
```
