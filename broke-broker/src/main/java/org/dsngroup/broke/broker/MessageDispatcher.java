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

package org.dsngroup.broke.broker;

import org.dsngroup.broke.broker.storage.ServerSession;
import org.dsngroup.broke.broker.storage.ServerSessionPool;
import org.dsngroup.broke.broker.storage.Subscription;
import org.dsngroup.broke.broker.storage.SubscriptionPool;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class MessageDispatcher {

    private ServerSessionPool serverSessionPool;

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    /**
     * Publish to subscriptions
     * */
    public void publishToSubscription(MqttPublishMessage mqttPublishMessage) {
        // Iterate through all sessions, publishing to every sessions' subscriptions
        for (ServerSession serverSession: serverSessionPool.asCollection()) {
            // Only publish to active sessions
            if(serverSession.getIsActive()) {
                // Whether the mqttPublish Message matches the subscription is performed in "publishToSubscription"
                serverSession.publishToSubscription(mqttPublishMessage);
            }
        }
    }

    /**
     * Group-Based publish
     * 1. Get all the sessions with matched subscription(s)
     * 2. Group these sessions with group ID
     * 3. For each group, perform selection algorithm
     * 4. For each group, publish the message to the selected subscriber(session)
     * @param mqttPublishMessage Message to be published.
     * */
    public void groupBasedPublishToSubscription(MqttPublishMessage mqttPublishMessage) {
        String topic = mqttPublishMessage.variableHeader().topicName();
        TreeMap<Integer, ArrayList<ServerSession>> groupIdSessionListMap = new TreeMap<>();
        for(ServerSession serverSession: serverSessionPool.asCollection()) {
            if(serverSession.getIsActive()) {
                SubscriptionPool subscriptionPool = serverSession.getSubscriptionPool();
                Subscription matchedSubscription = subscriptionPool.getMatchSubscription(topic);
                if(matchedSubscription!=null) {
                    int groupId = matchedSubscription.getGroupId();
                    if(!groupIdSessionListMap.containsKey(groupId)) {
                        groupIdSessionListMap.put(groupId, new ArrayList<>());
                    }
                    groupIdSessionListMap.get(groupId).add(serverSession);
                }
            }
        }
        for(Map.Entry<Integer, ArrayList<ServerSession>> entry: groupIdSessionListMap.entrySet()) {
            int groupId = entry.getKey();
            ArrayList sessionList = entry.getValue();
            // Select over the session lists
            ServerSession selectedSession = selectSession(sessionList);
            if(selectedSession!=null) {
                // TODO: debug
                logger.info("Group ID: " + groupId + " Publish to " + selectedSession.getClientId());
                selectedSession.publishToSubscription(mqttPublishMessage);
            } else {
                logger.info("No available session now");
                // TODO: store the published message.
            }
        }
    }

    private ServerSession selectSession(ArrayList<ServerSession> sessionList) {
        double[] scoreArray = new double[sessionList.size()];
        for(int i=0; i<sessionList.size();i++) {
            scoreArray[i] = sessionList.get(i).getPublishScore();
        }
        double maxScore = 0;
        int maxScoreIdx = 0;
        for(int i=0; i<scoreArray.length; i++) {
            if(maxScore<scoreArray[i]){
                maxScore = scoreArray[i];
                maxScoreIdx = i;
            }
        }
        if(maxScore == 0) {
            return null;
        } else {
            return sessionList.get(maxScoreIdx);
        }
    }

    MessageDispatcher(ServerSessionPool serverSessionPool) {
        this.serverSessionPool = serverSessionPool;
    }
}
