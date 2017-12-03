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

package org.dsngroup.broke.broker;

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.dsngroup.broke.broker.handler.MqttMessageHandler;
import org.dsngroup.broke.protocol.MqttDecoder;
import org.dsngroup.broke.protocol.MqttEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Server class used as an entry instance.
 * An example creation,
 * <code>
 *     Server srv = new Server(port, ctx).run();
 * </code>
 */
public class Server {

    private int port;

    private final ServerContext serverContext;

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * The PipelineInitializer is a customized ChannelInitializer for desired pipeline of handlers.
     * <code>
     *     new PipelineInitializer()
     * </code>
     */
    class PipelineInitializer extends ChannelInitializer<Channel> {

        private ServerContext serverContext;

        /**
         * Implement the channel, for the pipeline of handler.
         * @param channel The Netty {@see Channel}
         */
        @Override
        public void initChannel(Channel channel) throws Exception {
            channel.pipeline().addLast("MqttEncoder", MqttEncoder.INSTANCE);
            channel.pipeline().addLast("MqttDecoder", new MqttDecoder());
            channel.pipeline().addLast("MqttMessageHandler", new MqttMessageHandler(this.serverContext));
        }

        PipelineInitializer(ServerContext serverContext) {
            this.serverContext = serverContext;
        }
    }

    /**
     * The Server constructor construct a basic information of a Server.
     * @param port the binding port.
     * @param serverContext the {@see SeverContext} instance for associated information.
     */
    public Server(int port, ServerContext serverContext) {
        this.port = port;
        this.serverContext = serverContext;
    }

    /**
     * Run the server.
     * @throws Exception connection error
     */
    public void run() throws Exception {
        // TODO: May move these codebase into another class for scalability.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap boots = new ServerBootstrap();

            boots.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new PipelineInitializer(serverContext))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture after = boots.bind(port).sync();
            after.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    // TODO: close the server channel gracefully.
    // TODO: 1. Close all children channels. 2. close server channel.
    public void close() {}

    public static void main(String[] args) throws Exception {
        logger.info("Server is running at 0.0.0.0:8181");
        // TODO: Blocking currently.
        // TODO: Prefer to have a return binding for server as an interaction.
        new Server(8181, new ServerContext()).run();
    }
}
