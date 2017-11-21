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

import io.netty.channel.*;

import org.dsngroup.broke.protocol.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqttMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    private ProtocolProcessor protocolProcessor = new ProtocolProcessor();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(!(msg instanceof MqttMessage)) {
            logger.error("Undefined message");
            System.exit(1);
        }
        MqttMessage mqttMessage = (MqttMessage) msg;
        try {
            switch (mqttMessage.fixedHeader().messageType()) {
                case CONNACK:
                    protocolProcessor.processConnAck(ctx, (MqttConnAckMessage)mqttMessage);
                    break;
                case PUBLISH:
                    protocolProcessor.processPublish(ctx, (MqttPublishMessage)mqttMessage);
                    break;
                case PUBACK:
                    protocolProcessor.processPubAck(ctx, (MqttPubAckMessage)mqttMessage);
                    break;
                case SUBACK:
                    protocolProcessor.processSubAck(ctx, (MqttSubAckMessage)mqttMessage);
                    break;
                default:
                    logger.error("invalid message: "+msg.toString());
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // The msg object is an reference counting object.
            // ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
        logger.error(cause.getStackTrace().toString());
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        protocolProcessor = new ProtocolProcessor();
    }


}
