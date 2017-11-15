package org.dsngroup.broke.broker.channel.handler;

import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.protocol.*;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.broker.storage.ServerSession;
import org.dsngroup.broke.broker.storage.ServerSessionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolProcessor {

    // Status of the Protocol Processeor: Whether the client has sent the CONNECT message.
    private boolean isConnected;

    private final static Logger logger = LoggerFactory.getLogger(ProtocolProcessor.class);

    // Initialized at the construction of protocol processor
    private ServerSessionPool serverSessionPool;

    // Initialized if CONNECT is accepted.
    private ServerSession serverSession;

    /**
     * The logic to deal with CONNECT message.
     * @param ctx Channel Handler Context {@see ChannelHandlerContext}
     * @param mqttConnectMessage Instance of MqttConnectMessage
     * */
    public void processConnect(ChannelHandlerContext ctx, MqttConnectMessage mqttConnectMessage) {

        // Close the channel if receive the connect message second time.
        if(this.isConnected) {
            ctx.channel().close();
        } else {
            // Mark the channel as is connected when first received a connect message
            this.isConnected = true;

            // Get session
            String clientId = mqttConnectMessage.payload().clientIdentifier();
            boolean cleanSession = mqttConnectMessage.variableHeader().isCleanSession();

            // For a session, accept one client only (specified using clientId)
            // TODO: How to gracefully sets the isActive status when the connection closed,
            // TODO: no matter when a normal or abnormal termination occurred.
            synchronized (ProtocolProcessor.class) {
                if (!serverSessionPool.isSessionActive(clientId)) {
                    // Accept the connection: initialize the server session
                    serverSession = serverSessionPool.getSession(clientId, cleanSession);
                    serverSession.isActive = true;
                    // Response the client a CONNACK with return code CONNECTION_ACCEPTED
                    MqttConnAckMessage mqttConnAckMessage = connAck(
                            MqttConnectReturnCode.CONNECTION_ACCEPTED,
                            mqttConnectMessage
                    );
                    ctx.channel().writeAndFlush(mqttConnAckMessage);
                } else {
                    // Reject the connection
                    // Response the client a CONNACK with return code CONNECTION_REJECTED
                    MqttConnAckMessage mqttConnAckMessage = connAck(
                            MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED,
                            mqttConnectMessage
                    );
                    ctx.channel().writeAndFlush(mqttConnAckMessage);

                }
            }

        }
    }

    /**
     * Create the Mqtt CONNACK message with given return code and original CONNECT message
     * @param returnCode The return code of CONNACK
     * @param mqttConnectMessage original CONNECT message
     * @return created MqttConnAckMessage instance.
     * */
    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode, MqttConnectMessage mqttConnectMessage) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, mqttConnectMessage.fixedHeader().qosLevel(),
                false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(returnCode, true);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    public ProtocolProcessor(ServerContext serverContext) {
        this.isConnected = false;
        serverSessionPool = serverContext.getServerSessionPool();
    }
}
