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

package org.dsngroup.broke.client.channel.handler;

import io.netty.channel.ChannelHandlerContext;

import org.dsngroup.broke.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ProtocolProcessor {

    private final static Logger logger = LoggerFactory.getLogger(ProtocolProcessor.class);
    /**
     * Handle CONNACK
     * @param ctx {@see ChannelHandlerContext}
     * @param mqttConnAckMessage CONNACK message from broker
     * */
    public void processConnAck(ChannelHandlerContext ctx, MqttConnAckMessage mqttConnAckMessage) throws Exception {
        if (mqttConnAckMessage.variableHeader().connectReturnCode() == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
            logger.info("[Connect] Connect to broker"+ctx.channel().remoteAddress());
        } else {
            logger.error("[Connect] Connection denied, close the channel.");
            ctx.channel().close();
        }
    }

    /**
     * Handle PUBLISH
     * @param ctx {@see ChannelHandlerContext}
     * @param mqttPublishMessage PUBLISH message from broker
     * */
    public void processPublish(ChannelHandlerContext ctx, MqttPublishMessage mqttPublishMessage) throws Exception {
        // TODO: logger.debug
        logger.info( "Topic: "+mqttPublishMessage.variableHeader().topicName()+
                " Payload: "+mqttPublishMessage.payload().toString(StandardCharsets.UTF_8) );
        // TODO: return PUBACK
    }

    /**
     * Handle PUBACK
     * Remove the corresponding packet id in client session's unacked message queue
     * @param ctx {@see ChannelHandlerContext}
     * @param mqttPubAckMessage PUBACK message from broker
     * */
    public void processPubAck(ChannelHandlerContext ctx, MqttPubAckMessage mqttPubAckMessage) throws Exception {
        // TODO: remove message in client session's unacked message queue
        logger.info("[PUBACK] Packet ID"+mqttPubAckMessage.variableHeader().messageId());
    }

    /**
     * Handle SUBACK
     * @param ctx {@see ChannelHandlerContext}
     * @param mqttSubAckMessage SUBACK message from broker
     * */
    public void processSubAck(ChannelHandlerContext ctx, MqttSubAckMessage mqttSubAckMessage) throws Exception {
        // TODO: logger.debug
        logger.info(mqttSubAckMessage.payload().grantedQoSLevels().get(0).toString());
    }


    public ProtocolProcessor() {}
}
