---
id: introduction
title: Introduction
sidebar_label: Introduction
---

# Broke 
Broke is a message broker designed for IoT message brokering. It is based on MQTT, a machine-to-machine (M2M)/"Internet of Things" connectivity protocol, which was designed as an extremely lightweight publish/subscribe messaging transport. Broke not only provides a solution for message brokering, but is dedicated to solving the case of latency-sensitive applications. 

## Latency-Sensitive IoT Application
Latency-sensitive IoT application is one of important type of IoT applications. latency-sensitive IoT application needs to respond fast to specific events in order to avoid system failure or to prevent disasters. For example, a smart surveillance system detects intruders entering a bank’s building. The requirement of such system is latency-sensitive because the response to an intrusion must be in time in order to take pictures successfully. The system involves several drones patrolling the area, cameras moving along the floors and IR sensors deployed at critical spaces to detect suspicious behaviors. If an intruder is detected, the cameras and drones take a picture and sends alarm message to the guard of the building immediately. In the above case, the analysis of video from drones and cameras needs to be performed on powerful machines such as cloud data centers. The transmission of video takes network latency and the analysis tasks take computation latency. Therefore, building a latency-sensitive application is a challenge work because many factors cause latency in IoT operational environment.

## Background: From edge to geo-distributed processing units.
IoT broker is one way to transfer the data streams from data sources to processing units. However, the distance and the network transmission latency between data sources and processing units are different because of the complex network topology (e.g. WAN) and the diversity of communication technologies (e.g. WiFi, 4G/LTE, and Ethernet).
On the other hand, the computation capability on processing units are also different because of the diversity of edge devices. Therefore, dispatching data streams to different processing units causes different degree of latency. In latency-sensitive IoT applications, if the IoT broker dispatches workload to the processing unit with time-critical requirement meets long network distance or computation latency, the constraint of the event handling may violate and cause the disaster.
A traditional broker in IoT can do simple data routing mechanism only, such as data broadcasting. There is no broker implementation in IoT that takes latency into consideration currently so that a critical event can be routed to a processing unit with high network and computation latency, which may violate the real-time constraint for event handling.
To respond fast toward critical events for latency-sensitive IoT applications, how to dispatch (i.e. load balancing) workload to feasible processing units is the critical problem that needs to solve.

## Broke: The message brokering solution to latency-sensitive IoT applications
Broke is a latency-sensitive broker that optimizes processing time on workload handling. Broke dispatches the workload to a set of processing units that can balance the load between each other called consumer group. 
We designed Latency-Sensitive Message Dispatching (LSMD) algorithm for selecting processing units from each consumer group based on the latency-related factors in order to reduce the dispatching latency. 

## Latency Sensitive Message Dispatching (LSMD) Algorithm
LSMD runs under a popular message brokering protocol called MQTT. We contribute three functionalities on MQTT[] to support the functionality of LSMD. 
1. To support the load-balancing communication paradigm, we implemented the consumer group mechanism over MQTT. 
2. To make the broker aware of the network latency of each processing unit, we extend the MQTT protocol for the measurement of Round-Trip Time (RTT) between the broker and the processing unit. 
3. To make the broker aware of the computational latency of processing units, we implemented the watermark-based back-pressure[], a mechanism which indicates whether a processing unit is overloaded. With these extensions of MQTT, the latency-sensitive broker can detect the latency conditions of the processing units. The LSMD is able to dispatch workload to each consumer group based on the latency-related metrics.

### Consumer Group
<img src="https://dsngroup.github.io/broke/docs/site/build/broke/img/consumergroup.png" alt="Drawing" style="width: 350px;"/>
Consumers can be grouped into a consumer group for load balancing.

The concept of the consumer group is motivated by [Kafka’s consumer group](https://kafka.apache.org/documentation/#intro_consumers) mechanism. The subscribe clients in the consumer group are processes that run the same task which can offload tasks from each other

In the publish/subscribe system, the consumers are subscribe clients and the workload are messages published from publish clients. The consumers in a consumer group subscribe to a same topic and shares a group ID. Once a publish message matches a subscription of a consumer group, the message is dispatched to one of the consumers in the consumer group. For example, when the publish client publishes a message of topic 1, the message will be forwarded to one of the consumers in consumer group 1 and 2, which subscribe topic 1.

### Overview of LSMD Algorithm
<img src="https://dsngroup.github.io/broke/docs/site/build/broke/img/LSMD.png" alt="Drawing" style="width: 350px;"/>

LSMD selects the consumer in a consumer group based on the latency-related metrics:
* Round-Trip Time (RTT) between the broker and the client.
* Back-Pressure status: The client runs watermark-based back-pressure (Adopted from [Apache Storm Back Pressure](http://jobs.one2team.com/apache-storms/)) so the broker can try to lower the throughput to the client if the client is "back-pressured".
