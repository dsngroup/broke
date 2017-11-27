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

package org.dsngroup.broke.client.channel.storage;

import org.dsngroup.broke.protocol.MqttMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client session
 * Each client should have only one client session
 * */
public class ClientSession {

    private final String clientId;

    private FakePublishMessageQueue fakePublishMessageQueue;

    // Unacked messages store: Key: packet idenfier, value: message
    private final Map<String, MqttMessage> unackedMessages;

    public boolean isBackPressured() {
        return fakePublishMessageQueue.isBackPressured();
    }

    public FakePublishMessageQueue getFakePublishMessageQueue() {
        return fakePublishMessageQueue;
    }

    public ClientSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
        this.fakePublishMessageQueue = new FakePublishMessageQueue(10, 0.3, 0.8);
    }

}

