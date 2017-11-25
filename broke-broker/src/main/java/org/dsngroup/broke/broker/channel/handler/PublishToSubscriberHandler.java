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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.netty.util.ReferenceCountUtil;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.broker.storage.MessagePool;
import org.dsngroup.broke.protocol.MqttMessage;
import org.dsngroup.broke.protocol.MqttMessageType;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishToSubscriberHandler extends ChannelInboundHandlerAdapter {

    private ServerContext serverContext;

    private static final Logger logger = LoggerFactory.getLogger(PublishToSubscriberHandler.class);

    /**
     * Read the message from channel and publish to {@link MessagePool}
     * @param ctx {@see ChannelHandlerContext}
     * @param msg The message of the channel read.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {
            MqttMessage mqttMessage = (MqttMessage) msg;
            if (mqttMessage.fixedHeader().messageType() == MqttMessageType.PUBLISH) {
                MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) msg;
                publishToSubscriptions(serverContext, mqttPublishMessage);
            } else {
                // TODO: necessary exception handling or logic
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(e.getStackTrace().toString());
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    /**
     * Publish the message to all sessions
     * @param serverContext server context
     * @param mqttPublishMessage The PUBLISH message from the publisher
     * */
    private void publishToSubscriptions(ServerContext serverContext,
                                        MqttPublishMessage mqttPublishMessage) {
        // serverContext.publishToSubscription(mqttPublishMessage);
        serverContext.groupBasedPublishToSubscription(mqttPublishMessage);
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

    /**
     * The constructor
     * @param serverContext The global server context.
     * */
    public PublishToSubscriberHandler(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

}
