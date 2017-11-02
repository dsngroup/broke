package org.dsngroup.broke.client.channel.handler;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.dsngroup.broke.protocol.Message;
import org.dsngroup.broke.protocol.Method;

public class AckHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Message newMessage = (Message) msg;
        try {
            if (newMessage.getMethod() == Method.CONNACK) {
                // TODO: log this
                System.out.println("[Client] CONNACK from server: "+newMessage.getPayload());
            } else if (newMessage.getMethod() == Method.PUBACK ) {
                // TODO: log this
                System.out.println("[Client] PUBACK from server: "+newMessage.getPayload());
            } else if (newMessage.getMethod() == Method.SUBACK ) {
                // TODO: log this
                System.out.println("[Client] SUBACK from server: "+newMessage.getPayload());
            } else {
                throw new RuntimeException("[Client] Connect handler: unhandled message");
            }
        } finally {
            // The msg object is an reference counting object.
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause) {
        // TODO: use log instead of printStackTrace()
        cause.printStackTrace();
        ctx.close();
    }

}
