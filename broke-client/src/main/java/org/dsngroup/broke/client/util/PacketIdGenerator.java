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

package org.dsngroup.broke.client.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Packet Id generator.
 * Packet Id should between 1~65535
 */
public class PacketIdGenerator {

    private AtomicInteger packetId;

    public int getPacketId() {
        int retVal = packetId.getAndIncrement();
        if(retVal > 65535) {
            synchronized (this) {
                if(packetId.get() > 65535) {
                    packetId.set(1);
                    retVal = packetId.getAndIncrement();
                }
            }
        }
        return retVal;

    }

    public PacketIdGenerator() {
        packetId = new AtomicInteger();
        packetId.set(1);
    }

}
