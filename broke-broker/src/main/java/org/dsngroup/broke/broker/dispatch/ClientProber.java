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

package org.dsngroup.broke.broker.dispatch;

import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import org.dsngroup.broke.broker.metadata.PingRequestPool;
import org.dsngroup.broke.broker.metadata.RttStatistics;
import org.dsngroup.broke.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientProber {

    private static final Logger logger = LoggerFactory.getLogger(ClientProber.class);

    private AtomicInteger pingReqPacketIdGenerator;

    private ScheduledFuture pingReqScheduleFuture;

    private RttStatistics rttStatistics;

    private PingRequestPool pingRequestPool;

    private boolean isBackPressured;

    /**
     * Getter for current round-trip time.
     */
    public double getRttAvg() {
        return rttStatistics.getRttAvg();
    }

    private void setPingReq(int packetId) {
        pingRequestPool.setPingReq(packetId, System.nanoTime());
    }

    public void setPingResp(int packetId) {
        long sendTime = pingRequestPool.getPingReq(packetId);
        if (sendTime != -1) {
            double newRtt = (System.nanoTime() - sendTime) / 1e6;
            rttStatistics.updateRttStatistics(newRtt);
        }
    }

    /**
     * Getter for isBackPressured.
     */
    public boolean isBackPressured() {
        return isBackPressured;
    }

    /**
     * Setter for isBackPressured.
     */
    public void setIsBackPressured(boolean isBackPressured) {
        if (this.isBackPressured != isBackPressured ) {
            this.isBackPressured = isBackPressured;
            // TODO: debug
            logger.info("Back-pressure status toggled: " + isBackPressured);
        }
    }

    /**
     * Called in the processConnect(). When the client makes the connection successfully, schedule the PING message.
     */
    public void schedulePingReq(Channel channel) {
        pingReqScheduleFuture = channel.eventLoop().scheduleAtFixedRate(
                new Runnable(){
                    @Override
                    public void run(){

                        int packetId = pingReqPacketIdGenerator.getAndIncrement();
                        MqttFixedHeader mqttFixedHeader =
                                new MqttFixedHeader(MqttMessageType.PINGREQ,
                                        false, MqttQoS.AT_MOST_ONCE,
                                        false,
                                        0);
                        MqttPingReqVariableHeader mqttPingReqVariableHeader =
                                new MqttPingReqVariableHeader(false, packetId);
                        MqttPingReqMessage mqttPingReqMessage =
                                new MqttPingReqMessage(mqttFixedHeader, mqttPingReqVariableHeader);
                        // Set the PINGREQ's sendTime
                        setPingReq(packetId);
                        channel.writeAndFlush(mqttPingReqMessage);
                    }
                }, 1000, 250, TimeUnit.MILLISECONDS);
    }

    public void cancelPingReq() {
        // Stop the PINGREQ schedule
        // TODO: is the check necessary?
        if (pingReqScheduleFuture != null) {
            pingReqScheduleFuture.cancel(false);
        }
    }

    public ClientProber() {
        this.pingReqPacketIdGenerator = new AtomicInteger();
        this.pingReqPacketIdGenerator.getAndIncrement();
        this.pingRequestPool = new PingRequestPool();
        this.rttStatistics = new RttStatistics(1000);
    }
}

