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
import org.dsngroup.broke.broker.storage.SubscriptionPool;
import org.dsngroup.broke.protocol.deprecated.Message;
import org.dsngroup.broke.protocol.deprecated.Method;
import org.dsngroup.broke.protocol.deprecated.SubscribeMessage;

public class SubscribeHandler extends ChannelInboundHandlerAdapter {

    private SubscriptionPool subscriptionPool;

    /**
     * Read the message from channel and register the subscriber to {@see SubscriptionPool}
     * @param ctx {@see ChannelHandlerContext}
     * @param msg The message of the channel read.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Message newMessage = (Message) msg;
        try {
            if (newMessage.getMethod() == Method.SUBSCRIBE) {
                SubscribeMessage subscribeMessage = (SubscribeMessage)newMessage;

                // TODO: We'll log System.out and System.err in the future
                System.out.println("[Subscribe] Topic: " + subscribeMessage.getTopic() +
                        " Group ID: " + subscribeMessage.getGroupId());

                // Register the subscriber and ignore the returned subscriber instance
                // subscriptionPool.register(subscribeMessage.getTopic(), subscribeMessage.getQos(), subscribeMessage.getGroupId(), ctx);

                // Send PUBACK to the client
                // TODO: header definition & Encapsulation
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("SUBACK\r\nQoS:"+subscribeMessage.getQos()
                        +",Critical-Option:"+newMessage.getCriticalOption()
                        +",Topic:"+subscribeMessage.getTopic()
                        +"\r\nSubscribe Successfully\r\n").getBytes())).sync();

            }

        } finally {
            // The message object is a reference counting object
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
        // TODO: log this, instead of printStackTrace()
        cause.printStackTrace();
        ctx.close();
    }

    public SubscribeHandler(ServerContext serverContext) {
        // this.subscriptionPool = serverContext.getSubscriptionPool();
    }

}
