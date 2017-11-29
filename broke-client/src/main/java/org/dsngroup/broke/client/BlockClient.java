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
import org.dsngroup.broke.protocol.*;
import org.dsngroup.broke.client.channel.handler.MqttMessageHandler;
import org.dsngroup.broke.protocol.MqttEncoder;
import org.dsngroup.broke.protocol.MqttDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static Random rand = new Random();

    private String clientId;

    private PacketIdGenerator packetIdGenerator = new PacketIdGenerator();

    private final static Logger logger = LoggerFactory.getLogger(BlockClient.class);

    /**
     * Send CONNECT to server
     * @param qos qos option
     * */
    public void connect(int qos, int criticalOption) throws Exception {

        // Connect only when the channel is active
        if(targetServerChannel.isActive()) {
            // TODO: delete this
            logger.debug("[Client] Make connection, clientId: "+clientId);

            // Create CONNECT message
            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE,
                    true, 0);
            MqttConnectVariableHeader mqttConnectVariableHeader =
                    new MqttConnectVariableHeader("MQTT", 4, false, false,
                            true, 1, true, false, 10);
            MqttConnectPayload mqttConnectPayload =
                    new MqttConnectPayload(clientId, "Foo", "Bar".getBytes(), null, null);

            ChannelFuture future = targetServerChannel.pipeline().writeAndFlush(
                    new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload));
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.cause()!=null) {
                        logger.error("[Connect] Write CONNECT failed: "+future.cause());
                        System.exit(1);
                    }
                }
            });

        } else {
            logger.error("[Client] Channel is not active");
            throw new ConnectDeniedException("CONNECT_DENIED");
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

            // TODO: publish statistics.
            // TODO debug
            logger.info("[Publish] Topic: " + topic + " Payload: " + payload);
        } else {
            logger.error("[Publish] Channel closed, cannot publish");
            throw new ConnectDeniedException("CONNECT_DENIED");
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

            ChannelFuture future = targetServerChannel.writeAndFlush(mqttSubscribeMessage);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(!future.isSuccess()) {
                        logger.error("Write subscribe failed");
                    }
                }
            });

            logger.info("[Subscribe] Topic: " + topic);
        } else {
            logger.error("[Publish] Channel closed, cannot subscribe");
            throw new ConnectDeniedException("CONNECT_DENIED");
        }
    }

    /**
     * Disconnect: send DISCONNECT to server and close the channel.
     * */
    public void disconnect() {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_LEAST_ONCE, false, 0 );
        MqttMessage disconnectMessage = new MqttMessage(mqttFixedHeader);

        targetServerChannel.writeAndFlush(disconnectMessage);
        ChannelFuture future = targetServerChannel.closeFuture();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isDone()) {
                    workerGroup.shutdownGracefully();
                }
            }
        });
    }


    /**
     * The default constructor
     */
    public BlockClient() throws Exception {
        this("0.0.0.0", 8181);
    }

    /**
     * The constructor without specifying clientId
     * */
    public BlockClient(String targetBrokerAddress, int targetBrokerPort) throws Exception {
        this(targetBrokerAddress, targetBrokerPort, "client_"+(rand.nextInt(10000) + 1));
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
                            ch.pipeline().addLast(new MqttMessageHandler(clientContext));
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
            // TODO: delete
            logger.debug(e.getStackTrace().toString());
        }
    }

    public boolean isActive() {
        return targetServerChannel.isActive();
    }

    public void close() throws Exception {
        // Request to close this Channel and notify the ChannelFuture once the operation completes
        ChannelFuture future = targetServerChannel.close();
        targetServerChannel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                workerGroup.shutdownGracefully();
            }
        });
    }

}

/**
 * Packet Id should between 1~65535
 * */
class PacketIdGenerator {

    private AtomicInteger packetId;

    int getPacketId() {
        int retVal = packetId.getAndIncrement();
        if(retVal > 65535) {
            synchronized (this) {
                if(packetId.get() > 65535) {
                    packetId.set(1);
                    retVal = packetId.getAndIncrement();
                }
            }
        }
        return retVal;

    }

    PacketIdGenerator() {
        packetId = new AtomicInteger();
        packetId.set(1);
    }

}
