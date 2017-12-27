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

package org.dsngroup.broke.broker.handler.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    void processQos0Publish(ChannelHandlerContext ctx, ServerContext serverContext,
                            MqttPublishMessage mqttPublishMessage) {
        // TODO
    }

    void processQos1Publish(ChannelHandlerContext ctx, ServerContext serverContext,
                            MqttPublishMessage mqttPublishMessage) {

        try {
            if (ctx.channel().isActive()) {
                Channel channel = ctx.channel();

                int packetId = mqttPublishMessage.variableHeader().packetId();

                if (mqttPublishMessage.payload().isReadable()) {
                    if (mqttPublishMessage.fixedHeader().isRetain()) {
                        mqttPublishMessage.payload().retain();
                    }
                    publishToSubscriptions(serverContext, mqttPublishMessage);
                }

                MqttPubAckMessage mqttPubAckMessage = pubAck(MqttQoS.AT_LEAST_ONCE, packetId);
                channel.writeAndFlush(mqttPubAckMessage);

            } else {
                logger.error("Inactive channel");
            }
        } catch (NullPointerException e) {
            logger.error("Null PUBLISH payload: " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Publish the message to all sessions.
     * @param serverContext server context
     * @param mqttPublishMessage The PUBLISH message from the publisher
     */
    private void publishToSubscriptions(ServerContext serverContext,
                                        MqttPublishMessage mqttPublishMessage) {
        // serverContext.publishToSubscription(mqttPublishMessage);
        serverContext.getMessageDispatcher().groupBasedPublishToSubscription(mqttPublishMessage);
    }

    private MqttPubAckMessage pubAck(MqttQoS qos, int packetId) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.PUBACK, false, qos, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader =
                MqttMessageIdVariableHeader.from(packetId);

        return new MqttPubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader);
    }
}
