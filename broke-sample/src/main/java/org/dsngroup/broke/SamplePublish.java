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

package org.dsngroup.broke;

import org.dsngroup.broke.client.BlockClient;
import org.dsngroup.broke.client.exception.ConnectLostException;
import org.dsngroup.broke.client.handler.callback.IMessageCallbackHandler;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.dsngroup.broke.protocol.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * SamplePublish establish a connection and publish a message to a target broker.
 */

public class SamplePublish {

    private static final Logger logger = LoggerFactory.getLogger(SamplePublish.class);


    public static void main(String[] args) {

        class MessageCallbackHandler implements IMessageCallbackHandler {

            @Override
            public void messageArrive(MqttPublishMessage mqttPublishMessage) {
                logger.info(mqttPublishMessage.payload().toString(StandardCharsets.UTF_8));
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Connection lost: " + cause.getMessage());
                System.exit(1);
            }

        }

        // The args[0] = address, args[1] = port
        // TODO: better parsed from one string.
        try {

            String address = args[0].split(":")[0];
            int port = Integer.parseInt(args[0].split(":")[1]);

            BlockClient blockClient = new BlockClient(address, port);

            blockClient.connect(MqttQoS.AT_LEAST_ONCE, 0);

            blockClient.setMessageCallbackHandler(new MessageCallbackHandler());


            Thread.sleep(10000);
            while(true) {
                blockClient.publish("Foo", MqttQoS.AT_LEAST_ONCE, 0, "Bar");
                Thread.sleep(500);
            }
            // blockClient.disconnect();

        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Not enough arguments");
            System.exit(1);

        } catch (ConnectLostException e) {
            logger.error("Connection lost: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            System.exit(1);
        }
    }
}
