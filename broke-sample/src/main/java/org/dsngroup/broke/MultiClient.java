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
import org.dsngroup.broke.protocol.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiClient {
    public static void main(String args[]) {

        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        int numOfClients = 100;

        PublishClient[] publishClients = new PublishClient[numOfClients];
        SubscribeClient[] subscribeClients = new SubscribeClient[numOfClients];

        String topic = "Foo";
        int sleepInterval = 500; // publish sleep interval
        int groupId = 1; // Subscribe group ID

        // Initialize subscribe clients
        for(int i=0; i<subscribeClients.length; i++) {
            subscribeClients[i] = new SubscribeClient(serverAddress, serverPort, topic, groupId);
            subscribeClients[i].start();
        }

        // Initialize publish clients
        for(int i=0; i<publishClients.length; i++) {
            publishClients[i] = new PublishClient(serverAddress, serverPort, topic, sleepInterval);
            publishClients[i].start();
        }
    }
}

class PublishClient extends Thread {

    private BlockClient blockClient;

    private static final Logger logger = LoggerFactory.getLogger(PublishClient.class);

    private String topic;

    private int sleepInterval;

    @Override
    public void run() {
        try {
            blockClient.connect(1, 0);
            while(true) {
                blockClient.publish(topic, MqttQoS.AT_LEAST_ONCE, 0, "Foo");
                Thread.sleep(sleepInterval);
            }
        } catch(Exception e) {
            logger.error(e.getMessage());
            logger.error(e.getStackTrace().toString());
        }

    }

    public PublishClient(String serverAddress, int serverPort, String topic, int sleepInterval) {
        try {
            this.blockClient = new BlockClient(serverAddress, serverPort);
            this.topic = topic;
            this.sleepInterval = sleepInterval;
        } catch (Exception e){
            logger.error(e.getMessage());
            logger.error(e.getStackTrace().toString());
        }

    }
}

class SubscribeClient extends Thread {

    private BlockClient blockClient;

    private static final Logger logger = LoggerFactory.getLogger(SubscribeClient.class);

    private String topicFilter;

    private int groupId;

    @Override
    public void run() {
        try {
            blockClient.connect(1, 0);
            blockClient.subscribe(topicFilter, MqttQoS.AT_LEAST_ONCE, 0, groupId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(e.getStackTrace().toString());
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
            System.exit(1);
        }

    }
}