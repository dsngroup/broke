---
id: publish
title: Publish
sidebar_label: Publish
---

After the client established the connection with the broker, the client can publish messages to the broker. 

## Publish a message to the broker
Every publish request should specify ```topic``` and ```payload```.

Example: publish a message "Bar" with topic "Foo" to the broker.
```java
// Initialization of the blockClient
// Connect to the broker server

String topic = "Foo";
String payload = "Bar";

blockClient.publish(topic, MqttQoS.AT_LEAST_ONCE, criticalOption, payload);  // publish request
```
