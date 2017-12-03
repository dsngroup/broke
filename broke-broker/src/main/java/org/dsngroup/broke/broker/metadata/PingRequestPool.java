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

package org.dsngroup.broke.broker.metadata;

/**
 * Storage for PINGREQ messages
 * */
public class PingRequestPool {

    private int pingReqSize = 100;

    private PingReq[] pingReq;

    /**
     * An element of PINGREQ storage
     * 1. packetId: the packet identifier for PINGREQ.
     * 2. sendTime: the send time of the PINGREQ.
     * Used to calculate the round-trip time once the corresponding PINGRESP has received.
     * */
    private class PingReq {

        private int packetId;
        private long sendTime;

        synchronized long getPingReq(int packetId) {
            if(this.packetId == packetId)
                return sendTime;
            else
                return -1;
        }

        synchronized void setPingReq(int packetId, long sendTime) {
            this.packetId = packetId;
            this.sendTime = sendTime;
        }

        public PingReq(int packetId, long sendTime) {
            this.packetId = packetId;
            this.sendTime = sendTime;
        }

        PingReq() {
            this.packetId = 1;
            this.sendTime = -1;
        }

    }

    public long getPingReq(int packetId) {
        return pingReq[packetId % pingReqSize].getPingReq(packetId);
    }

    public void setPingReq(int packetId, long sendTime) {
        pingReq[packetId % pingReqSize].setPingReq(packetId, sendTime);
    }

    public PingRequestPool() {
        this(100);
    }

    public PingRequestPool(int pingReqSize) {
        pingReq = new PingReq[pingReqSize];
        for (int i=0; i<pingReqSize; i++) {
            pingReq[i] = new PingReq();
        }
    }
}

