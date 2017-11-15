/*
 * Copyright (c) 2017 original authors and authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dsngroup.broke.broker.storage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.protocol.*;

import java.util.*;

/**
 * Subscription pool that stores all subscriptions of a server session.
 * */
public class SubscriptionPool {

    /**
     * The subscriber pool
     * The List of subscribers
     * */
    private List<Subscription> subscriptionList;

    /**
     *
     * */
    private Random packetIdRandomGenerator = new Random();

    /**
     * Register the subscriber to an interested topic
     * @param topic Interested topic
     * @param groupId consumer group id
     * @param channel subscriber's Channel {@see Channel}
     * @return the subscriber's instance
     * */
    public Subscription register(String topic, MqttQoS qos, int groupId, Channel channel) {

        Subscription subscription = new Subscription(topic, qos, groupId, channel);

        subscriptionList.add(subscription);

        return subscription;
    }

    /**
     * Unregister the subscriber from the interested topic keyed by subscriber's ChannelHandlerContext
     * @param topic topic to unsubscribe
     * @param channel the subscriber's ChannelHandlerContext
     * @Exception no such topic or subscriber
     * */
    public void unRegister(String topic, Channel channel) throws Exception{
        // TODO: How to remove a closed subscriber channel context?
        // TODO: e.g. on channel close, call unRegister(channelHandlerContext);
        // TODO: Currently, message sending to inactive subscribers is
        // TODO: avoided by explicitly checking by channel().isActive() method
        synchronized (subscriptionList) {
            for (int i = 0; i < subscriptionList.size(); i++) {
                Subscription subscription = subscriptionList.get(i);
                if (subscription.getTopic().equals(topic)) {
                    subscriptionList.remove(i);
                }
            }
        }
    }

    public void sendToSubscribers(MqttPublishMessage mqttPublishMessageIn) {

        String topic = mqttPublishMessageIn.variableHeader().topicName();

        for(Subscription subscription: subscriptionList) {
            // TODO: implement a match method that performs pattern matching between publish topic and subscribe topic filter
            if (subscription.getTopic().equals(topic)) {
                int packetId = packetIdRandomGenerator.nextInt(10000);
                // TODO: perform QoS selection between publish QoS and subscription QoS
                MqttFixedHeader mqttFixedHeader =
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, subscription.getQos(), false, 0);
                MqttPublishVariableHeader mqttPublishVariableHeader
                        = new MqttPublishVariableHeader(subscription.getTopic(), packetId);
                MqttPublishMessage mqttPublishMessageOut
                        = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, mqttPublishMessageIn.payload());
                subscription.getSubscriberChannel().writeAndFlush(mqttPublishMessageOut);
            }
        }

    }

    /**
     * Not allowed to construct
     * */
    public SubscriptionPool() {
        subscriptionList = new LinkedList<>();
    }

}

/**
 * The comparator for comparing ChannelHandlerContext in HashMap
 * TODO: remove this comparator because hashmap doesn't need this.
 * TODO: but observer how does the hashmap deals channel handler context as the key.
 * */
class ChannelHandlerComparator implements Comparator<ChannelHandlerContext> {
    @Override
    public int compare(ChannelHandlerContext o1, ChannelHandlerContext o2) {
        if (o1 == o2)
            return 0;
        else
            return o1.hashCode()-o2.hashCode();
    }
}
