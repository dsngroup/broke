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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ExtendedMqttCodecTest {

    /**
     * Test whether the codec works for groupId extension in SUBSCRIBE.
     * */
    @Test
    public void mqttSubscribeGroupIdCodecTest() {
        EmbeddedChannel channel = new EmbeddedChannel(MqttEncoder.INSTANCE);
        // channel.pipeline().addLast("Mqtt Decoder", new MqttDecoder());

        MqttFixedHeader mqttFixedHeader
                = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);

        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(111);

        List<MqttTopicSubscription> mqttTopicSubscriptionList = new ArrayList<>();
        mqttTopicSubscriptionList.add(new MqttTopicSubscription("Foo", MqttQoS.AT_LEAST_ONCE, 555));
        mqttTopicSubscriptionList.add(new MqttTopicSubscription("Foo", MqttQoS.AT_LEAST_ONCE, 666));
        MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(mqttTopicSubscriptionList);

        MqttSubscribeMessage mqttSubscribeMessageIn
                = new MqttSubscribeMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);

        channel.writeOutbound(mqttSubscribeMessageIn);
        EmbeddedChannel channel2 = new EmbeddedChannel(new MqttDecoder());
        channel2.writeInbound((ByteBuf)channel.readOutbound());
        // TODO: alternative than to cast.
        MqttSubscribeMessage mqttSubscribeMessageOut = (MqttSubscribeMessage) channel2.readInbound();

        assertNotEquals(mqttSubscribeMessageOut, null);

        assertEquals(mqttSubscribeMessageIn.payload().topicSubscriptions().get(0).groupId(),
                mqttSubscribeMessageOut.payload().topicSubscriptions().get(0).groupId());
        assertEquals(mqttSubscribeMessageIn.payload().topicSubscriptions().get(1).groupId(),
                mqttSubscribeMessageOut.payload().topicSubscriptions().get(1).groupId());
    }

    /**
     * Detailed test for both Mqtt Encoder and Decoder.
     * Check both encoded ByteBuf's content and decoded MqttMessage instance's content.
     * */
    @Test
    public void mqttSubscribeCodecTest() {

        int packetId = 123;
        int groupId = 555;
        String topicFilter = "Foo";

        EmbeddedChannel channel = new EmbeddedChannel(MqttEncoder.INSTANCE);

        MqttFixedHeader mqttFixedHeader
                = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);

        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(packetId);

        List<MqttTopicSubscription> mqttTopicSubscriptionList = new ArrayList<>();
        mqttTopicSubscriptionList.add(new MqttTopicSubscription(topicFilter, MqttQoS.AT_LEAST_ONCE, groupId));
        // mqttTopicSubscriptionList.add(new MqttTopicSubscription("Foo", MqttQoS.AT_LEAST_ONCE, 666));
        MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(mqttTopicSubscriptionList);

        MqttSubscribeMessage mqttSubscribeMessageIn
                = new MqttSubscribeMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);

        channel.writeOutbound(mqttSubscribeMessageIn);
        ByteBuf buffer = (ByteBuf) channel.readOutbound();
        ByteBuf bufferCopy = Unpooled.copiedBuffer(buffer); // deep copied buffer for decoder test

        if(buffer.isReadable()) {
            byte byte1 = buffer.readByte();
            byte remainingLength = buffer.readByte();
            String s1 = String.format("%8s", Integer.toBinaryString(byte1 & 0xFF)).replace(' ', '0');
            assertEquals( "10000010", s1);
            assertEquals(buffer.readableBytes(), remainingLength);
            int packetIdActual = buffer.readShort();  // length=2
            assertEquals(packetId, packetIdActual);

            while(buffer.isReadable()) {
                int topicFilterLength = buffer.readShort();    // length=2
                assertEquals(topicFilter.length(), topicFilterLength);
                String topicFilterActual = buffer.readBytes(topicFilterLength).toString(StandardCharsets.UTF_8);   // length=3
                assertEquals(topicFilter, topicFilterActual);
                byte qos = buffer.readByte();       // length=1
                String qosActual = String.format("%8s", Integer.toBinaryString(qos & 0xFF)).replace(' ', '0');
                assertEquals("00000001", qosActual);
                int groupIdActual = buffer.readShort();   // length=2
                assertEquals(groupId, groupIdActual);
            }
        }
        assertEquals(0, buffer.readableBytes());

        // test decoder for subscribe message
        EmbeddedChannel channel2 = new EmbeddedChannel(new MqttDecoder());

        channel2.writeInbound(bufferCopy);
        MqttSubscribeMessage mqttSubscribeMessage = (MqttSubscribeMessage) channel2.readInbound();

        assertEquals(MqttMessageType.SUBSCRIBE, mqttSubscribeMessage.fixedHeader().messageType());
        assertEquals(packetId, mqttSubscribeMessage.variableHeader().messageId());
        assertEquals(topicFilter, mqttSubscribeMessage.payload().topicSubscriptions().get(0).topicName());
        assertEquals(groupId, mqttSubscribeMessage.payload().topicSubscriptions().get(0).groupId());
        assertEquals(0, bufferCopy.readableBytes());
    }

    @Test
    public void mqttPingReqMsgCodecTest() {

        int packetId = 1;
        boolean isBackPressured = false;

        EmbeddedChannel serverChannel = new EmbeddedChannel(MqttEncoder.INSTANCE);

        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttPingReqVariableHeader mqttPingReqVariableHeader =
                new MqttPingReqVariableHeader(isBackPressured, packetId);
        MqttPingReqMessage pingMsg =
                new MqttPingReqMessage(mqttFixedHeader, mqttPingReqVariableHeader);
        serverChannel.writeOutbound(pingMsg);
        ByteBuf pingReqMsgBuffer = (ByteBuf) serverChannel.readOutbound();

        assertEquals(5, pingReqMsgBuffer.readableBytes());
        if(pingReqMsgBuffer.isReadable()) {
            byte byte1 = pingReqMsgBuffer.readByte();
            String s1 = String.format("%8s", Integer.toBinaryString(byte1 & 0xFF)).replace(' ', '0');
            assertEquals("11000000", s1);
            byte byte2 = pingReqMsgBuffer.readByte();
            String s2 = String.format("%8s", Integer.toBinaryString(byte2 & 0xFF)).replace(' ', '0');
            assertEquals(3, byte2);
            boolean isBackPressuredActual = ( pingReqMsgBuffer.readByte() & 0x01 ) == 0x01;
            assertEquals(isBackPressured, isBackPressuredActual);
            int packetIdActual = pingReqMsgBuffer.readShort();
            assertEquals(packetId, packetIdActual);
        }
        assertEquals(0, pingReqMsgBuffer.readableBytes());
        pingReqMsgBuffer.resetReaderIndex();
        assertEquals(5, pingReqMsgBuffer.readableBytes());

        EmbeddedChannel clientChannel = new EmbeddedChannel(new MqttDecoder());

        try {
            clientChannel.writeInbound(pingReqMsgBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MqttMessage msgRec = (MqttMessage) clientChannel.readInbound();
        assertEquals(msgRec instanceof MqttPingReqMessage, true);
        MqttPingReqMessage pingReqMsgRec = (MqttPingReqMessage) msgRec;
        assertEquals(packetId, pingReqMsgRec.variableHeader().packetId());
        assertEquals(isBackPressured, pingReqMsgRec.variableHeader().isBackPressured());
    }

    @Test
    public void mqttPingRespMsgCodecTest() {

        int packetId = 1;
        boolean isBackPressured = true;
        int consumptionRate = 100;
        int queueCapacity = 200;

        EmbeddedChannel serverChannel = new EmbeddedChannel(MqttEncoder.INSTANCE);

        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttPingRespVariableHeader mqttPingRespVariableHeader =
                new MqttPingRespVariableHeader(isBackPressured, consumptionRate, queueCapacity, packetId);
        MqttPingRespMessage pingMsg =
                new MqttPingRespMessage(mqttFixedHeader, mqttPingRespVariableHeader);
        serverChannel.writeOutbound(pingMsg);
        ByteBuf pingRespMsgBuffer = (ByteBuf) serverChannel.readOutbound();

        assertEquals(9, pingRespMsgBuffer.readableBytes());
        if(pingRespMsgBuffer.isReadable()) {
            byte byte1 = pingRespMsgBuffer.readByte();
            String s1 = String.format("%8s", Integer.toBinaryString(byte1 & 0xFF)).replace(' ', '0');
            assertEquals("11010000", s1);
            byte byte2 = pingRespMsgBuffer.readByte();
            String s2 = String.format("%8s", Integer.toBinaryString(byte2 & 0xFF)).replace(' ', '0');
            assertEquals(7, byte2);
            boolean isBackPressuredActual = ( pingRespMsgBuffer.readByte() & 0x01 ) == 0x01;
            assertEquals(isBackPressured, isBackPressuredActual);
            int consumptionRateActual = pingRespMsgBuffer.readShort();
            assertEquals(consumptionRate, consumptionRateActual);
            int queueCapacityActual = pingRespMsgBuffer.readShort();
            assertEquals(queueCapacity, queueCapacityActual);
            int packetIdActual = pingRespMsgBuffer.readShort();
            assertEquals(packetId, packetIdActual);
        }
        assertEquals(0, pingRespMsgBuffer.readableBytes());
        pingRespMsgBuffer.resetReaderIndex();
        assertEquals(9, pingRespMsgBuffer.readableBytes());

        EmbeddedChannel clientChannel = new EmbeddedChannel(new MqttDecoder());

        try {
            clientChannel.writeInbound(pingRespMsgBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MqttMessage msgRec = (MqttMessage) clientChannel.readInbound();
        assertEquals(msgRec instanceof MqttPingRespMessage, true);
        MqttPingRespMessage pingRespMsgRec = (MqttPingRespMessage) msgRec;
        assertEquals(packetId, pingRespMsgRec.variableHeader().messageId());
        assertEquals(isBackPressured, pingRespMsgRec.variableHeader().isBackPressured());
        assertEquals(consumptionRate, pingRespMsgRec.variableHeader().getConsumptionRate());
        assertEquals(queueCapacity, pingRespMsgRec.variableHeader().getQueueCapacity());
    }
}
