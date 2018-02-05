---
id: subscribe
title: Subscribe
sidebar_label: Subscribe
---

After the client established the connection with the broker, the client can subscribe its interested topic.

## [IMessageCallbackHandler](https://dsngroup.github.io/broke/javadoc/org/dsngroup/broke/client/handler/callback/IMessageCallbackHandler.html)
```IMessageCallbackHandler``` is the interface for handling asynchronous events of ```blockClient```.

There are two callback functions the client can override:
* ```messageArrive```: The function for handling arrived messages from the broker. The messages was sent to the client because of the subscription.
* ```connectionLost```: The function for handling connection lost to the broker.

Example implementation of the interface ```IMessageCallbackHandler```:
```java
class MessageCallbackHandler implements IMessageCallbackHandler {

    @Override
    public void messageArrive(ByteBuf payload) {
        logger.info(payload.toString(StandardCharsets.UTF_8));
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("Connection lost: " + cause.getMessage());
        System.exit(1);
    }

}
```

## Set the message callback handler to ```BlockClient```
The message callback handler should be set to the instance of ```BlockClient```.
```java
// Initialization of the blockClient
// Connect to the broker server

// Set the message callback handler to the blockClient
blockClient.setMessageCallbackHandler(new MessageCallbackHandler());
```

## Make a subscription to the broker
Subscrbe to the broker by specifying:
* ```topic```: Interested topic.
* ```consumer group ID```: The consumer group this subscribe client belongs to.

Example: Subscribe to topic "Foo" and specify the consumer group ID to "555".
```java
// Initialization of the blockClient
// Connect to the broker server
// Set the message callback handler.

// Make a subscription.
String topic = "Foo";
int consumerGroup = 555;
blockClient.subscribe(topic, MqttQoS.AT_LEAST_ONCE, 0, consumerGroup);
```
