package org.dsngroup.broke.broker.channel.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.dsngroup.broke.protocol.Message;
import org.dsngroup.broke.protocol.MessageBuilder;

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
