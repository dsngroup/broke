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
import org.dsngroup.broke.broker.channel.handler.ClientProber;
import org.dsngroup.broke.protocol.*;

import java.util.concurrent.ConcurrentHashMap;

public class ServerSession {

    private boolean isActive;

    final String clientId;

    private SubscriptionPool subscriptionPool;

    /**
     * An incremental packet ID
     * */
    private PacketIdGenerator packetIdGenerator;

    // Unacked messages store: Key: packet idenfier, value: message
    final ConcurrentHashMap<String, MqttMessage> unackedMessages;

    private ClientProber clientProber;


    /**
     * Getter for isActive
     * */
    public boolean getIsActive() {
        return isActive;
    }

    /**
     * Setter for isActive
     * */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Getter for client Id
     * */
    public String getClientId() {
        return clientId;
    }


    /**
     * Getter for subscription pool
     * */
    public SubscriptionPool getSubscriptionPool() {
        return subscriptionPool;
    }

    /**
     * Setter for client prober
     * */
    public void setClientProber(ClientProber clientProber) {
        this.clientProber = clientProber;
    }

    /**
     * For a PUBLISH message, check whether any subscription in the subscription pool matches its topic.
     * If the topic is matched, create a PUBLISH and publish to the corresponding subscriber client.
     * @param mqttPublishMessage The PUBLISH message from the publisher.
     * */
    public void publishToSubscription(MqttPublishMessage mqttPublishMessage) {

        String topic = mqttPublishMessage.variableHeader().topicName();

        Subscription matchSubscription = subscriptionPool.getMatchSubscription(topic);

        if(matchSubscription!=null) {
            int packetId = packetIdGenerator.getPacketId();
            // TODO: perform QoS selection between publish QoS and subscription QoS
            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, matchSubscription.getQos(), false, 0);
            MqttPublishVariableHeader mqttPublishVariableHeader
                    = new MqttPublishVariableHeader(matchSubscription.getTopic(), packetId);
            // TODO: figure out how to avoid being garbage collected.
            ByteBuf payload = Unpooled.copiedBuffer(mqttPublishMessage.payload());
            // TODO: figure out how to avoid being garbage collected.
            payload.retain();
            MqttPublishMessage mqttPublishMessageOut
                    = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, payload);
            if(matchSubscription.getSubscriberChannel().isActive()) {
                try {
                    // TODO: try to remove sync()
                    matchSubscription.getSubscriberChannel().writeAndFlush(mqttPublishMessageOut).sync();
                } catch (Exception e) {
                    // TODO: remove this
                    e.printStackTrace();
                }
            }
        }
    }

    public double getPublishScore() {
        if(clientProber.isBackPressured())
            return 0;
        else
            return 1/clientProber.getRttAvg();
    }

    ServerSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
        this.isActive = false;
        this.subscriptionPool = new SubscriptionPool();
        this.packetIdGenerator = new PacketIdGenerator();
    }
}