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

package org.dsngroup.broke.protocol;

/**
 * The Message class intialized an message which parsed from raw message (String).
 * <p>
 *     The message is used in this format.
 *     e.g.
 *     GET\r\n
 *     topic:Hello\r\n
 *     qos:0\r\n
 *     critical-option:0\r\n
 *     world
 * </p>
 */
public class Message {

    // Message fields
    // TODO: may let this message to release the burden of construction
    // and destruction.
    private Method method;
    private String topic;
    private QoS qos;
    private CriticalOption criticalOption;
    private String payload;

    /**
     * The constructor, construct the message class which parsed from rawMessage.
     * @param rawMessage The String rawMessage.
     * @throws Exception Parsing failed.
     */
    public Message(String rawMessage) throws Exception {
        parseToMessage(rawMessage);
    }

    /**
     * Parse a rawMessage into a Message.
     * @param rawMessage the rawMessage accepted from handler.
     */
    private void parseToMessage(String rawMessage) throws Exception {

        // TODO: Safely deal with the wrong format of message.
        // Use CLRT to split for the fields
        String[] fields = rawMessage.split("\r\n");

        // Parse the request line
        parseMethod(fields[0]);

        // Parse the options
        parseOptionsFields(fields);

        // Payload
        payload = fields[fields.length - 1];
    }

    /**
     * Parse the message request method.
     * @param requestField the first line of the message field.
     * @throws Exception the message parsing failed.
     */
    private void parseMethod(String requestField) throws Exception {
        // Parse the method field
        switch (requestField) {
            case "GET":
                method = Method.GET;
                break;
            case "PUT":
                method = Method.PUT;
                break;
            default:
                throw new RuntimeException("Wrong request method");
        }
    }

    /**
     * Parse the message option fields
     * @param fields the message options fields, split from raw string.
     * @throws Exception the parse failed runtime exception.
     */
    private void parseOptionsFields(String[] fields) throws Exception {
         for (int i = 1; i < fields.length - 1; i++) {
            String[] optionSplit = fields[i].split(":", 2);
            switch (optionSplit[0].toUpperCase()) {
                case "TOPIC":
                    topic = optionSplit[1];
                    break;
                case "QOS":
                    qos = QoS.values()[Integer.parseInt(optionSplit[1])];
                    break;
                case "CRITICAL-OPTION":
                    criticalOption = CriticalOption.values()[Integer.parseInt(optionSplit[1])];
                    break;
                default:
                    throw new RuntimeException("Wrong option fields.");
            }
        }
    }

    /**
     * Get the method from this message.
     * @return method
     */
    public Method getMethod() {
        return method;
    }
    /**
     * Get the topic from this message.
     * @return topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Get the QoS from this message.
     * @return qos
     */
    public QoS getQos() {
        return qos;
    }

    /**
     * Get the CriticalOption from this message.
     * @return crticalOption
     */
    public CriticalOption getCriticalOption() {
        return criticalOption;
    }

    /**
     * Get the Payload from this message.
     * @return payload
     */
    public String getPayload() {
        return payload;
    }
}
