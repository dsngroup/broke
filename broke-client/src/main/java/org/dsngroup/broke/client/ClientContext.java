package org.dsngroup.broke.client;

import org.dsngroup.broke.client.channel.storage.ClientSession;

public class ClientContext {

    private ClientSession clientSession;

    public ClientContext(String clientId) {
        clientSession = new ClientSession(clientId);
    }
}
