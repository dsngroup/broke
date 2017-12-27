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

package org.dsngroup.broke.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.dsngroup.broke.client.handler.callback.IMessageCallbackHandler;
import org.dsngroup.broke.client.exception.ConnectLostException;
import org.dsngroup.broke.client.storage.IPublishMessageQueue;
import org.dsngroup.broke.client.util.ClientIdGenerator;
import org.dsngroup.broke.client.util.PacketIdGenerator;
import org.dsngroup.broke.client.handler.MqttMessageHandler;
import org.dsngroup.broke.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The BlockClient should be deprecated after the asynchronous client is implemented.
 * Only used for test!
 */
public class BlockClient {

    private String targetBrokerAddress;

    private int targetBrokerPort;

    private ClientContext clientContext;

    private Channel targetServerChannel;

    private EventLoopGroup workerGroup;

    private String clientId;

    private PacketIdGenerator packetIdGenerator = new PacketIdGenerator();

    private final static Logger logger = LoggerFactory.getLogger(BlockClient.class);

    private MqttMessageHandler mqttMessageHandler;

    /**
     * Getter for client ID.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter for publish message queue.
     * @param publishMessageQueue The publish message queue.
     */
    public void setPublishMessageQueue(IPublishMessageQueue publishMessageQueue) {
        clientContext.getClientSession().setPublishMessageQueue(publishMessageQueue);
    }

    /**
     * Send CONNECT to server.
     * @param qos qos option
     * @param criticalOption Critical Option
     */
    public void connect(MqttQoS qos, int criticalOption) throws Exception {

        // Connect only when the channel is active
        if(targetServerChannel.isActive()) {

            // Create CONNECT message
            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.CONNECT, false, qos,
                    true, 0);
            MqttConnectVariableHeader mqttConnectVariableHeader =
                    new MqttConnectVariableHeader("MQTT", 4, false, false,
                            true, 1, true, false, 10);
            MqttConnectPayload mqttConnectPayload =
                    new MqttConnectPayload(clientId, "Foo", "Bar".getBytes(), null, null);

            targetServerChannel.pipeline().writeAndFlush(
                    new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload));

        } else {
            throw new ConnectLostException("CONNECT_DENIED");
        }

    }

    /**
     * Send the PUBLISH message to server
     * @param topic topic to publish
     * @param qos qos of transmission
     * @param criticalOption TODO: critical option mechanism
     * @param payload payload of the message
     */
    public void publish(String topic, MqttQoS qos, int criticalOption, String payload) throws Exception {
        if (targetServerChannel.isActive()) {

            int packetId = packetIdGenerator.getPacketId();

            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos,
                            true, 0);
            MqttPublishVariableHeader mqttPublishVariableHeader =
                    new MqttPublishVariableHeader(topic, packetId);
            ByteBuf publisherPayload = Unpooled.wrappedBuffer(payload.getBytes());

            targetServerChannel.writeAndFlush(
                    new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, publisherPayload)
            );

        } else {
            throw new ConnectLostException("CANNOT_PUBLISH");
        }
    }

    /**
     * Send the SUBSCRIBE message to server
     * @param topic The topic to subscribe.
     * @param qos QoS of transmission.
     * @param groupId The user-defined consumer group id for data parallel
     * */
    public void subscribe(String topic, MqttQoS qos, int criticalOption, int groupId) throws Exception {

        if (targetServerChannel.isActive()) {
            MqttFixedHeader subscriberFixedHeader =
                    new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, qos, true, 0);
            MqttMessageIdVariableHeader subscriberVariableHeader = MqttMessageIdVariableHeader.from(666);
            List<MqttTopicSubscription> mqttTopicSubscriptionList = new ArrayList<>();
            mqttTopicSubscriptionList.add(new MqttTopicSubscription(topic, MqttQoS.AT_LEAST_ONCE, groupId));
            MqttSubscribePayload subscriberSubscribePayload = new MqttSubscribePayload(mqttTopicSubscriptionList);
            MqttSubscribeMessage mqttSubscribeMessage =
                    new MqttSubscribeMessage(subscriberFixedHeader, subscriberVariableHeader, subscriberSubscribePayload);
            targetServerChannel.writeAndFlush(mqttSubscribeMessage);

        } else {
            throw new ConnectLostException("CANNOT_SUBSCRIBE");
        }
    }

    /**
     * Disconnect: send DISCONNECT to server and close the channel.
     */
    public void disconnect() {
        if (targetServerChannel.isActive()) {
            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessage disconnectMessage = new MqttMessage(mqttFixedHeader);

            targetServerChannel.writeAndFlush(disconnectMessage);
        }
        targetServerChannel.close();
    }

    /**
     * Setter for message callback handler
     * @param messageCallbackHandler user-defined message callback handler
     */
    public void setMessageCallbackHandler(IMessageCallbackHandler messageCallbackHandler) {
        mqttMessageHandler.setMessageCallbackHandler(messageCallbackHandler);
    }

    /**
     * The default constructor
     */
    public BlockClient() throws Exception {
        this("0.0.0.0", 8181);
    }

    /**
     * The constructor without specifying clientId
     */
    public BlockClient(String targetBrokerAddress, int targetBrokerPort) throws Exception {
        this(targetBrokerAddress, targetBrokerPort, ClientIdGenerator.getClientId());
    }

    /**
     * The constructor for creating a BlockClient
     * 1. Build a connection to client
     * 2. Send a CONNECT message to server.
     * @param targetBrokerAddress The address of broker server.
     * @param targetBrokerPort The port of broker server.
     */
    public BlockClient(String targetBrokerAddress, int targetBrokerPort, String clientId) throws Exception {
        this.targetBrokerAddress = targetBrokerAddress;
        this.targetBrokerPort = targetBrokerPort;
        this.clientId = clientId;
        this.clientContext = new ClientContext(clientId);

        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(MqttEncoder.INSTANCE);
                            ch.pipeline().addLast(new MqttDecoder());
                            ch.pipeline().addLast(mqttMessageHandler = new MqttMessageHandler(clientContext));
                        }
                    });

            // Block until the connection built
            ChannelFuture future = b.connect(this.targetBrokerAddress, this.targetBrokerPort).sync();
            targetServerChannel = future.channel();
            targetServerChannel.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    workerGroup.shutdownGracefully();
                }
            });

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}

