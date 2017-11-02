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
import org.dsngroup.broke.client.channel.handler.AckHandler;

import java.nio.charset.Charset;

import org.dsngroup.broke.client.channel.handler.MessageParseHandler;

/**
 * The BlockClient should be deprecated after the asynchronous client is implemented.
 * Only used for test!
 */
public class BlockClient {

    private String targetBrokerAddress;

    private int targetBrokerPort;

    private Channel targetServerChannel;

    private EventLoopGroup workerGroup;

    /**
     * Send CONNECT to server
     * TODO: the CONNECT message may contain user and authentication information
     * @param qos qos option
     * @param payload payload
     * */
    public void connect(int qos, int criticalOption, String payload) throws Exception {

        // Connect only when the channel is active
        if(targetServerChannel.isActive()) {
            System.out.println("[Client] Make connection");
            targetServerChannel.pipeline().writeAndFlush(Unpooled.wrappedBuffer(("CONNECT\r\nQoS:"+qos+",Critical-Option:"+criticalOption+"\r\n"+payload+"\r\n")
                    .getBytes(Charset.forName("UTF-8")))).sync();
        } else {
            System.out.println("[Client] Channel is not active");
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
            // TODO: We'll log System.out and System.err in the future
            System.out.println("[Publish] Topic: " + topic + " Payload: " + payload);
        } else {
            // TODO: log this
            System.out.println("[Publish] Channel closed, cannot publish");
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
        // TODO: We'll log System.out and System.err in the future
        System.out.println("[Subscribe] Topic: " + topic+" Payload: " + payload );
    }

    /**
     * The default constructor
     */
    public BlockClient() throws Exception {
        this("0.0.0.0", 8181);
    }

    /**
     * The constructor for creating a BlockClient
     * 1. Build a connection to client
     * 2. Send a CONNECT message to server.
     * @param targetBrokerAddress The address of broker server.
     * @param targetBrokerPort The port of broker server.
     */
    public BlockClient(String targetBrokerAddress, int targetBrokerPort) throws Exception {
        this.targetBrokerAddress = targetBrokerAddress;
        this.targetBrokerPort = targetBrokerPort;

        workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MessageParseHandler());
                            ch.pipeline().addLast(new AckHandler());
                        }
                    });

            // Block until the connection built
            ChannelFuture future = b.connect(this.targetBrokerAddress, this.targetBrokerPort).sync();
            targetServerChannel = future.channel();

        } catch (Exception e) {
            // TODO: log this
            e.printStackTrace();
        }
    }

    public boolean isActive() {
        return targetServerChannel.isActive();
    }

    public void close() throws Exception {
        // Request to close this Channel and notify the ChannelFuture once the operation completes
        ChannelFuture future = targetServerChannel.close();
        // Block until the channel closed.
        future.channel().closeFuture().sync();
        workerGroup.shutdownGracefully();
    }

}
