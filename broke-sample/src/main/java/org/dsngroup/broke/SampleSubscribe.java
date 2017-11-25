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
import org.dsngroup.broke.protocol.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(SampleSubscribe.class);

    public static void main(String[] args) {
        // The args[0] = address, args[1] = port
        // TODO: better parsed from one string.
        try {
            BlockClient blockClient = new BlockClient(args[0], Integer.parseInt(args[1]));

            blockClient.connect(0, 0);
            blockClient.subscribe("Foo", MqttQoS.AT_LEAST_ONCE, 0, 555);
            // blockClient.disconnect();
            while(true) {
                if (System.in.read()!=0){
                    // Block
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Not enough arguments");
            System.exit(1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("Wrong settings of message options");
            System.exit(1);
        }
    }
}
