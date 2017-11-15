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

import io.netty.channel.embedded.EmbeddedChannel;
import org.dsngroup.broke.protocol.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

        channel.writeInbound(mqttSubscribeMessageIn);
        MqttSubscribeMessage mqttSubscribeMessageOut = channel.readInbound();

        assertNotEquals(mqttSubscribeMessageOut, null);

        assertEquals(mqttSubscribeMessageIn.payload().topicSubscriptions().get(0).groupId(),
                mqttSubscribeMessageOut.payload().topicSubscriptions().get(0).groupId());
        assertEquals(mqttSubscribeMessageIn.payload().topicSubscriptions().get(1).groupId(),
                mqttSubscribeMessageOut.payload().topicSubscriptions().get(1).groupId());
    }
}
