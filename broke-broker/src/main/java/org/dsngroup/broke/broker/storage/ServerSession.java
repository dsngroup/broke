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

package org.dsngroup.broke.broker.storage;

import org.dsngroup.broke.protocol.MqttMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ServerSession {

    private boolean isActive;

    final String clientId;

    private SubscriptionPool subscriptionPool;

    // Unacked messages store: Key: packet idenfier, value: message
    final ConcurrentHashMap<String, MqttMessage> unackedMessages;

    private PingRequests pingRequests;

    private double currentRtt;

    /**
     * Getter for isActive
     * */
    public boolean getIsActive() {
        return isActive;
    }

    /**
     * Setter for isActive
     * */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Getter for client Id
     * */
    public String getClientId() {
        return clientId;
    }

    /**
     * Getter for subscription pool
     * */
    public SubscriptionPool getSubscriptionPool() {
        return subscriptionPool;
    }

    /**
     * Getter for current round-trip time
     * */
    public double getCurrentRtt() {
        return currentRtt;
    }

    public void setPingReq(int packetId) {
        pingRequests.setPingReq(packetId, System.nanoTime());
    }

    public void setPingResp(int packetId) {
        long sendTime = pingRequests.getPingReq(packetId);
        if (sendTime != -1) {
            currentRtt = (System.nanoTime() - sendTime)/1e6;
        }
    }

    ServerSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
        this.isActive = false;
        this.subscriptionPool = new SubscriptionPool();
        this.pingRequests = new PingRequests();
        this.currentRtt = 10000000;
    }
}