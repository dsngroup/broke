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

package org.dsngroup.broke.client.channel.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.dsngroup.broke.protocol.deprecated.Message;
import org.dsngroup.broke.protocol.deprecated.MessageBuilder;

import java.util.ArrayDeque;

public class MessageParseHandler extends ChannelInboundHandlerAdapter {

    /**
     * Read the message from channel, parse to Message instance and send to next handler.
     * @param ctx {@see ChannelHandlerContext}
     * @param msg The message of the channel read
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf incoming = (ByteBuf)msg;
        StringBuilder packet = new StringBuilder();
        ArrayDeque<Message> newMessages = new ArrayDeque();

        try {
            // TODO: Investigate the encode/decode mechanism
            while(incoming.isReadable()) {
                packet.append((char)incoming.readByte());
            }

            String[] segments = packet.toString().split("\r\n");
            for(int i=0; i<segments.length; i+=3) {
                if( MessageBuilder.isRequestField(segments[i]) ) {
                    Message newMessage = MessageBuilder.build(segments[i], segments[i+1], segments[i+2]);
                    newMessages.add(newMessage);
                }
            }

            for (Message newMessage: newMessages) {
                ctx.fireChannelRead(newMessage);
            }

        } finally {
            // The msg object is an reference counting object.
            ReferenceCountUtil.release(msg);
        }

    }


    /**
     * Catch exception, and close connection.
     * @param ctx {@see ChannelHandlerContext}
     * @param cause rethrow
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        // TODO: log this, instead of printStackTrace()
        cause.printStackTrace();
        ctx.close();
    }

}
