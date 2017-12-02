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

package org.dsngroup.broke.broker.channel.handler;

import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import org.dsngroup.broke.broker.storage.PingRequests;
import org.dsngroup.broke.broker.storage.ServerSession;
import org.dsngroup.broke.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientProber {

    private AtomicInteger pingReqPacketIdGenerator;

    private ScheduledFuture pingReqScheduleFuture;

    private RttStatistics rttStatistics;

    private PingRequests pingRequests;

    private boolean isBackPressured;

    /**
     * Getter for current round-trip time
     * */
    public double getRttAvg() {
        return rttStatistics.getRttAvg();
    }

    private void setPingReq(int packetId) {
        pingRequests.setPingReq(packetId, System.nanoTime());
    }

    void setPingResp(int packetId) {
        long sendTime = pingRequests.getPingReq(packetId);
        if (sendTime != -1) {
            double newRtt = (System.nanoTime() - sendTime)/1e6;
            rttStatistics.updateRttStatistics(newRtt);
        }
    }

    /**
     * Getter for isBackPressured
     * */
    public boolean isBackPressured() {
        return isBackPressured;
    }

    /**
     * Setter for isBackPressured
     * */
    void setIsBackPressured(boolean isBackPressured) {
        this.isBackPressured = isBackPressured;
    }

    /**
     * Called in the processConnect(). When the client makes the connection successfully, schedule the PING message.
     * */
    void schedulePingReq(Channel channel, ServerSession serverSession) {
        pingReqScheduleFuture = channel.eventLoop().scheduleAtFixedRate(
                new Runnable(){
                    @Override
                    public void run(){

                        int packetId = pingReqPacketIdGenerator.getAndIncrement();
                        MqttFixedHeader mqttFixedHeader =
                                new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);
                        MqttPingReqVariableHeader mqttPingReqVariableHeader =
                                new MqttPingReqVariableHeader(false, packetId);
                        MqttPingReqMessage mqttPingReqMessage =
                                new MqttPingReqMessage(mqttFixedHeader, mqttPingReqVariableHeader);
                        // Set the PINGREQ's sendTime
                        setPingReq(packetId);
                        channel.writeAndFlush(mqttPingReqMessage);
                    }
                }, 3, 3, TimeUnit.SECONDS);
    }

    void cancelPingReq() {
        // Stop the PINGREQ schedule
        // TODO: is the check necessary?
        if (pingReqScheduleFuture!=null)
            pingReqScheduleFuture.cancel(false);
    }

    ClientProber() {
        this.pingReqPacketIdGenerator = new AtomicInteger();
        this.pingReqPacketIdGenerator.getAndIncrement();
        this.pingRequests = new PingRequests();
        this.rttStatistics = new RttStatistics(1000);
    }
}

// TODO: metadata
class RttStatistics {

    // Queue that stores history RTTs for statistic calculation.
    private ArrayDeque<Double> rttQueue;

    private final int maxRttNumber;

    // Average of RTTs
    private double rttAvg;

    // Standard deviation of RTTs
    private double rttDev;

    private static final Logger logger = LoggerFactory.getLogger(RttStatistics.class);

    // TODO: synchronize may be unnecessary?
    synchronized double getRttAvg() {
        return rttAvg;
    }

    // TODO: synchronize may be unnecessary?
    synchronized void updateRttStatistics(double newRtt) {
        logger.debug("Rtt numbers: "+rttQueue.size()+" Rtt New: "+newRtt+" RTT Avg "+rttAvg+" Rtt StdDev: "+rttDev);
        // Check validation only when the collected number of RTT is larger than half of the maxRttNumber
        if(rttQueue.size()>maxRttNumber/2 && !isValidRtt(newRtt)) {
            return;
        }
        rttQueue.offer(newRtt);
        while(rttQueue.size()>maxRttNumber) {
            rttQueue.poll();
        }

        // Update average
        double sum = 0;
        for(double rtt: rttQueue) {
            sum+=rtt;
        }
        rttAvg = sum/rttQueue.size();

        // Update rttDev;
        double rttDiffSquareSum = 0;
        for(double rtt: rttQueue) {
            rttDiffSquareSum += Math.pow(rtt-rttAvg,2);
        }
        rttDev = Math.sqrt(rttDiffSquareSum/rttQueue.size());
    }

    private boolean isValidRtt(double newRtt) {
        // A valid RTT should be in 2 standard deviations to the RTT average.
        return Math.abs(newRtt-rttAvg)<=2*rttDev;
    }

    RttStatistics(int maxRttNumber) {
        this.maxRttNumber = maxRttNumber;
        this.rttQueue = new ArrayDeque<>();
    }

}