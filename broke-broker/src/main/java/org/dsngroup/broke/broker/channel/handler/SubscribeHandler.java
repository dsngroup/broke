package org.dsngroup.broke.broker.channel.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.dsngroup.broke.broker.storage.SubscriberPool;
import org.dsngroup.broke.protocol.Message;
import org.dsngroup.broke.protocol.Method;
import org.dsngroup.broke.protocol.SubscribeMessage;

public class SubscribeHandler extends ChannelInboundHandlerAdapter {

    /**
     * Read the message from channel and register the subscriber to {@see SubscriberPool}
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
                SubscriberPool.register(subscribeMessage.getTopic(), subscribeMessage.getGroupId(), ctx);
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

}
