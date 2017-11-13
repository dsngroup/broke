package org.dsngroup.broke.broker.storage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Contain all the Server Sessions for all clients
 * */
public class ServerSessionPool {

    private ConcurrentHashMap<String, ServerSession> serverSessionPoolMap;

    /**
     * Judge whether a session in now in use or not.
     * */
    public boolean isSessionActive(String clientId) {
        if (!serverSessionPoolMap.containsKey(clientId))
            return false;
        else {
            return serverSessionPoolMap.get(clientId).isActive;
        }
    }

    /**
     * Function for clients to get the session upon CONNECT
     * If the cleanSession flag is false, use existed session. If not existed, create a new one.
     * If the cleanSession flag is true, replace the old session with a new one.
     * See "Clean Session" flag in Mqtt Specification
     * @param clientId client ID
     * @param cleanSession Clean Session Flag
     * */
    public ServerSession getSession(String clientId, boolean cleanSession) {

        if (cleanSession) {
            if(serverSessionPoolMap.containsKey(clientId))
                serverSessionPoolMap.remove(clientId);
            serverSessionPoolMap.put(clientId, new ServerSession(clientId));
        } else {
            if(!serverSessionPoolMap.containsKey(clientId))
                serverSessionPoolMap.put(clientId, new ServerSession(clientId));
        }
        return serverSessionPoolMap.get(clientId);

    }

    /**
     * Constructor for ServerSessionPool
     * */
    public ServerSessionPool() {
        serverSessionPoolMap = new ConcurrentHashMap<>();
    }

}
