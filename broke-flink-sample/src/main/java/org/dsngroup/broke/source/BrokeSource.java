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

package org.dsngroup.broke.source;

import org.apache.flink.api.common.functions.StoppableFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.dsngroup.broke.client.BlockClient;
import org.dsngroup.broke.client.handler.callback.IMessageCallbackHandler;
import org.dsngroup.broke.client.storage.IPublishMessageQueue;
import org.dsngroup.broke.client.storage.PublishMessageQueue;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.dsngroup.broke.protocol.MqttQoS;
import org.dsngroup.broke.util.PublishMessageQueueMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

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

        @Override
        public void messageArrive(MqttPublishMessage mqttPublishMessage) {
            publishMessageQueue.putMessage(mqttPublishMessage.payload().toString(StandardCharsets.UTF_8));
        }

        @Override
        public void connectionLost(Throwable cause) {
            logger.error("Connection lost: " + cause.getMessage());
            System.exit(1);
        }

        BrokeCallBack() {}
    }

    @Override
    public void run(final SourceContext<String> ctx) {
        try {
            blockClient = new BlockClient(serverAddress, serverPort);
            blockClient.connect(MqttQoS.AT_LEAST_ONCE, 0);
            blockClient.setMessageCallbackHandler(new BrokeCallBack());
            blockClient.setPublishMessageQueue(publishMessageQueue =
                    new PublishMessageQueue(1000, 0.3, 0.7));
            blockClient.subscribe(subscribeTopic, MqttQoS.AT_LEAST_ONCE, 0, groupId);

            this.isRunning = true;

            this.monitorThread = new Thread(new PublishMessageQueueMonitor(publishMessageQueue));
            this.monitorThread.start();

            while (this.isRunning) {
                String message = publishMessageQueue.getMessage();
                if (message == null) {
                    Thread.sleep(100);
                } else {
                    ctx.collect(message);
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

    public void close() {
        if (blockClient != null) {
            blockClient.disconnect();
        }
        this.isRunning = false;

    }

    public BrokeSource(String serverAddress, int serverPort, String subscribeTopic, int groupId) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.subscribeTopic = subscribeTopic;
        this.groupId = groupId;
    }
}
