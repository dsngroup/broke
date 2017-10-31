package org.dsngroup.broke.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

public class PublishHandler extends ChannelInboundHandlerAdapter {

    // TODO: handle PUBACK message
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final ByteBuf msg = ctx.alloc().buffer(4);
        final ChannelFuture f = ctx.writeAndFlush( msg );
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                assert f == future;
                ctx.close();
            }
        });
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause) {
        // TODO: use log instead of printStackTrace()
        cause.printStackTrace();
        ctx.close();
    }

}
