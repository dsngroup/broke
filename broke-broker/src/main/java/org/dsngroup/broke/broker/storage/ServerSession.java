package org.dsngroup.broke.broker.storage;

import org.dsngroup.broke.protocol.MqttMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ServerSession {

    public boolean isActive;

    final String clientId;

    // Unacked messages store: Key: packet idenfier, value: message
    final ConcurrentHashMap<String, MqttMessage> unackedMessages;

    ServerSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
        this.isActive = false;
    }

}