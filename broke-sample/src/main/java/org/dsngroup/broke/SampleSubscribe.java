/*
 * Copyright (c) 2017-2018 Dependable Network and System Lab, National Taiwan University.
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

import io.netty.buffer.ByteBuf;
import org.dsngroup.broke.client.BlockClient;
import org.dsngroup.broke.client.handler.callback.IMessageCallbackHandler;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.dsngroup.broke.protocol.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SampleSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(SampleSubscribe.class);

    public static void main(String[] args) {

        class MessageCallbackHandler implements IMessageCallbackHandler {

            @Override
            public void messageArrive(ByteBuf payload) {
                logger.info(payload.toString(StandardCharsets.UTF_8));
            }

            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Connection lost: " + cause.getMessage());
                System.exit(1);
            }

        }

        // TODO: better parsed from one string.
        try {

            String address = args[0].split(":")[0];
            int port = Integer.parseInt(args[0].split(":")[1]);

            BlockClient blockClient = new BlockClient(address, port);

            blockClient.connect(MqttQoS.AT_LEAST_ONCE, 0);

            blockClient.setMessageCallbackHandler(new MessageCallbackHandler());

            blockClient.subscribe("Foo", MqttQoS.AT_LEAST_ONCE, 0, 555);

            Scanner scanner = new Scanner(System.in);

            while(true) {
                String cmd = scanner.next();
                if (cmd.equals("terminate")) {
                    break;
                }
                Thread.sleep(3000);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Not enough arguments");
            System.exit(1);
        } catch (Exception e) {
            logger.error("Wrong settings of message options");
            logger.error(e.getMessage());
            System.exit(1);
        }
    }
}

