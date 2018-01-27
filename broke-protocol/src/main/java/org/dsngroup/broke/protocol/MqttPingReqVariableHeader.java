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

public final class MqttPingReqVariableHeader {

    private final int packetId;
    private final boolean isBackPressured;

    public MqttPingReqVariableHeader(boolean isBackPressured, int packetId) {
        this.isBackPressured = isBackPressured;
        this.packetId = packetId;
    }

    public boolean isBackPressured() {
        return isBackPressured;
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
                .append(", packetId=").append(packetId)
                .append(']')
                .toString();
    }

}
