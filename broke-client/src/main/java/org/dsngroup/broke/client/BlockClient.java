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

package org.dsngroup.broke.client;

import org.dsngroup.broke.protocol.Message;

/**
 * The BlockClient should be deprecated after the asynchronous client is implemented.
 * Only used for test!
 */
public class BlockClient {

    private String targetBrokerAddress;

    private int targetBrokerPort;

    public void publish(String topic, int qos, int criticalOption, String payload) throws Exception {
        Message msg = new Message(topic, qos, criticalOption, payload);
        // TODO: Send this message
        System.out.println("Publish to topic: " + msg.getTopic() + "\nPayload: " + msg.getPayload());
    }

    public BlockClient(String targetBrokerAddress, int targetBrokerPort) {
        this.targetBrokerAddress = targetBrokerAddress;
        this.targetBrokerPort = targetBrokerPort;
        // TODO: Connect to the Broker Server, a.k.a. Send CONNECTION message
    }
}
