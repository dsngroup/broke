package org.dsngroup.broke.broker.storage;

import io.netty.handler.codec.mqtt.MqttMessage;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The singleton class ServerSessionCollection
 * Contain all the Server Sessions for all clients
 * */
public class ServerSessionCollection {

    // Use volatile to guarantee all session user threads gets the well initialized instance.
    private volatile static ServerSessionCollection serverSessionCollection;

    private HashMap<String, ServerSession> serverSessionCollectionMap;

    /**
     * Getter method of the server session collection
     * */
    public static ServerSessionCollection getServerSessionCollection() {
        if(serverSessionCollection==null) {
            synchronized (ServerSessionCollection.class) {
                if(serverSessionCollection==null) {
                    serverSessionCollection = new ServerSessionCollection();
                }
            }
        }
        return serverSessionCollection;
    }

    /**
     * Judge whether a session in now in use or not.
     * */
    public boolean isSessionActive(String clientId) {
        if (!serverSessionCollectionMap.containsKey(clientId))
            return false;
        else {
            return serverSessionCollectionMap.get(clientId).isActive;
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
            if(serverSessionCollectionMap.containsKey(clientId))
                serverSessionCollectionMap.remove(clientId);
            serverSessionCollectionMap.put(clientId, new ServerSession(clientId));
        } else {
            if(!serverSessionCollectionMap.containsKey(clientId))
                serverSessionCollectionMap.put(clientId, new ServerSession(clientId));
        }
        return serverSessionCollectionMap.get(clientId);

    }

    /**
     * Private constructor for ServerSessionCollection
     * */
    private ServerSessionCollection() {
        serverSessionCollectionMap = new HashMap();
    }

}
