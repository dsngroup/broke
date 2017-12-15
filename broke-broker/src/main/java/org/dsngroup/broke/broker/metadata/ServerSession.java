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

public class ServerSession {

    private boolean isActive;

    private final String clientId;

    private SubscriptionPool subscriptionPool;

    /**
     * An incremental packet ID.
     * */
    private PacketIdGenerator packetIdGenerator;

    // Unacked messages store: Key: packet idenfier, value: message
    private final ConcurrentHashMap<String, MqttMessage> unackedMessages;

    private ClientProber clientProber;

    /**
     * Getter for isActive.
     * */
    public boolean getIsActive() {
        return isActive;
    }

    /**
     * Setter for isActive.
     * */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Getter for client Id.
     * */
    public String getClientId() {
        return clientId;
    }


    /**
     * Getter for subscription pool.
     * */
    public SubscriptionPool getSubscriptionPool() {
        return subscriptionPool;
    }

    /**
     * Getter for the next valid publish packet Id.
     * */
    public int getNextPacketId() {
        return packetIdGenerator.getPacketId();
    }

    /**
     * Setter for client prober.
     * */
    public void setClientProber(ClientProber clientProber) {
        this.clientProber = clientProber;
    }

    /**
     * Getter for the current publish score.
     * */
    public double getPublishScore() {
        if (clientProber.isBackPressured()) {
            return 0;
        } else {
            return 1 / clientProber.getRttAvg();
        }
    }

    ServerSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
        this.isActive = false;
        this.subscriptionPool = new SubscriptionPool();
        this.packetIdGenerator = new PacketIdGenerator();
    }
}
