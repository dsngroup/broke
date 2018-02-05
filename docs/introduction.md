---
id: introduction
title: Introduction
sidebar_label: Introduction
---

### Topic-based publish/subscribe system which tries to load-balancing workloads between multiple consumers. 
* Balance the workload based on the latency-related metrics of the consumers.

### [MQTT](http://mqtt.org/documentation)-based M2M communication protocol:
* Topic-based publish/subscribe protocol.
* Light-weight.
* TCP long connection based protocol.
 
## Consumer Group
![Consumer Group](/broke/docusaurus/img/consumergroup.png)
Consumers can be grouped into a consumer group for load balancing.

The concept of the consumer group is motivated by [Kafka’s consumer group](https://kafka.apache.org/documentation/#intro_consumers) mechanism. The subscribe clients in the consumer group are processes that run the same task which can offload tasks from each other

In the publish/subscribe system, the consumers are subscribe clients and the workload are messages published from publish clients. The consumers in a consumer group subscribe to a same topic and shares a group ID. Once a publish message matches a subscription of a consumer group, the message is dispatched to one of the consumers in the consumer group. For example, when the publish client publishes a message of topic 1, the message will be forwarded to one of the consumers in consumer group 1 and 2, which subscribe topic 1.

## Latency-Sensitive Message Dispatching (LSMD)
![LSMD](/broke/docusaurus/img/LSMD.png)

LSMD selects the consumer in a consumer group based on the latency-related metrics:
* Round-Trip Time (RTT) between the broker and the client.
* Back-Pressure status: The client runs watermark-based back-pressure (Adopted from [Apache Storm Back Pressure](http://jobs.one2team.com/apache-storms/)) so the broker can try to lower the throughput to the client if the client is "back-pressured".
