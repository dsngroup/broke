# Broke [![Build Status](https://travis-ci.org/dsngroup/broke.svg?branch=master)](https://travis-ci.org/dsngroup/broke)
A Latency-Aware Broker

## Build
```bash
mvn package
```
## Configure the Broke server
The configuration file for the broker ```ServerContext.properties``` is located in ```/broke-broker/src/main/resources```
Example of the ```ServerContext.properties```:
```properties
# Configurable properties of server related context

# Port bindings
BOUND_PORT = 8181

# Number of boss (a.k.a. reactor) threads
NUM_OF_BOSS = 1

# Number of worker threads
NUM_OF_WORKER = 4
```

## Run Broke server
The jar for the broker is located in ```/broke-broker/target/```

```bash
cd /broke-broker/target/
java -jar Server-jar-with-dependencies.jar
```

## Client API for Broke Server
### Connect
### Publish
### Subscribe

## License
Apache License 2.0
