package org.dsngroup.broke.client.channel.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
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
