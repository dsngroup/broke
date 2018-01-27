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

package org.dsngroup.broke.broker.dispatch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.dsngroup.broke.broker.metadata.ClientSession;
import org.dsngroup.broke.broker.metadata.ClientSessionPool;
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

    private ClientSessionPool clientSessionPool;

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private ISessionSelector sessionSelector;

    // TODO: delete this
    private int printerCounter = 0;

    /**
     * Publish to subscriptions.
     */
    public void publishToSubscription(MqttPublishMessage mqttPublishMessage) {
        // Iterate through all sessions, publishing to every sessions' subscriptions
        for (ClientSession clientSession : clientSessionPool.asCollection()) {
            // Only publish to active sessions
            if (clientSession.getIsActive()) {
                // Whether the mqttPublish Message matches the subscription is performed in "publishToSubscription"
                publishToSubscription(clientSession, mqttPublishMessage);
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
     */
    public void groupBasedPublishToSubscription(MqttPublishMessage mqttPublishMessage) {
        String topic = mqttPublishMessage.variableHeader().topicName();
        TreeMap<Integer, ArrayList<ClientSession>> groupIdSessionListMap = getGroupedServerSessions(topic);
        for (Map.Entry<Integer, ArrayList<ClientSession>> entry: groupIdSessionListMap.entrySet()) {
            int groupId = entry.getKey();
            ArrayList<ClientSession> sessionList = entry.getValue();
            // Select over the session lists
            ClientSession selectedSession = sessionSelector.selectSession(sessionList);

            // TODO: debug
            printerCounter ++;
            if (printerCounter == 10000) {
                logger.info("Number of possible consumers: " + sessionList.size());
                for (ClientSession session : sessionList) {
                    logger.info(session.getClientId() +
                            ": score: " + session.getPublishScoreString() +
                            " total publishes: " + session.getPublishCount());
                }
                logger.info("Selected session: " + selectedSession.getClientId());
                printerCounter = 0;
            }

            if (selectedSession != null) {
                publishToSubscription(selectedSession, mqttPublishMessage);
                // TODO: remove after debug
                selectedSession.setPublishCount();
            } else {
                logger.debug("No available session now");
                // TODO: store the published message.
            }
        }
    }

    private TreeMap<Integer, ArrayList<ClientSession>> getGroupedServerSessions(String topic) {
        TreeMap<Integer, ArrayList<ClientSession>> groupedServerSessions = new TreeMap<>();
        for (ClientSession clientSession : clientSessionPool.asCollection()) {
            if (clientSession.getIsActive()) {
                SubscriptionPool subscriptionPool = clientSession.getSubscriptionPool();
                Subscription matchedSubscription = subscriptionPool.getMatchSubscription(topic);
                if (matchedSubscription != null) {
                    int groupId = matchedSubscription.getGroupId();
                    if (!groupedServerSessions.containsKey(groupId)) {
                        groupedServerSessions.put(groupId, new ArrayList<>());
                    }
                    groupedServerSessions.get(groupId).add(clientSession);
                }
            }
        }
        return groupedServerSessions;
    }

    /**
     * For a PUBLISH message, check whether any subscription in the subscription pool matches its topic.
     * If the topic is matched, create a PUBLISH and publish to the corresponding subscriber client.
     * @param mqttPublishMessage The PUBLISH message from the publisher.
     */
    private void publishToSubscription(ClientSession clientSession, MqttPublishMessage mqttPublishMessage) {

        String topic = mqttPublishMessage.variableHeader().topicName();

        Subscription matchSubscription = clientSession.getSubscriptionPool().getMatchSubscription(topic);

        if (matchSubscription != null) {
            int packetId = clientSession.getNextPacketId();
            // TODO: perform QoS selection between publish QoS and subscription QoS
            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, matchSubscription.getQos(), false, 0);
            MqttPublishVariableHeader mqttPublishVariableHeader =
                    new MqttPublishVariableHeader(matchSubscription.getTopic(), packetId);
            // TODO: figure out how to avoid being garbage collected.
            ByteBuf payload = Unpooled.copiedBuffer(mqttPublishMessage.payload());
            // TODO: figure out how to avoid being garbage collected.
            payload.retain();
            MqttPublishMessage mqttPublishMessageOut =
                    new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, payload);
            if (matchSubscription.getSubscriberChannel().isActive()) {
                try {
                    matchSubscription.getSubscriberChannel().writeAndFlush(mqttPublishMessageOut);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public MessageDispatcher(ClientSessionPool clientSessionPool) {
        this.clientSessionPool = clientSessionPool;
        this.sessionSelector = new RRSessionSelector();
    }
}
