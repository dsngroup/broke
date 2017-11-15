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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.dsngroup.broke.protocol.*;
import org.dsngroup.broke.broker.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttMessageHandler extends ChannelInboundHandlerAdapter{

    private final static Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    private ProtocolProcessor protocolProcessor;

    private ServerContext serverContext;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            MqttMessage mqttMessage = (MqttMessage) msg;
            switch (mqttMessage.fixedHeader().messageType()) {
                case CONNECT:
                    protocolProcessor.processConnect(ctx.channel(), (MqttConnectMessage) mqttMessage);
                    logger.debug("[MqttMessageHandler] CONNECT");
                    break;
                case PUBLISH:
                    protocolProcessor.processPublish(ctx.channel(), (MqttPublishMessage) mqttMessage);
                    logger.debug("[MqttMessageHandler] PUBLISH");
                    break;
                case SUBSCRIBE:
                    protocolProcessor.processSubscribe(ctx.channel(), (MqttSubscribeMessage) mqttMessage);
                    logger.debug("[MqttMessageHandler] SUBSCRIBE");
                    break;
            }
        } catch (NullPointerException e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Initialization of channel handler
    }

    public MqttMessageHandler(ServerContext serverContext) {
        super();
        this.serverContext = serverContext;
        protocolProcessor = new ProtocolProcessor(serverContext);
    }

}
