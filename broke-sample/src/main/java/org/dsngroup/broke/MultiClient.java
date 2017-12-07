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

package org.dsngroup.broke;

import org.dsngroup.broke.client.BlockClient;
import org.dsngroup.broke.client.handler.callback.IMessageCallbackHandler;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.dsngroup.broke.protocol.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class MultiClient {

    private static final Logger logger = LoggerFactory.getLogger(MultiClient.class);

    public static void main(String args[]) {

        try {
            String serverAddress = args[0];
            int serverPort = Integer.parseInt(args[1]);

            int numOfClients = 100;

            PublishClient[] publishClients = new PublishClient[numOfClients];
            SubscribeClient[] subscribeClients = new SubscribeClient[numOfClients];

            String topic = "Foo";
            int sleepInterval = 500; // publish sleep interval
            int groupId = 1; // Subscribe group ID

            // Initialize subscribe clients
            for (int i = 0; i < subscribeClients.length; i++) {
                subscribeClients[i] = new SubscribeClient(serverAddress, serverPort, topic, groupId);
                subscribeClients[i].start();
            }

            // Initialize publish clients
            for (int i = 0; i < publishClients.length; i++) {
                publishClients[i] = new PublishClient(serverAddress, serverPort, topic, sleepInterval);
                publishClients[i].start();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Not enough argument");
            System.exit(1);
        }
    }

    private static class PublishClient extends Thread {

        private BlockClient blockClient;

        private static final Logger logger = LoggerFactory.getLogger(PublishClient.class);

        private String topic;

        private int sleepInterval;

        class MessageCallbackHandler implements IMessageCallbackHandler {

            @Override
            public void messageArrive(MqttPublishMessage mqttPublishMessage) {}

            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Connection lost: " + cause.getMessage() + " Client ID: " + blockClient.getClientId());
                Thread.interrupted();
            }

        }

        @Override
        public void run() {
            try {
                blockClient.connect(MqttQoS.AT_LEAST_ONCE, 0);
                blockClient.setMessageCallbackHandler(new MessageCallbackHandler());
                while (true) {
                    blockClient.publish(topic, MqttQoS.AT_LEAST_ONCE, 0, "Foo");
                    Thread.sleep(sleepInterval);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                return;
            }

        }

        public PublishClient(String serverAddress, int serverPort, String topic, int sleepInterval) {
            try {
                this.blockClient = new BlockClient(serverAddress, serverPort);
                this.topic = topic;
                this.sleepInterval = sleepInterval;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return;
            }

        }
    }

    private static class SubscribeClient extends Thread {

        private BlockClient blockClient;

        private static final Logger logger = LoggerFactory.getLogger(SubscribeClient.class);

        private String topicFilter;

        private int groupId;

        class MessageCallbackHandler implements IMessageCallbackHandler {

            @Override
            public void messageArrive(MqttPublishMessage mqttPublishMessage) {
                logger.info(mqttPublishMessage.payload().toString(StandardCharsets.UTF_8));
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Connection lost: " + cause.getMessage() + " Client ID: " + blockClient.getClientId());
                Thread.interrupted();
            }

        }

        @Override
        public void run() {
            try {
                blockClient.connect(MqttQoS.AT_LEAST_ONCE, 0);
                blockClient.setMessageCallbackHandler(new MessageCallbackHandler());
                blockClient.subscribe(topicFilter, MqttQoS.AT_LEAST_ONCE, 0, groupId);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return;
            }

        }

        public SubscribeClient(String serverAddress, int serverPort, String topicFilter, int groupId) {

            try {
                this.blockClient = new BlockClient(serverAddress, serverPort);
                this.topicFilter = topicFilter;
                this.groupId = groupId;
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("Not enough arguments");
                System.exit(1);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return;
            }

        }
    }

}