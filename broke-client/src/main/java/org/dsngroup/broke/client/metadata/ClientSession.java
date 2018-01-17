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

package org.dsngroup.broke.client.metadata;

import org.dsngroup.broke.client.storage.FakePublishMessageQueue;
import org.dsngroup.broke.client.storage.IPublishMessageQueue;
import org.dsngroup.broke.protocol.MqttMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client session.
 * Each client should have only one client session.
 * Maintain a reference of publish message queue for back-pressure status monitoring.
 * The operation of publish message queue is performed outside. (e.g. poll(), offer() at the user's coding space.)
 */
public class ClientSession {

    private final String clientId;

    @SuppressWarnings("Need to rediscuss this, for the scope of fake publish")
    private IPublishMessageQueue publishMessageQueue;

    // Unacked messages store: Key: packet idenfier, value: message
    private final Map<String, MqttMessage> unackedMessages;

    /**
     * Getter for back-pressure status. Determined by current publish message queue's status.
     */
    public boolean isBackPressured() {
        return publishMessageQueue.isBackPressured();
    }

    /**
     * Getter for the consumption rate of the publish message queue.
     * @return consumption rate.
     */
    public double getConsumptionRate() {
        return publishMessageQueue.getConsumptionRate();
    }

    /**
     * Getter for the capacity of publish message queue.
     * @return Capacity of the publish message queue.
     */
    public int getQueueCapacity() {
        return publishMessageQueue.getCapacity();
    }

    /**
     * Getter for the publish message queue.
     */
    public IPublishMessageQueue getPublishMessageQueue() {
        return publishMessageQueue;
    }

    /**
     * Setter for the publish message queue.
     * @param publishMessageQueue The publish message queue.
     */
    public void setPublishMessageQueue(IPublishMessageQueue publishMessageQueue) {
        this.publishMessageQueue = publishMessageQueue;
    }

    /**
     * Constructor of the client session.
     * The publish message is set to fake publish message queue for fake back-pressure monitoring by default.
     * @param clientId Client ID
     */
    public ClientSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
        this.publishMessageQueue = new FakePublishMessageQueue(10, 0.3, 0.7);
    }

}

