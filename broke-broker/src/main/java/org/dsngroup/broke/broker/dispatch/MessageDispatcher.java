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

package org.dsngroup.broke.broker.dispatch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.dsngroup.broke.broker.metadata.ServerSession;
import org.dsngroup.broke.broker.metadata.ServerSessionPool;
import org.dsngroup.broke.broker.metadata.Subscription;
import org.dsngroup.broke.broker.metadata.SubscriptionPool;
import org.dsngroup.broke.protocol.MqttFixedHeader;
import org.dsngroup.broke.protocol.MqttMessageType;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.dsngroup.broke.protocol.MqttPublishVariableHeader;
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
                publishToSubscription(serverSession, mqttPublishMessage);
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
        TreeMap<Integer, ArrayList<ServerSession>> groupIdSessionListMap = getGroupedServerSessions(topic);
        for(Map.Entry<Integer, ArrayList<ServerSession>> entry: groupIdSessionListMap.entrySet()) {
            int groupId = entry.getKey();
            ArrayList sessionList = entry.getValue();
            // Select over the session lists
            ServerSession selectedSession = selectSession(sessionList);
            if(selectedSession != null) {
                // TODO: debug
                logger.info("Group ID: " + groupId + " Publish to " + selectedSession.getClientId());
                publishToSubscription(selectedSession, mqttPublishMessage);
            } else {
                logger.info("No available session now");
                // TODO: store the published message.
            }
        }
    }

    private TreeMap<Integer, ArrayList<ServerSession>> getGroupedServerSessions(String topic) {
        TreeMap<Integer, ArrayList<ServerSession>> groupedServerSessions = new TreeMap<>();
        for(ServerSession serverSession: serverSessionPool.asCollection()) {
            if(serverSession.getIsActive()) {
                SubscriptionPool subscriptionPool = serverSession.getSubscriptionPool();
                Subscription matchedSubscription = subscriptionPool.getMatchSubscription(topic);
                if(matchedSubscription!=null) {
                    int groupId = matchedSubscription.getGroupId();
                    if(!groupedServerSessions.containsKey(groupId)) {
                        groupedServerSessions.put(groupId, new ArrayList<>());
                    }
                    groupedServerSessions.get(groupId).add(serverSession);
                }
            }
        }
        return groupedServerSessions;
    }

    private ServerSession selectSession(ArrayList<ServerSession> sessionList) {
        double[] scoreArray = new double[sessionList.size()];
        for(int i = 0; i < sessionList.size(); i++) {
            scoreArray[i] = sessionList.get(i).getPublishScore();
        }

        double sumScore = 0;
        for(int i = 0; i < scoreArray.length; i++) {
            sumScore += scoreArray[i];
        }

        double rand = (Math.random())*sumScore;
        int selectedIdx = 0;
        double aggr = 0;
        for(int i = 0; i < scoreArray.length; i++) {
            aggr += scoreArray[i];
            if(aggr >= rand) {
                selectedIdx = i;
                break;
            }
        }

        if(scoreArray[selectedIdx] == 0) {
            return null;
        } else {
            return sessionList.get(selectedIdx);
        }
    }

    /**
     * For a PUBLISH message, check whether any subscription in the subscription pool matches its topic.
     * If the topic is matched, create a PUBLISH and publish to the corresponding subscriber client.
     * @param mqttPublishMessage The PUBLISH message from the publisher.
     * */
    private void publishToSubscription(ServerSession serverSession, MqttPublishMessage mqttPublishMessage) {

        String topic = mqttPublishMessage.variableHeader().topicName();

        Subscription matchSubscription = serverSession.getSubscriptionPool().getMatchSubscription(topic);

        if(matchSubscription!=null) {
            int packetId = serverSession.getNextPacketId();
            // TODO: perform QoS selection between publish QoS and subscription QoS
            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, matchSubscription.getQos(), false, 0);
            MqttPublishVariableHeader mqttPublishVariableHeader
                    = new MqttPublishVariableHeader(matchSubscription.getTopic(), packetId);
            // TODO: figure out how to avoid being garbage collected.
            ByteBuf payload = Unpooled.copiedBuffer(mqttPublishMessage.payload());
            // TODO: figure out how to avoid being garbage collected.
            payload.retain();
            MqttPublishMessage mqttPublishMessageOut
                    = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, payload);
            if(matchSubscription.getSubscriberChannel().isActive()) {
                try {
                    // TODO: try to remove sync()
                    matchSubscription.getSubscriberChannel().writeAndFlush(mqttPublishMessageOut).sync();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public MessageDispatcher(ServerSessionPool serverSessionPool) {
        this.serverSessionPool = serverSessionPool;
    }
}
