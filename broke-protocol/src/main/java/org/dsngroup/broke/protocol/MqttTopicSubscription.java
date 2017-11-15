/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.dsngroup.broke.protocol;

import io.netty.util.internal.StringUtil;

/**
 * Contains a topic name and Qos Level.
 * This is part of the {@link MqttSubscribePayload}
 */
public final class MqttTopicSubscription {

    private final String topicFilter;
    private final MqttQoS qualityOfService;
    private final int groupId;

    public MqttTopicSubscription(String topicFilter, MqttQoS qualityOfService, int groupId) {
        this.topicFilter = topicFilter;
        this.qualityOfService = qualityOfService;
        this.groupId = groupId;
    }

    public String topicName() {
        return topicFilter;
    }

    public MqttQoS qualityOfService() {
        return qualityOfService;
    }

    public int groupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("topicFilter=").append(topicFilter)
            .append(", qualityOfService=").append(qualityOfService)
            .append(", groupId=").append(groupId)
            .append(']')
            .toString();
    }
}
