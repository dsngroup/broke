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

package org.dsngroup.broke.broker.channel.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.protocol.deprecated.ConnectMessage;
import org.dsngroup.broke.protocol.deprecated.Message;
import org.dsngroup.broke.protocol.deprecated.Method;

/**
 * Handle Client connections
 */
public class ConnectHandler extends ChannelInboundHandlerAdapter {

    private boolean pubsubHandlerSet;

    private ServerContext serverContext;

    public ConnectHandler() {
        super();
        this.pubsubHandlerSet = false;
    }

    /**
     * Accept connection messages from the clients
     * Replace this handler to PublishHandler if the client requests to publish
     * Replace this handler to SubscribeHandler if the client requests to subscribe
     * @param ctx {@see ChannelHandlerContext}
     * @param msg The message of the channel read.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {
            Message newMessage = (Message) msg;

            if (newMessage.getMethod() == Method.CONNECT) {
                ConnectMessage newConnectMessage = (ConnectMessage) newMessage;
                // MessagePool.putContentOnTopic(newMessage.getTopic(), newMessage.getPayload());
                // TODO: We'll log System.out and System.err in the future
                System.out.println("[Connection] QoS: " + newConnectMessage.getQos()
                        + " critical option: " + newConnectMessage.getCriticalOption());

                // Send CONNACK to the client
                // TODO: header definition & Encapsulation
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("CONNACK\r\nQoS:"+newConnectMessage.getQos()
                        +",Critical-Option:"+newMessage.getCriticalOption()
                        +"\r\nConnect Successfully\r\n").getBytes())).sync();

            } else if (newMessage.getMethod() == Method.PUBLISH) {
                // TODO: write the payload to MessagePool
                // TODO: Send PUBACK message to the client

                // Add PublishHandler at last of pipeline and redirect message to it
                if (!pubsubHandlerSet) {
                    ctx.pipeline().addLast(new PublishHandler(serverContext));
                    pubsubHandlerSet = true;
                }
                ctx.fireChannelRead(newMessage);

            } else if (newMessage.getMethod() == Method.SUBSCRIBE ) {
                // TODO: write the payload to MessagePool
                // TODO: Send PUBACK message to the client

                // Add SubscribeHandler at last of pipeline and redirect message to it
                if (!pubsubHandlerSet) {
                    ctx.pipeline().addLast(new SubscribeHandler(serverContext));
                    pubsubHandlerSet = true;
                }
                ctx.fireChannelRead(newMessage);
            }
        } finally {
            // The msg object is an reference counting object.
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * Catch exception, and close connections.
     * @param ctx {@see ChannelHandlerContext}
     * @param cause rethrow
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        // TODO: log this, instead of printStackTrace()
        cause.printStackTrace();
        ctx.close();
    }

    public ConnectHandler(ServerContext serverContext) {
        this.serverContext = serverContext;
    }
}
