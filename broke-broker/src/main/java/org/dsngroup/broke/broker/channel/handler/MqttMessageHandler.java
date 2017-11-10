package org.dsngroup.broke.broker.channel.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import org.dsngroup.broke.broker.storage.ServerSessionCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttMessageHandler extends ChannelInboundHandlerAdapter{

    private final static Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    // TODO: initialize properly
    private ServerSessionCollection serverSession;

    private ProtocolProcessor protocolProcessor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            MqttMessage mqttMessage = (MqttMessage) msg;
            switch (mqttMessage.fixedHeader().messageType()) {
                case CONNECT:
                    protocolProcessor.processConnect(ctx, (MqttConnectMessage) mqttMessage);
                    break;
            }
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Initialization of channel handler
        protocolProcessor = new ProtocolProcessor();

    }

}
