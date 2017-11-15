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

import java.nio.charset.Charset;
import java.util.Random;

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

    private final static Logger logger = LoggerFactory.getLogger(BlockClient.class);

    /**
     * Send CONNECT to server
     * TODO: the CONNECT message may contain user and authentication information
     * @param qos qos option
     * @param payload payload
     * */
    public void connect(int qos, int criticalOption, String payload) throws Exception {

        // Connect only when the channel is active
        if(targetServerChannel.isActive()) {
            logger.info("[Client] Make connection");

            // TODO: how to set the remaining length correctly
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                    MqttMessageType.CONNECT,
                    false,
                    MqttQoS.AT_LEAST_ONCE,
                    true,
                    0);

            MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(
                    "MQTT",
                    4,
                    false,
                    false,
                    true,
                    1,
                    true,
                    false,
                    10);

            MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(
                    clientId,
                    "Foo",
                    "Bar".getBytes(),
                    null,
                    null
            );

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
        }

    }

    /**
     * Send the PUBLISH message to server
     * @param topic topic to publish
     * @param qos qos of transmission
     * @param criticalOption TODO: critical option mechanism
     * @param payload payload of the message
     */
    public void publish(String topic, int qos, int criticalOption, String payload) throws Exception {
        // TODO: Send this message
        if (targetServerChannel.isActive()) {
            targetServerChannel.pipeline().writeAndFlush(Unpooled.wrappedBuffer(
                    ("PUBLISH\r\nQoS:"+qos+",Topic:"+topic+",Critical-Option:"+criticalOption+"\r\n"+payload+"\r\n")
                    .getBytes(Charset.forName("UTF-8")))).sync();
            // clientOutputStream.write(msg.toString().getBytes(Charset.forName("UTF-8")));
            logger.info("[Publish] Topic: " + topic + " Payload: " + payload);
        } else {
            logger.error("[Publish] Channel closed, cannot publish");
        }
    }

    /**
     * Send the SUBSCRIBE message to server
     * @param topic The topic to subscribe.
     * @param qos QoS of transmission.
     * @param groupId The user-defined consumer group id for data parallel
     * @param payload payload
     * */
    public void subscribe(String topic, int qos, int criticalOption, String groupId, String payload) throws Exception {
        targetServerChannel.pipeline().writeAndFlush(Unpooled.wrappedBuffer(
                ("SUBSCRIBE\r\nQoS:"+qos+",Topic:"+topic+",critical-option:"+criticalOption+",group-id:"+groupId+"\r\n"+payload+"\r\n")
                .getBytes(Charset.forName("UTF-8")))).sync();
        logger.info("[Subscribe] Topic: " + topic+" Payload: " + payload );
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
                            ch.pipeline().addLast(new MqttMessageHandler());
                        }
                    });

            // Block until the connection built
            ChannelFuture future = b.connect(this.targetBrokerAddress, this.targetBrokerPort).sync();
            targetServerChannel = future.channel();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public boolean isActive() {
        return targetServerChannel.isActive();
    }

    public void close() throws Exception {
        // Request to close this Channel and notify the ChannelFuture once the operation completes
        ChannelFuture future = targetServerChannel.close();
        // Block until the channel closed.
        // Block until the channel closed.
        future.channel().closeFuture().sync();
        workerGroup.shutdownGracefully();
    }

}
