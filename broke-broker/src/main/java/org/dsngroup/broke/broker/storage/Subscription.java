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
import org.dsngroup.broke.protocol.MqttQoS;

import java.util.Objects;

/**
 * The subscriber information class
 * */
public class Subscription {

    private String topic;
    private MqttQoS qos;
    private int groupId;
    private Channel subscriberChannel;

    /**
     * Getter methods
     * */
    public String getTopic() {
        return topic;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public int getGroupId() {
        return groupId;
    }

    public Channel getSubscriberChannel() {
        return subscriberChannel;
    }

    /**
     * The constructor
     * @param subscriberChannel subscriberChannel of this subscriber {@see ChannelHandlerContext}
     * @param groupId the consumer group id
     * */
    public Subscription(String topic, MqttQoS qos, int groupId, Channel subscriberChannel) {
        this.topic = topic;
        this.qos = qos;
        this.groupId = groupId;
        this.subscriberChannel = subscriberChannel;
    }

    /**
     * Override hashCode() for HashSet
     * @return hash code
     * */
    @Override
    public int hashCode() {
        return Objects.hash(groupId, subscriberChannel);
    }

    /**
     * Override equals() for Set comparator
     * Compare using the reference of subscriberChannelHandlerContext
     * @param obj Other object
     * */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Subscription other = (Subscription) obj;
        if(this.getSubscriberChannel() != other.getSubscriberChannel())
            return false;
        if(this.getTopic()!=other.getTopic())
            return false;
        return true;
    }

}
