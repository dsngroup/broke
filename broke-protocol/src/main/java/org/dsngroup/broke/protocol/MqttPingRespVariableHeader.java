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

package org.dsngroup.broke.protocol;

import io.netty.util.internal.StringUtil;

/**
 * Variable header of PINGRESP.
 */
public class MqttPingRespVariableHeader {

    private final int packetId;
    private final boolean isBackPressured;
    private final int consumptionRate;
    private final int queueCapacity;

    /**
     * Constructor.
     * @param isBackPressured Back-pressure status.
     * @param consumptionRate Current consumption rate.
     * @param queueCapacity Current capacity of the queue.
     * @param packetId Packet identifier.
     */
    public MqttPingRespVariableHeader(boolean isBackPressured, int consumptionRate, int queueCapacity, int packetId) {
        this.isBackPressured = isBackPressured;
        this.packetId = packetId;
        this.consumptionRate = consumptionRate;
        this.queueCapacity = queueCapacity;
    }

    public boolean isBackPressured() {
        return isBackPressured;
    }

    public int getConsumptionRate() {
        return consumptionRate;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    @Deprecated
    public int messageId() {
        return packetId;
    }

    public int packetId() {
        return packetId;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
                .append('[')
                .append("isBackPressured=").append(isBackPressured)
                .append(", consumptionRate=").append(consumptionRate)
                .append(", queueCapacity=").append(queueCapacity)
                .append(", packetId=").append(packetId)
                .append(']')
                .toString();
    }

}
