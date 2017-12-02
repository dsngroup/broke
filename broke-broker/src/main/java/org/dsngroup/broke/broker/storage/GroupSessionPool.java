package org.dsngroup.broke.broker.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupSessionPool {

    private Map<Integer, Map<String, Integer>> messageIndex;

    public synchronized Integer getIdx(int groupId, String topic) {
        if(!messageIndex.containsKey(groupId)) {
            messageIndex.put(groupId, new ConcurrentHashMap<>());
            messageIndex.get(groupId).put(topic, 0);
        } else if (!messageIndex.get(groupId).containsKey(topic)) {
            messageIndex.get(groupId).put(topic, 0);
        }
        return messageIndex.get(groupId).get(topic);
    }

    public synchronized void setIdx(int groupId, String topic, int newIndex) {
        messageIndex.get(groupId).put(topic, newIndex);
    }

    public GroupSessionPool() {
        messageIndex = new ConcurrentHashMap<>();
    }
}

