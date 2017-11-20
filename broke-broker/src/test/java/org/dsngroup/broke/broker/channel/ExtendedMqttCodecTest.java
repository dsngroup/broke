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

package org.dsngroup.broke.broker.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.dsngroup.broke.protocol.*;
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
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("Mqtt Encoder", MqttEncoder.INSTANCE);
        channel.pipeline().addLast("Mqtt Decoder", new MqttDecoder());

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
        channel.writeInbound((ByteBuf)channel.readOutbound());
        MqttSubscribeMessage mqttSubscribeMessageOut = channel.readInbound();

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

        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("MqttEncoder", MqttEncoder.INSTANCE);

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
        ByteBuf buffer = channel.readOutbound();
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
        EmbeddedChannel channel2 = new EmbeddedChannel();
        channel2.pipeline().addLast("MqttDecoder", new MqttDecoder());

        channel2.writeInbound(bufferCopy);
        MqttSubscribeMessage mqttSubscribeMessage = channel2.readInbound();

        assertEquals(MqttMessageType.SUBSCRIBE, mqttSubscribeMessage.fixedHeader().messageType());
        assertEquals(packetId, mqttSubscribeMessage.variableHeader().messageId());
        assertEquals(topicFilter, mqttSubscribeMessage.payload().topicSubscriptions().get(0).topicName());
        assertEquals(groupId, mqttSubscribeMessage.payload().topicSubscriptions().get(0).groupId());
        assertEquals(0, bufferCopy.readableBytes());

    }

}
