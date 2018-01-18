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

package org.dsngroup.broke.broker.metadata;

import org.dsngroup.broke.broker.dispatch.ClientProber;
import org.dsngroup.broke.broker.util.PacketIdGenerator;
import org.dsngroup.broke.protocol.*;

import java.util.concurrent.ConcurrentHashMap;

public class ClientSession {

    private boolean isActive;

    private final String clientId;

    private SubscriptionPool subscriptionPool;

    private SessionStatistics sessionStatistics = new SessionStatistics();

    private class SessionStatistics {
        public long publishCount;
        SessionStatistics() {
            this.publishCount = 0;
        }
    }

    public void setPublishCount() {
        sessionStatistics.publishCount ++;
    }

    public long getPublishCount() {
        return sessionStatistics.publishCount;
    }

    /**
     * An incremental packet ID.
     */
    private PacketIdGenerator packetIdGenerator;

    // Unacked messages store: Key: packet idenfier, value: message
    private final ConcurrentHashMap<String, MqttMessage> unackedMessages;

    private ClientProber clientProber;

    /**
     * Getter for isActive.
     */
    public boolean getIsActive() {
        return isActive;
    }

    /**
     * Setter for isActive.
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Getter for client Id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Getter for subscription pool.
     * @return Subscription Pool
     */
    public SubscriptionPool getSubscriptionPool() {
        return subscriptionPool;
    }

    /**
     * Getter for the next valid publish packet Id.
     * @return Next valid packet ID.
     */
    public int getNextPacketId() {
        return packetIdGenerator.getPacketId();
    }

    /**
     * Getter for the average RTT of this client.
     * @return Average RTT.
     */
    public double getAverageRTT() {
        return clientProber.getRttAvg();
    }

    /**
     * Setter for client prober.
     */
    public void setClientProber(ClientProber clientProber) {
        this.clientProber = clientProber;
    }

    /**
     * Getter for the current publish score.
     */
    public double getPublishScore() {
        double bpFactor = clientProber.isBackPressured() ? 0.1 : 1.0;
        double estimatedNetworkDelay = clientProber.getRttAvg();
        double estimatedQueuingDelay = clientProber.getEstimatedQueuingDelay();

        double networkDelayFactor;
        if (estimatedNetworkDelay < 10) {
            networkDelayFactor = 100;
        } else if (estimatedNetworkDelay < 30) {
            networkDelayFactor = 50;
        } else if (estimatedNetworkDelay < 70) {
            networkDelayFactor = 25;
        } else if (estimatedNetworkDelay < 120) {
            networkDelayFactor = 12;
        } else {
            networkDelayFactor = 1;
        }
        return networkDelayFactor * bpFactor;
    }

    /**
     * Getter for the current publish score String for Debug.
     * TODO: remove this
     */
    public String getPublishScoreString() {
        double bpFactor = clientProber.isBackPressured() ? 0.1 : 1.0;
        double estimatedNetworkDelay = clientProber.getRttAvg();
        double estimatedQueuingDelay = clientProber.getEstimatedQueuingDelay();

        double delayFactor;
        if (estimatedNetworkDelay < 10) {
            delayFactor = 100;
        } else if (estimatedNetworkDelay < 30) {
            delayFactor = 70;
        } else if (estimatedNetworkDelay < 70) {
            delayFactor = 50;
        } else if (estimatedNetworkDelay < 120) {
            delayFactor = 30;
        } else {
            delayFactor = 1;
        }
        return delayFactor + " * " + bpFactor + " Estimated delay: " + estimatedNetworkDelay;
    }

    ClientSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
        this.isActive = false;
        this.subscriptionPool = new SubscriptionPool();
        this.packetIdGenerator = new PacketIdGenerator();
    }
}
