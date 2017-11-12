package org.dsngroup.broke.broker.channel;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.mqtt.*;
import org.dsngroup.broke.broker.channel.handler.MqttMessageHandler;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MqttMessageHandlerTest {

    /**
     * Test the MqttMessageHandler on CONNECT message handling
     * 1. Send a CONNECT, the handler should send a CONNACK back with return code CONNECTION_ACCEPTED
     * 2. Send two CONNECT, the handler should close the channel.
     * */
    @Test
    public void mqttMessageHandlerSendConnectTest() {
        try {
            EmbeddedChannel channel = new EmbeddedChannel(new MqttMessageHandler());

            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                    MqttMessageType.CONNECT,
                    false,
                    MqttQoS.AT_LEAST_ONCE,
                    true,
                    0);

            MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(
                    "MQTT",
                    4,
                    false,
                    false,
                    true,
                    1,
                    true,
                    false,
                    10);

            MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(
                    "client_123",
                    "Foo",
                    "Bar".getBytes(),
                    null,
                    null
            );

            MqttConnectMessage mqttConnectMessage = new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);
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
        EmbeddedChannel channel1 = new EmbeddedChannel(new MqttMessageHandler());
        EmbeddedChannel channel2 = new EmbeddedChannel(new MqttMessageHandler());
        // Channel 1 and 2 connects to the same "embedded" server
        // TODO: are they really the same "embedded" server?
        assertEquals(channel1.remoteAddress(), channel2.remoteAddress());

        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                MqttMessageType.CONNECT,
                false,
                MqttQoS.AT_LEAST_ONCE,
                true,
                0);

        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(
                "MQTT",
                4,
                false,
                false,
                true,
                1,
                true,
                false,
                10);

        MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(
                "client_123",
                "Foo",
                "Bar".getBytes(),
                null,
                null
        );

        MqttConnectMessage mqttConnectMessage = new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);
        channel1.writeInbound(mqttConnectMessage);
        channel2.writeInbound(mqttConnectMessage);
        MqttConnAckMessage mqttConnAckMessage1 = channel1.readOutbound();
        MqttConnAckMessage mqttConnAckMessage2 = channel2.readOutbound();
        // The CONNACK of channel1 should be accepted.
        assertEquals(mqttConnAckMessage1.variableHeader().connectReturnCode(), MqttConnectReturnCode.CONNECTION_ACCEPTED);
        // The CONNACK of channel2 should be rejected because the same client ID is used by client 1
        assertEquals(mqttConnAckMessage2.variableHeader().connectReturnCode(), MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);

    }

}
