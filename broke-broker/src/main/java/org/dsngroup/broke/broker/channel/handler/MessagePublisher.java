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

package org.dsngroup.broke.broker.channel.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.broker.storage.MessagePool;
import org.dsngroup.broke.broker.storage.ServerSession;
import org.dsngroup.broke.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    public void processQos0Publish(Channel channel, ServerContext ctx, MqttPublishMessage mqttPublishMessage) {
        // TODO
    }

    public void processQos1Publish(Channel channel, ServerContext serverContext, MqttPublishMessage mqttPublishMessage) {

        try {
            if(channel.isActive()) {
                MessagePool messagePool = serverContext.getMessagePool();

                String topic = mqttPublishMessage.variableHeader().topicName();
                int packetId = mqttPublishMessage.variableHeader().packetId();
                ByteBuf appMessage = mqttPublishMessage.payload();

                if (appMessage.isReadable()) {
                    messagePool.putContentOnTopic(topic, appMessage);
                } else {
                    // TODO: handle unreadable payload
                }

                publishToSubscriptions(serverContext, mqttPublishMessage);

                MqttPubAckMessage mqttPubAckMessage = pubAck(channel, MqttQoS.AT_LEAST_ONCE, packetId);
                channel.writeAndFlush(mqttPubAckMessage);

            } else {
                logger.error("Inactive channel");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    private void publishToSubscriptions(ServerContext serverContext,
                                      MqttPublishMessage mqttPublishMessage) {
        // TODO: publish messages to the subscriptions in every server session
        // Iterate through all sessions, publishing to every sessions' subscriptions
        for (ServerSession serverSession: serverContext.getServerSessionPool().asCollection()) {
            // Only publish to active sessions
            if(serverSession.getIsActive()) {
                // Whether the mqttPublish Message matches the subscription is performed in "sendToSubscribers"
                serverSession.getSubscriptionPool().sendToSubscribers(mqttPublishMessage);
            }
        }
    }

    private MqttPubAckMessage pubAck(Channel channel, MqttQoS qos, int packetId) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.PUBACK, false, qos, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader =
                MqttMessageIdVariableHeader.from(packetId);

        return new MqttPubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader);
    }

    public MessagePublisher() {
    }
}
