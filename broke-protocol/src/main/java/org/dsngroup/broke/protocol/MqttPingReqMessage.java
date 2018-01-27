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

/**
 * Extended MQTT PINGREQ message.
 * Additional message id was added in variable header
 */
public class MqttPingReqMessage extends MqttMessage {

    public MqttPingReqMessage(MqttFixedHeader mqttFixedHeader, MqttPingReqVariableHeader variableHeader) {
        super(mqttFixedHeader, variableHeader);
    }

    @Override
    public MqttPingReqVariableHeader variableHeader() {
        return (MqttPingReqVariableHeader) super.variableHeader();
    }
}
