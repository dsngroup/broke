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

package org.dsngroup.broke;

import io.netty.buffer.ByteBuf;
import org.dsngroup.broke.client.BlockClient;
import org.dsngroup.broke.client.handler.callback.IMessageCallbackHandler;
import org.dsngroup.broke.protocol.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Create multiple Broke clients.
 */
public class MultiClient {

    private static final Logger logger = LoggerFactory.getLogger(MultiClient.class);

    /**
     * Main function.
     */
    public static void main(String[] args) {
        try {
            String serverAddress = args[0];
            int serverPort = Integer.parseInt(args[1]);

            /*
            * 3 Flink consumers: 15 clients
            * Stable: 8 clients
            * LSMD stable: 12 clients 2ms
            * LSMD small throughput: 6 clients 2ms
            */
            int numOfClients = 6;

            PublishClient[] publishClients = new PublishClient[numOfClients];
            SubscribeClient[] subscribeClients = new SubscribeClient[numOfClients];

            String topic = "Foo";
            int sleepInterval = 2; // publish sleep interval
            int groupId = 1; // Subscribe group ID

            Path path = Paths.get(MultiClient.class.getClassLoader().getResource("payload.txt").getFile());
            String payload = null;
            try {
                byte[] payloadArray = Files.readAllBytes(path);
                payload = new String(payloadArray);
            } catch (IOException e) {
                logger.error("Payload reading failed: " + e.getMessage());
            }
            // Initialize subscribe clients
            // for (int i = 0; i < subscribeClients.length; i++) {
            //     subscribeClients[i] = new SubscribeClient(serverAddress, serverPort, topic, groupId);
            //     subscribeClients[i].start();
            // }

            // Initialize publish clients
            for (int i = 0; i < publishClients.length; i++) {
                publishClients[i] =
                        new PublishClient(serverAddress,
                                serverPort,
                                topic,
                                sleepInterval,
                                "client_" + i,
                                payload);
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

        private String payload;

        class MessageCallbackHandler implements IMessageCallbackHandler {

            @Override
            public void messageArrive(ByteBuf payload) {}

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
                payload = "During the 2012–13 season, Curry set the NBA record for three-pointers made in a "
                        + "regular season with 272. He surpassed that record in 2015 with 286, and again in 2016 with "
                        + "402. During the 2013–14 season, he and teammate Klay Thompson were nicknamed the Splash "
                        + "Brothers en route to setting the NBA record for combined three-pointers in a season with "
                        + "484, a record they broke the following season (525) and again in the 2015–16 season (678).";
                while (true) {
                    blockClient.publish(topic, MqttQoS.AT_LEAST_ONCE, 0, payload);
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

        public PublishClient(String serverAddress,
                             int serverPort,
                             String topic,
                             int sleepInterval,
                             String clientId,
                             String payload) {
            try {
                this.blockClient = new BlockClient(serverAddress, serverPort, clientId);
                this.topic = topic;
                this.sleepInterval = sleepInterval;
                this.payload = payload;
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
            public void messageArrive(ByteBuf payload) {
                logger.info(payload.toString(StandardCharsets.UTF_8));
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