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

import org.dsngroup.broke.client.metadata.ClientSession;
import org.dsngroup.broke.client.storage.FakePublishMessageQueue;
import org.dsngroup.broke.client.storage.PublishMessageQueue;

/**
 * The context that contains client's information.
 * TODO: May be better to integrate with client session.
 * */
public class ClientContext {

    private ClientSession clientSession;

    private String clientId;

    /**
     * Getter for the client session.
     * */
    public ClientSession getClientSession() {
        return clientSession;
    }

    /**
     * Getter for the client ID.
     * */
    public String getClientId() {
        return clientId;
    }

    /**
     * Constructor of the client context.
     * @param clientId Client ID.
     * */
    public ClientContext(String clientId) {
        this.clientId = clientId;
        clientSession = new ClientSession(clientId);
    }
}
