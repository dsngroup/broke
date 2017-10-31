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

/**
 * SamplePublish establish a connection and publish a message to a target broker.
 */

public class SamplePublish {

    public static void main(String[] args) {
        // The args[0] = address, args[1] = port
        // TODO: better parsed from one string.
        try {
            BlockClient blockClient = new BlockClient(args[0], Integer.parseInt(args[1]));
            // blockClient.publish("Foo", 0, 0, "bar");
            while (true) {
                if (System.in.read()!=0){
                    blockClient.publish("Foo", 0, 0, "bar");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // TODO: We'll log System.out and System.err in the future
            System.out.println("Not enough arguments");
            System.exit(1);
        } catch (Exception e) {
            // TODO: use log instead of printStackTrace()
            e.printStackTrace();
            // TODO: We'll log System.out and System.err in the future
            System.out.println("Wrong settings of message options");
            System.exit(1);
        }
    }
}
