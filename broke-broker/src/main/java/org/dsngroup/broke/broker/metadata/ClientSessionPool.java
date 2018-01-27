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

package org.dsngroup.broke.broker.metadata;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contain all the Server Sessions for all clients.
 */
public class ClientSessionPool {

    private ConcurrentHashMap<String, ClientSession> serverSessionPoolMap;

    public Collection<ClientSession> asCollection() {
        return serverSessionPoolMap.values();
    }

    /**
     * Judge whether a session with specified clientId is in use.
     */
    public boolean isSessionActive(String clientId) {
        if (!serverSessionPoolMap.containsKey(clientId)) {
            return false;
        } else {
            return serverSessionPoolMap.get(clientId).getIsActive();
        }
    }

    /**
     * Function for clients to get the session upon CONNECT
     * If the cleanSession flag is false, use existed session. If not existed, create a new one.
     * If the cleanSession flag is true, replace the old session with a new one.
     * See "Clean Session" flag in Mqtt Specification
     * @param clientId client ID
     * @param cleanSession Clean Session Flag
     */
    public ClientSession getSession(String clientId, boolean cleanSession) {

        if (cleanSession) {
            // TODO: is the synchronized necessary?
            synchronized (this) {
                if (serverSessionPoolMap.containsKey(clientId)) {
                    serverSessionPoolMap.remove(clientId);
                }
                serverSessionPoolMap.put(clientId, new ClientSession(clientId));
            }
        } else {
            // TODO: is the synchronized necessary?
            synchronized (this) {
                if (!serverSessionPoolMap.containsKey(clientId)) {
                    serverSessionPoolMap.put(clientId, new ClientSession(clientId));
                }
            }
        }
        return serverSessionPoolMap.get(clientId);

    }

    /**
     * Constructor for ClientSessionPool.
     */
    public ClientSessionPool() {
        serverSessionPoolMap = new ConcurrentHashMap<>();
    }

}
