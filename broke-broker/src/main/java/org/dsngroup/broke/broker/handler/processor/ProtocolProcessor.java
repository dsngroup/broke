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

package org.dsngroup.broke.broker.handler.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.broker.dispatch.ClientProber;
import org.dsngroup.broke.protocol.*;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.broker.metadata.ServerSession;
import org.dsngroup.broke.broker.metadata.ServerSessionPool;
import org.dsngroup.broke.broker.metadata.SubscriptionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ProtocolProcessor {

    // Status of the Protocol Processor: Whether the client has sent the CONNECT message.
    private boolean isConnected;

    private static final Logger logger = LoggerFactory.getLogger(ProtocolProcessor.class);

    // Initialized at the construction of protocol processor
    private ServerContext serverContext;

    // Initialized if CONNECT is accepted.
    private ServerSession serverSession;

    private MessagePublisher messagePublisher;

    private ClientProber clientProber;

    /**
     * The logic to deal with CONNECT message.
     * @param channel Channel the protocol processor belongs to
     * @param mqttConnectMessage Instance of MqttConnectMessage
     */
    public void processConnect(Channel channel, MqttConnectMessage mqttConnectMessage) {

        // Close the channel if receive the connect message second time.
        if (this.isConnected) {
            channel.close();
        } else {
            // Mark the channel as is connected when first received a connect message
            this.isConnected = true;

            // Get session
            String clientId = mqttConnectMessage.payload().clientIdentifier();
            boolean cleanSession = mqttConnectMessage.variableHeader().isCleanSession();

            // Get server session pool from the server context
            ServerSessionPool serverSessionPool = serverContext.getServerSessionPool();

            // For a session, accept one client only (specified using clientId)
            synchronized (this) {
                if (!serverSessionPool.isSessionActive(clientId)) {
                    // Accept the connection: initialize the server session
                    serverSession = serverSessionPool.getSession(clientId, cleanSession);
                    serverSession.setIsActive(true);
                    // Response the client a CONNACK with return code CONNECTION_ACCEPTED
                    MqttConnAckMessage mqttConnAckMessage = connAck(
                            MqttConnectReturnCode.CONNECTION_ACCEPTED,
                            mqttConnectMessage
                    );
                    channel.writeAndFlush(mqttConnAckMessage);
                    clientProber.schedulePingReq(channel);
                    serverSession.setClientProber(clientProber);
                } else {
                    // Reject the connection
                    // Response the client a CONNACK with return code CONNECTION_REJECTED
                    MqttConnAckMessage mqttConnAckMessage = connAck(
                            MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED,
                            mqttConnectMessage
                    );
                    channel.writeAndFlush(mqttConnAckMessage);
                }
            }
        }
    }

    /**
     * Create the Mqtt CONNACK message with given return code and original CONNECT message.
     * @param returnCode The return code of CONNACK
     * @param mqttConnectMessage original CONNECT message
     * @return created MqttConnAckMessage instance.
     */
    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode, MqttConnectMessage mqttConnectMessage) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false,
                mqttConnectMessage.fixedHeader().qosLevel(), false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader =
                new MqttConnAckVariableHeader(returnCode, true);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    /**
     * process PUBLISH message using messagePublisher.
     * @param ctx {@see ChannelHandlerContext}
     * @param mqttPublishMessage PUBLISH message from the client
     */
    public void processPublish(ChannelHandlerContext ctx, MqttPublishMessage mqttPublishMessage) {

        if (isConnected) {
            MqttQoS qos = mqttPublishMessage.fixedHeader().qosLevel();
            switch (qos) {
                case AT_MOST_ONCE:
                    messagePublisher.processQos0Publish(ctx, serverContext, mqttPublishMessage);
                    break;
                case AT_LEAST_ONCE:
                    messagePublisher.processQos1Publish(ctx, serverContext, mqttPublishMessage);
                    break;
            }
        } else {
            logger.error("[Protocol Processor] Not connected, cannot process publish");
        }
    }

    /**
     * Process PUBACK.
     * @param mqttPubAckMessage puback message from the subscribe client.
     */
    public void processPubAck(MqttPubAckMessage mqttPubAckMessage) {
        // nop currently.
    }

    /**
     * Process SUBSCRIBE
     * Registor all of the subscriptions to the subscription pool in the server session.
     * @param channel {@see channel}
     * @param mqttSubscribeMessage SUBSCRIBE message from the client
     */
    public void processSubscribe(Channel channel, MqttSubscribeMessage mqttSubscribeMessage) {

        if (isConnected) {
            SubscriptionPool subscriptionPool = serverSession.getSubscriptionPool();

            List<MqttQoS> grantedQosList = new ArrayList<>();
            for (MqttTopicSubscription mqttTopicSubscription : mqttSubscribeMessage.payload().topicSubscriptions()) {
                subscriptionPool.register(mqttTopicSubscription.topicName(),
                        mqttTopicSubscription.qualityOfService(),
                        mqttTopicSubscription.groupId(),
                        channel);
                grantedQosList.add(mqttTopicSubscription.qualityOfService());
            }

            MqttSubAckMessage mqttSubAckMessage = subAck(mqttSubscribeMessage.fixedHeader().qosLevel(),
                    mqttSubscribeMessage.variableHeader().messageId(),
                    grantedQosList);
            channel.writeAndFlush(mqttSubAckMessage);
        } else {
            logger.error("[Protocol Processor] Not connected, cannot process subscribe");
        }

    }

    /**
     * Create the SUBACK message.
     * @param qos QoS of the SUBACK message
     * @param packetId packetId corresponding to the SUBSCRIBE message
     * @param grantedQosList granted QoS List of the corresponding SUBSCRIBE
     * @return Created SUBACK message
     */
    private MqttSubAckMessage subAck(MqttQoS qos, int packetId, List<MqttQoS> grantedQosList) {

        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.SUBACK, false, qos, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(packetId);
        List<Integer> grantedQoSInteger = new ArrayList<>();
        for (MqttQoS grantedQos: grantedQosList) {
            grantedQoSInteger.add(grantedQos.value());
        }
        MqttSubAckPayload mqttSubAckPayload = new MqttSubAckPayload(grantedQoSInteger);
        return new MqttSubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubAckPayload);
    }

    public void processPingResp(MqttPingRespMessage mqttPingRespMessage) {
        int packetId = mqttPingRespMessage.variableHeader().packetId();
        boolean isBackPressured = mqttPingRespMessage.variableHeader().isBackPressured();
        clientProber.setPingResp(packetId);
        clientProber.setIsBackPressured(isBackPressured);
    }

    /**
     * process DISCONNECT message from the client
     * If the session is used by this channel
     * 1. Set the session's isActive to false
     * 2. Close the PINGREQ schedule.
     */
    public void processDisconnect() {
        if (isConnected) {
            isConnected = false;
            synchronized (this) {
                serverSession.setIsActive(false);
            }
            clientProber.cancelPingReq();
        }
    }

    /**
     * The constructor of the protocol processor.
     * Initialize the isConnected status to false
     * Create the message publisher
     * @param serverContext the global server context
     */
    public ProtocolProcessor(ServerContext serverContext) {
        this.isConnected = false;
        this.serverContext = serverContext;
        this.messagePublisher = new MessagePublisher();
        this.clientProber = new ClientProber();
    }
}
