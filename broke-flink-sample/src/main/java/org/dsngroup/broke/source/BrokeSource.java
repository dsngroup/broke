/*
 * Copyright (c) 2017-2018 Dependable Network and System Lab, National Taiwan University.
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

package org.dsngroup.broke.source;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.apache.flink.api.common.functions.StoppableFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.dsngroup.broke.client.BlockClient;
import org.dsngroup.broke.client.handler.callback.IMessageCallbackHandler;
import org.dsngroup.broke.client.storage.IPublishMessageQueue;
import org.dsngroup.broke.client.storage.PublishMessageQueue;
import org.dsngroup.broke.protocol.MqttQoS;
import org.dsngroup.broke.util.PublishMessageQueueMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Source of the latency-sensitive broker for Flink.
 */
public class BrokeSource implements SourceFunction<String>, StoppableFunction {

    private static final Logger logger = LoggerFactory.getLogger(BrokeSource.class);

    private String serverAddress;

    private int serverPort;

    private String subscribeTopic;

    private int groupId;

    BlockClient blockClient = null;

    private boolean isRunning;

    private Thread monitorThread;

    private IPublishMessageQueue publishMessageQueue;

    class BrokeCallBack implements IMessageCallbackHandler {

        private int messageCounter;

        @Override
        public void messageArrive(ByteBuf payload) {
            publishMessageQueue.putMessage(payload);
            messageCounter++;
        }

        @Override
        public void connectionLost(Throwable cause) {
            logger.error("Connection lost: " + cause.getMessage());
            System.exit(1);
        }

        BrokeCallBack() {
            this.messageCounter = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        // TODO: debug
                        logger.info("Message per second: " + messageCounter + " Time: " + System.currentTimeMillis());
                        messageCounter = 0;
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
            }).start();

        }
    }

    @Override
    public void run(final SourceContext<String> ctx) {
        try {
            blockClient = new BlockClient(serverAddress, serverPort);
            blockClient.connect(MqttQoS.AT_LEAST_ONCE, 0);
            blockClient.setMessageCallbackHandler(new BrokeCallBack());
            blockClient.setPublishMessageQueue(publishMessageQueue =
                    new PublishMessageQueue(30000, 0.5, 0.8));
            blockClient.subscribe(subscribeTopic, MqttQoS.AT_LEAST_ONCE, 0, groupId);

            this.isRunning = true;

            this.monitorThread = new Thread(new PublishMessageQueueMonitor(publishMessageQueue));
            this.monitorThread.start();

            while (this.isRunning) {
                ByteBuf message = publishMessageQueue.getMessage();
                try {
                    if (message == null) {
                        Thread.sleep(100);
                    } else {
                        ctx.collect(message.toString(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    logger.error("Flink ctx.collect failed: " + e.getMessage());
                    System.exit(1);
                } finally {
                    ReferenceCountUtil.release(message);
                }
            }

        } catch (Exception e) {
            // TODO e.getMessage();
            logger.error(e.getStackTrace().toString());
        }

    }

    @Override
    public void stop() {
        close();
    }

    @Override
    public void cancel() {
        close();
    }

    /**
     * Close the source.
     */
    public void close() {
        if (blockClient != null) {
            blockClient.disconnect();
        }
        this.isRunning = false;
        // TODO: debug
        logger.info("Source closed");
        System.exit(1);
    }

    /**
     * Constructor
     * @param serverAddress Server address.
     * @param serverPort Server port
     * @param subscribeTopic Topic to subscribe.
     * @param groupId The group ID this subscriber belongs to.
     */
    public BrokeSource(String serverAddress, int serverPort, String subscribeTopic, int groupId) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.subscribeTopic = subscribeTopic;
        this.groupId = groupId;
    }
}
