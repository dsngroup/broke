# System Design of Latency-Sensitive Broker

Consider the different latency from consumer to broker, a better consumer group mechanism of data partition is need.

## Connections

A pub/sub system will keep **long-living tcp connections**, between server(broker) and clients. 

## Message Format

The message format will be designed similar to kafka and mqtt.

### Message Type

From client,

* CONNECTION
* PUBLISH
* SUBSCRIBE

From server, 
* CONNACK,
* PUBLISHACK,
* SUBSCRIBEACK

## In-Memory Store Data Structure

The in-memory store data structure is kept as HashMap, LinkedLists.

```java
/** The consumer group subscribe same topic for different offsets */
public class ConsumerGroup {
    // TODO: Concurrent list.
    private volatile List<Consumer> consumerList; 
    // ...
}

public class SubscribeGroup {
    // TODO: Concurrent list.
    private volatile List<ConsumerGroup> subscribeList;
    // ...
}

public class TopicPool {
    private static Map<String, SubscribeGroup> inMemoryTopicPool = new ConcurrentHashMap<>();
    // ...
}
```
