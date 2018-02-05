---
id: example
title: Consumer Group Example
sidebar_label: Consumer Group
---

## Build a consumer group
Subscribers can be grouped into a consumer group by specifying the same consumer group ID.
These consumers can balance the workload from the broker server.

In this example, we showed how to build a 2-consumer consumer group.

### Start the broker:
```bash
cd /broke-broker/target/
java -jar Server-jar-with-dependencies.jar
```

### Consumer 1:
```java
// Initialization of the blockClient
// Connect to the broker server
// Set the message callback handler.

// Make a subscription.
String topic = "Foo";
int consumerGroup = 555;
blockClient.subscribe(topic, MqttQoS.AT_LEAST_ONCE, 0, consumerGroup);
```

### Consumer 2:
```java
// Initialization of the blockClient
// Connect to the broker server
// Set the message callback handler.

// Make a subscription.
String topic = "Foo";
int consumerGroup = 555;
blockClient.subscribe(topic, MqttQoS.AT_LEAST_ONCE, 0, consumerGroup);
```

### Publisher client:
```java
// Initialization of the blockClient
// Connect to the broker server

String topic = "Foo";
String payload = "Bar";

blockClient.publish(topic, MqttQoS.AT_LEAST_ONCE, criticalOption, payload);  // publish request
```

### Result
The publish message will be forwarded to either consumer 1 or consumer 2.
