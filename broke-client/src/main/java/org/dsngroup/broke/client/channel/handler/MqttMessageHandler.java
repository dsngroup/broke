package org.dsngroup.broke.client.channel.handler;

import io.netty.channel.*;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.Channel;

public class MqttMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    private ProtocolProcessor protocolProcessor = new ProtocolProcessor();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        MqttMessage mqttMessage = (MqttMessage) msg;
        logger.debug("[Client] Message in");
        try {
            switch (mqttMessage.fixedHeader().messageType()) {
                case CONNACK:
                    protocolProcessor.processConnAck(ctx, (MqttConnAckMessage)mqttMessage);
                    break;
            }

        } finally {
            // The msg object is an reference counting object.
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        protocolProcessor = new ProtocolProcessor();
    }


}
