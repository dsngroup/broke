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

    // Unacked messages store: Key: packet idenfier, value: message
    private final Map<String, MqttMessage> unackedMessages;

    public ClientSession(String clientId) {
        this.clientId = clientId;
        this.unackedMessages = new ConcurrentHashMap<>();
    }

}
