package org.dsngroup.broke.client.channel.storage;

import io.netty.handler.codec.mqtt.MqttMessage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Client session
 * Each client should have only one client session
 * */
public class ClientSession {

    private volatile static ClientSession clientSession;

    private final String clientId;

    // Unacked messages store: Key: packet idenfier, value: message
    private final ConcurrentHashMap<String, MqttMessage> unackedMessages;

    private ClientSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
    }

    public static ClientSession getClientSession(String clientId) {
        if(clientSession==null) {
            synchronized (ClientSession.class) {
                if (clientSession==null)
                    clientSession = new ClientSession(clientId);
            }
        } else {
            if(!clientSession.clientId.equals(clientId))
                throw new RuntimeException("Invalid client identifier");
        }
        return clientSession;
    }

}
