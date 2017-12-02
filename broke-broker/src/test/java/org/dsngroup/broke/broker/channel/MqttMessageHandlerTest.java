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
import org.dsngroup.broke.protocol.*;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.broker.channel.handler.MqttMessageHandler;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MqttMessageHandlerTest {

    /**
     * Test the MqttMessageHandler on CONNECT message handling
     * 1. Send a CONNECT, the handler should send a CONNACK back with return code CONNECTION_ACCEPTED
     * 2. Send two CONNECT, the handler should close the channel.
     * */
    @Test
    public void mqttConnectTest() {

        ServerContext serverContext = new ServerContext();

        try {
            EmbeddedChannel channel = new EmbeddedChannel(new MqttMessageHandler(serverContext));

            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader( MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE,
                            true, 0);

            MqttConnectVariableHeader mqttConnectVariableHeader =
                    new MqttConnectVariableHeader("MQTT", 4, false, false,
                            true, 1, true, false, 10);

            MqttConnectPayload mqttConnectPayload =
                    new MqttConnectPayload("client_123", "Foo", "Bar".getBytes(),
                            null, null);

            MqttConnectMessage mqttConnectMessage =
                    new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);
            channel.writeInbound(mqttConnectMessage);

            MqttConnAckMessage mqttConnAckMessage = channel.readOutbound();
            // CONNACK must have the return code CONNECTION_ACCEPTED
            assertEquals(mqttConnAckMessage.variableHeader().connectReturnCode(), MqttConnectReturnCode.CONNECTION_ACCEPTED);
            // The channel must be active now
            assertEquals(channel.isActive(), true);


            // Send second CONNECT, the handler should close the channel
            channel.writeInbound(mqttConnectMessage);
            // The channel should become inactive now
            assertEquals(channel.isActive(), false);

        } catch (Exception e) {
            fail("[Test] MqttMessageHandler send CONNECT test failed");
        }
    }

    /**
     * Two clients with the same client ID send CONNECT to the same broker.
     * Only first client will be accepted, the second client will be rejected because the client ID is used.
     * */
    @Test
    public void twoClientsSendsSameClientIdTest() {

        ServerContext serverContext = new ServerContext();

        EmbeddedChannel channel1 = new EmbeddedChannel();
        channel1.pipeline().addLast(new MqttMessageHandler(serverContext));
        EmbeddedChannel channel2 = new EmbeddedChannel();
        channel2.pipeline().addLast(new MqttMessageHandler(serverContext));
        // Channel 1 and 2 connects to the same "embedded" server
        assertEquals(channel1.remoteAddress(), channel2.remoteAddress());

        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE, true, 0);
        MqttConnectVariableHeader mqttConnectVariableHeader =
                new MqttConnectVariableHeader("MQTT", 4, false, false,
                        true, 1, true, false, 10);
        MqttConnectPayload mqttConnectPayload =
                new MqttConnectPayload("client_123", "Foo", "Bar".getBytes(),
                null, null);
        MqttConnectMessage mqttConnectMessage =
                new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);

        channel1.writeInbound(mqttConnectMessage);
        channel2.writeInbound(mqttConnectMessage);
        MqttConnAckMessage mqttConnAckMessage1 = channel1.readOutbound();
        MqttConnAckMessage mqttConnAckMessage2 = channel2.readOutbound();
        // The CONNACK of channel1 should be accepted.
        assertEquals(mqttConnAckMessage1.variableHeader().connectReturnCode(), MqttConnectReturnCode.CONNECTION_ACCEPTED);
        // The CONNACK of channel2 should be rejected because the same client ID is used by client 1
        assertEquals(mqttConnAckMessage2.variableHeader().connectReturnCode(), MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);

    }

    @Test
    public void basicPublishSubscribeTest() {

        ServerContext serverContext = new ServerContext();

        EmbeddedChannel publisherChannel = new EmbeddedChannel();
        publisherChannel.pipeline().addLast(new MqttMessageHandler(serverContext));
        EmbeddedChannel subscriberChannel = new EmbeddedChannel();
        subscriberChannel.pipeline().addLast(new MqttMessageHandler(serverContext));

        // Creation of CONNECT message for both publisher and subscriber clients
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE, true, 0);
        MqttConnectVariableHeader mqttConnectVariableHeader =
                new MqttConnectVariableHeader("MQTT", 4, false, false,
                        true, 1, true, false, 10);
        MqttConnectPayload publisherConnectPayload =
                new MqttConnectPayload("client_publisher", "Foo", "Bar".getBytes(),
                        null, null);
        MqttConnectPayload subscriberConnectPayload =
                new MqttConnectPayload("client_subscriber", "Foo", "Bar".getBytes(),
                        null, null);
        // Create CONNECT for publish client
        MqttConnectMessage mqttPublisherConnectMessage =
                new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, publisherConnectPayload);
        // Create CONNECT for subscribe client
        MqttConnectMessage mqttSubscriberConnectMessage =
                new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, subscriberConnectPayload);

        // Two of the connections should all be accepted
        publisherChannel.writeInbound(mqttPublisherConnectMessage);
        MqttConnAckMessage publisherConnAck = publisherChannel.readOutbound();
        assertEquals(publisherConnAck.variableHeader().connectReturnCode(), MqttConnectReturnCode.CONNECTION_ACCEPTED);

        subscriberChannel.writeInbound(mqttSubscriberConnectMessage);
        MqttConnAckMessage subscriberConnAck= subscriberChannel.readOutbound();
        assertEquals(subscriberConnAck.variableHeader().connectReturnCode(), MqttConnectReturnCode.CONNECTION_ACCEPTED);

        // Subscribe
        MqttFixedHeader subscriberFixedHeader =
                new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, true, 0);
        MqttMessageIdVariableHeader subscriberVariableHeader = MqttMessageIdVariableHeader.from(666);
        List<MqttTopicSubscription> mqttTopicSubscriptionList = new ArrayList<>();
        mqttTopicSubscriptionList.add(new MqttTopicSubscription("Foo", MqttQoS.AT_LEAST_ONCE, 555));
        MqttSubscribePayload subscriberSubscribePayload = new MqttSubscribePayload(mqttTopicSubscriptionList);

        MqttSubscribeMessage mqttSubscribeMessage =
                new MqttSubscribeMessage(subscriberFixedHeader, subscriberVariableHeader, subscriberSubscribePayload);

        subscriberChannel.writeInbound(mqttSubscribeMessage);
        MqttSubAckMessage mqttSubAckMessage = subscriberChannel.readOutbound();

        assertEquals(mqttSubAckMessage.variableHeader().messageId(), mqttSubscribeMessage.variableHeader().messageId());

        // Publish
        MqttFixedHeader publisherFixedHeader =
                new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, true, 0);
        MqttPublishVariableHeader publisherVariableHeader =
                new MqttPublishVariableHeader("Foo", 666);
        ByteBuf publisherPayload = Unpooled.wrappedBuffer("Hello world".getBytes());

        MqttPublishMessage mqttPublishMessage =
                new MqttPublishMessage(publisherFixedHeader, publisherVariableHeader, publisherPayload);

        publisherChannel.writeInbound(mqttPublishMessage);
        MqttPubAckMessage mqttPubAckMessage = publisherChannel.readOutbound();

        assertEquals(mqttPubAckMessage.variableHeader().messageId(), publisherVariableHeader.packetId());

        // Read published message from the subscriber channel
        MqttPublishMessage receivedMessage = subscriberChannel.readOutbound();

        assertEquals(receivedMessage.payload().toString(StandardCharsets.UTF_8),
                publisherPayload.toString(StandardCharsets.UTF_8));

    }

}
