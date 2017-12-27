### Start Flink clusters
```shell
cd flink*/
sudo docker-compose up
```

### Deploy Flink benchmarks
```shell
sudo ./run.sh
```

### Get log files from Flink clusters using docker-compose logs
```shell
sudo ./getLogs
```
Or specify the number of lines to get from the docker-compose logs
```shell
sudo ./getLogs [lineOfLogs]
```
