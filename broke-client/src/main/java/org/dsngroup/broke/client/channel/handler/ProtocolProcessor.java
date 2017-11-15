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

import org.dsngroup.broke.protocol.MqttConnAckMessage;
import org.dsngroup.broke.protocol.MqttConnectReturnCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolProcessor {

    private final static Logger logger = LoggerFactory.getLogger(ProtocolProcessor.class);
    /**
     * Handle CONNACK
     * @param ctx {@see ChannelHandlerContext}
     * @param mqttConnAckMessage CONNACK message from broker
     * */
    public void processConnAck(ChannelHandlerContext ctx, MqttConnAckMessage mqttConnAckMessage) {
        if (mqttConnAckMessage.variableHeader().connectReturnCode() == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
            logger.info("[Connect] Connect to broker"+ctx.channel().remoteAddress());
        } else {
            logger.error("[Connect] Connection denied, close the channel.");
            ctx.channel().close();
        }
    }

    public ProtocolProcessor() {}
}
