---
id: connect
title: Connect
sidebar_label: Connect
---

## [BlockClient](https://dsngroup.github.io/broke/javadoc/org/dsngroup/broke/client/BlockClient.html)

```BlockClient``` is the representation of a client to the broker. All client APIs are called from ```BlockClient```.
Example: Initialization of a ```BlockClient```.
```java
String address = "localhost"; // Address of the broker.
int port = 8181; // Port of the broker.

BlockClient blockClient = new BlockClient(address, port);
```


## Connect
Befor the client can publish to or subscribe from the broker, the connection should be established first.

The function of ```CriticalOption``` hasn't been implemented yet. Assign any integer number to it currentliy.

```java
// Initialization of the blockClient.

int criticalOption = 0; // Critical option. Useless currently.

blockClient.connect(MqttQoS.AT_LEAST_ONCE, criticalOption);
```
