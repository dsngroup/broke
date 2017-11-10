package org.dsngroup.broke.broker.channel.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.dsngroup.broke.broker.storage.ServerSession;
import org.dsngroup.broke.broker.storage.ServerSessionCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolProcessor {

    // Status of the Protocol Processeor: Whether the client has sent the CONNECT message.
    private boolean isConnected;

    private final static Logger logger = LoggerFactory.getLogger(ProtocolProcessor.class);

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
            // TODO: evaluate again either use single pattern or static methods
            ServerSessionCollection serverSessionCollection = ServerSessionCollection.getServerSessionCollection();

            // For a session, accept one client only (specified using clientId)
            // TODO: How to gracefully sets the isActive status when the connection closed,
            // TODO: no matter when a normal or abnormal termination occurred.
            synchronized (ProtocolProcessor.class) {
                if (!serverSessionCollection.isSessionActive(clientId)) {
                    serverSession = serverSessionCollection.getSession(clientId, cleanSession);
                    serverSession.isActive = true;
                    // Accept the connection
                    MqttConnAckMessage mqttConnAckMessage = connAck(
                            MqttConnectReturnCode.CONNECTION_ACCEPTED,
                            mqttConnectMessage
                    );
                    ctx.channel().writeAndFlush(mqttConnAckMessage);
                } else {
                    // Reject the connection
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

    public ProtocolProcessor() {
        this.isConnected = false;
    }
}
