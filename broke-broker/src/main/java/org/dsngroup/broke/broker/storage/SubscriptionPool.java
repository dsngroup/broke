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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.protocol.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    public Subscription getMatchSubscription(String topic) {
        for(Subscription subscription: subscriptionList) {
            if(subscription.getTopic().equals(topic)) return subscription;
        }
        return null;
    }

    /**
     * Constructor
     * */
    SubscriptionPool() {
        subscriptionList = new LinkedList<>();
    }

}

/**
 * Packet Id should between 1~65535
 * */
class PacketIdGenerator {

    private AtomicInteger packetId;

    int getPacketId() {
        int retVal = packetId.getAndIncrement();
        if(retVal > 65535) {
            synchronized (this) {
                if(packetId.get() > 65535) {
                    packetId.set(1);
                    retVal = packetId.getAndIncrement();
                }
            }
        }
        return retVal;

    }

    PacketIdGenerator() {
        packetId = new AtomicInteger();
        packetId.set(1);
    }

}