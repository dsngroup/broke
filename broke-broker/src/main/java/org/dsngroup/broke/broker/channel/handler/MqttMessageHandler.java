package org.dsngroup.broke.broker.channel.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.broker.storage.ServerSessionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttMessageHandler extends ChannelInboundHandlerAdapter{

    private final static Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    // TODO: initialize properly
    private ServerSessionPool serverSession;

    private ProtocolProcessor protocolProcessor;

    private ServerContext serverContext;

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
    }

    public MqttMessageHandler(ServerContext serverContext) {
        super();
        this.serverContext = serverContext;
        protocolProcessor = new ProtocolProcessor(serverContext);
    }

}
