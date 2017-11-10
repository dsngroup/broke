package org.dsngroup.broke.broker.channel.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.netty.util.ReferenceCountUtil;
import org.dsngroup.broke.broker.storage.MessagePool;
import org.dsngroup.broke.broker.storage.SubscriberPool;
import org.dsngroup.broke.protocol.Message;
import org.dsngroup.broke.protocol.Method;
import org.dsngroup.broke.protocol.PublishMessage;

public class PublishHandler extends ChannelInboundHandlerAdapter {

    /**
     * Read the message from channel and publish to {@link MessagePool}
     * @param ctx {@see ChannelHandlerContext}
     * @param msg The message of the channel read.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Message newMessage = (Message) msg;
        try {
            if (newMessage.getMethod() == Method.PUBLISH) {

                PublishMessage publishMessage = (PublishMessage) msg;

                // TODO: We'll log System.out and System.err in the future
                System.out.println("[Publish] Topic: " + publishMessage.getTopic() +
                        " Payload: " + publishMessage.getPayload());

                // Put the message to MessagePool
                MessagePool.putContentOnTopic(publishMessage.getTopic(), publishMessage.getPayload());

                // TODO: Not really sent back to subscriber currently
                SubscriberPool.sendToSubscribers(publishMessage);

                // Send PUBACK to the client
                // TODO: header definition & Encapsulation
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("PUBACK\r\nQoS:"+publishMessage.getQos()
                        +",Critical-Option:"+publishMessage.getCriticalOption()
                        +",Topic:"+publishMessage.getTopic()
                        +"\r\nPublish Successfully\r\n").getBytes())).sync();

            } else {
                throw new RuntimeException("Unknown message");
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
        // TODO: log this, instead of printStackTrace()
        cause.printStackTrace();
        ctx.close();
    }
}
