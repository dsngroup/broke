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

package org.dsngroup.broke.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.dsngroup.broke.client.ClientContext;
import org.dsngroup.broke.protocol.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests of the Mqtt Message Handler of the client side.
 * */
public class MqttMessageHandlerTest {

    /**
     * Test the PUBLISH message handling.
     * */
    @Test
    public void publishTest() {
        EmbeddedChannel channel = new EmbeddedChannel(new MqttMessageHandler(new ClientContext("client_01")));

        String topic = "Foo";
        int packetId = 123;
        String payloadString = "Bar";

        // create publish message
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0);

        MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(topic, packetId);

        ByteBuf payload = Unpooled.copiedBuffer(payloadString.getBytes());
        payload.retain();
        MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, payload);

        try {
            channel.writeInbound(mqttPublishMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test with codec
        channel.pipeline().addFirst("MqttDecoder", new MqttDecoder());
        channel.pipeline().addLast("MqttEncoder", MqttEncoder.INSTANCE);

        channel.writeOutbound(mqttPublishMessage);
        ByteBuf pubMsgBuffer = channel.readOutbound();
        ByteBuf pubMsgBufferCopy = Unpooled.copiedBuffer(pubMsgBuffer);

        if(pubMsgBuffer.isReadable()) {
            byte byte1 = pubMsgBuffer.readByte();
            byte remainingLength = pubMsgBuffer.readByte();
            String s1 = String.format("%8s", Integer.toBinaryString(byte1 & 0xFF)).replace(' ', '0');
            assertEquals( "00110010", s1);
            assertEquals(pubMsgBuffer.readableBytes(), remainingLength);

            int topicLengthActual = pubMsgBuffer.readShort();   // length = 2
            assertEquals(topic.length(), topicLengthActual);

            String topicActual = pubMsgBuffer.readBytes(topicLengthActual).toString(StandardCharsets.UTF_8);
            assertEquals(topic, topicActual);

            int packetIdActual = pubMsgBuffer.readShort();  // length=2
            assertEquals(packetId, packetIdActual);

            int payloadLengthActual = remainingLength-2-2-topicLengthActual;
            assertEquals(payloadLengthActual, payloadString.length());
            String payloadActual = pubMsgBuffer.readBytes(payloadLengthActual).toString(StandardCharsets.UTF_8);
            assertEquals(payloadString, payloadActual);
        }
        assertEquals(pubMsgBuffer.isReadable(), false);

        assertEquals(pubMsgBufferCopy.isReadable(), true);

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MqttDecoder());
        embeddedChannel.writeInbound(pubMsgBufferCopy);
        MqttPublishMessage mqttPublishMessageOut = embeddedChannel.readInbound();
        // channel.writeInbound(pubMsgBufferCopy);
        // MqttPublishMessage mqttPublishMessageOut = channel.readInbound();

        assertEquals(mqttPublishMessageOut.fixedHeader().messageType(), MqttMessageType.PUBLISH);
        embeddedChannel.pipeline().addLast(new MqttMessageHandler(new ClientContext("client_02")));
        embeddedChannel.writeInbound(mqttPublishMessageOut);
    }
}
