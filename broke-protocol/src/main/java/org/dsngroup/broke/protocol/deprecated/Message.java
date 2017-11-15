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

package org.dsngroup.broke.protocol.deprecated;

/**
 * The Message class intialized an message which parsed from raw message (String).
 * <p>
 *     The message is used in this format.
 *     method\r\n
 *     header\r\n
 *     payload\r\n
 *     e.g.
 *     GET\r\n
 *     topic:Hello,qos:0,critical-option:0\r\n
 *     world\r\n
 *
 *     Fields of a header is separated by a comma.
 * </p>
 */
public class Message {

    // Message fields
    // TODO: may let this message to release the burden of construction
    // and destruction.
    protected Method method;
    protected MessageHeader header;
    protected Object payload;

    /**
     * The constructor, construct the message by method, header and payload
     * @param method The method of the messaage
     * @param header The header of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     */
    public Message(Method method, MessageHeader header, Object payload) {
        this.method = method;
        this.header = header;
        this.payload = payload;
    }

    /**
     * Get the method from this message.
     * @return method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Get the QoS from this message.
     * @return qos
     */
    public QoS getQos() {
        return header.getQos();
    }

    /**
     * Get the CriticalOption from this message.
     * @return crticalOption
     */
    public CriticalOption getCriticalOption() {
        return header.getCriticalOption();
    }

    /**
     * Get the header from this message.
     * @return header
     */
    public MessageHeader getHeader() {
        return header;
    }

    /**
     * Get the Payload from this message.
     * @return payload
     */
    public String getPayload() {
        return (String)payload;
    }
}
