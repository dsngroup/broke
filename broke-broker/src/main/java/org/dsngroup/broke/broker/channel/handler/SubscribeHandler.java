package org.dsngroup.broke.broker.channel.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.broker.storage.SubscriberPool;
import org.dsngroup.broke.protocol.Message;
import org.dsngroup.broke.protocol.Method;
import org.dsngroup.broke.protocol.SubscribeMessage;

public class SubscribeHandler extends ChannelInboundHandlerAdapter {

    private SubscriberPool subscriberPool;

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
                subscriberPool.register(subscribeMessage.getTopic(), subscribeMessage.getGroupId(), ctx);

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
        this.subscriberPool = serverContext.getSubscriberPool();
    }

}
