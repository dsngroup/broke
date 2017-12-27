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

package org.dsngroup.broke.client.handler.callback;

import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Default message callback handler.
 * Default behaviors of messageArrive() and connectionLost().
 */
public class DefaultMessageCallbackHandler implements IMessageCallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageCallbackHandler.class);

    /**
     * Print the publish message.
     * @param mqttPublishMessage The incoming publish message.
     */
    @Override
    public void messageArrive(MqttPublishMessage mqttPublishMessage) {
        logger.info("topic: " + mqttPublishMessage.variableHeader().topicName() +
                " payload: " + mqttPublishMessage.payload().toString(StandardCharsets.UTF_8));
    }

    /**
     * Log the connection lost error message.
     */
    @Override
    public void connectionLost(Throwable cause) {
        logger.error(cause.getMessage());
    }
}
