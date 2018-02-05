# Broke [![Build Status](https://travis-ci.org/dsngroup/broke.svg?branch=master)](https://travis-ci.org/dsngroup/broke)
A Latency-Aware Broker

## Online Resources
* [Website](https://dsngroup.github.io/broke/docusaurus/docs/introduction.html)
* [Javadoc](https://dsngroup.github.io/broke/javadoc/index.html)

## Prerequisites
* [apache-maven-3.5.2](https://maven.apache.org/) or above
* [Java 1.8.0_151](https://www.java.com) or above

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

## Client APIs for Broke Server
[Introduction to client APIs](https://dsngroup.github.io/broke/docusaurus/docs/connect.html)

## License
Apache License 2.0
