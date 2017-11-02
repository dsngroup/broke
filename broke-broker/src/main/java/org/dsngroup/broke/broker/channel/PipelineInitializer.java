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

package org.dsngroup.broke.broker.channel;

import io.netty.channel.ChannelInitializer;

import io.netty.channel.Channel;
import org.dsngroup.broke.broker.channel.handler.ConnectHandler;
import org.dsngroup.broke.broker.channel.handler.MessageParseHandler;

/**
 * The PipelineInitializer is a customized ChannelInitializer for desired pipeline of handlers.
 * <code>
 *     new PipelineInitializer()
 * </code>
 */
public class PipelineInitializer extends ChannelInitializer<Channel> {

    // TODO: register the handler topologies here.

    /**
     * Implement the channel, for the pipeline of handler.
     * @param channel The Netty {@see Channel}
     * @throws Exception
     */
    @Override
    public void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast("MessageParseHandler", new MessageParseHandler());
        channel.pipeline().addLast("ConnectHandler", new ConnectHandler());
    }
}
