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

package org.dsngroup.broke.broker.metadata;

import io.netty.channel.Channel;
import org.dsngroup.broke.protocol.*;

import java.util.*;

/**
 * Subscription pool that stores all subscriptions of a server session.
 */
public class SubscriptionPool {

    /**
     * The subscriber pool.
     * The List of subscribers
     */
    private final List<Subscription> subscriptionList;

    /**
     * Register the subscriber to an interested topic.
     * @param topic Interested topic
     * @param groupId consumer group id
     * @param channel subscriber's Channel {@see Channel}
     */
    public void register(String topic, MqttQoS qos, int groupId, Channel channel) {

        Subscription subscription = new Subscription(topic, qos, groupId, channel);

        subscriptionList.add(subscription);
    }

    /**
     * Return whether the subscription list is null.
     * @return whether the subscription list is null.
     */
    public boolean isEmpty() {
        return subscriptionList.isEmpty();
    }

    /**
     * Unregister the subscriber from the interested topic keyed by subscriber's ChannelHandlerContext.
     * @param topic topic to unsubscribe
     * @param channel the subscriber's ChannelHandlerContext
     * @Exception no such topic or subscriber
     */
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
        for (Subscription subscription: subscriptionList) {
            if (subscription.getTopic().equals(topic)) {
                return subscription;
            }
        }
        return null;
    }

    /**
     * Constructor.
     */
    SubscriptionPool() {
        subscriptionList = new LinkedList<>();
    }

}

