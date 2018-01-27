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

package org.dsngroup.broke.broker.dispatch;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.dsngroup.broke.broker.metadata.PingRequestPool;
import org.dsngroup.broke.broker.metadata.RttStatistics;
import org.dsngroup.broke.protocol.MqttFixedHeader;
import org.dsngroup.broke.protocol.MqttMessageType;
import org.dsngroup.broke.protocol.MqttPingReqMessage;
import org.dsngroup.broke.protocol.MqttPingReqVariableHeader;
import org.dsngroup.broke.protocol.MqttQoS;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measure and keep RTT statistics, back-pressure status of the client.
 */
public class ClientProber {

    private static final Logger logger = LoggerFactory.getLogger(ClientProber.class);

    private AtomicInteger pingReqPacketIdGenerator;

    private ScheduledFuture pingReqScheduleFuture;

    private RttStatistics rttStatistics;

    private PingRequestPool pingRequestPool;

    private boolean isBackPressured;

    private int consumptionRate;

    private int queueCapacity;

    /**
     * Getter for current round-trip time.
     */
    public double getRttAvg() {
        return rttStatistics.getRttAvg();
    }

    /**
     * Set the sending time of a PINGREQ.
     * @param packetId packet identifier.
     */
    private void setPingReq(int packetId) {
        pingRequestPool.setPingReq(packetId, System.nanoTime());
    }

    /**
     * Set the receiving time of the PINGRESP and update RTT statistics.
     * @param packetId packet identifier.
     */
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
     * Getter for consumption rate of publish message queue of client.
     * @return consumption rate of publish message queue of client.
     */
    public int getConsumptionRate() {
        return consumptionRate;
    }

    /**
     * Getter for queue capacity.
     * @return capacity of publish message queue of client.
     */
    public int getQueueCapacity() {
        return queueCapacity;
    }

    /**
     * Getter for estimated queuing delay of the client.
     * @return queuing delay in milliseconds.
     */
    public double getEstimatedQueuingDelay() {
        return consumptionRate * 1000.0d / queueCapacity;
    }

    /**
     * Setter for isBackPressured.
     */
    public void setIsBackPressured(boolean isBackPressured) {
        if (this.isBackPressured != isBackPressured) {
            this.isBackPressured = isBackPressured;
            // TODO: debug
            logger.info("Back-pressure status toggled: " + isBackPressured);
        }
    }

    /**
     * Setter for consumption rate of publish message queue of client.
     * @param consumptionRate Consumption rate per second.
     */
    public void setConsumptionRate(int consumptionRate) {
        if (consumptionRate > this.consumptionRate) {
            this.consumptionRate = consumptionRate;
        }
    }

    /**
     * Setter for queue capacity.
     * @param queueCapacity Current queue capacity.
     */
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    /**
     * Schedule the periocid PINGREQ message.
     * @param channel Channel.
     */
    public void schedulePingReq(Channel channel) {
        pingReqScheduleFuture = channel.eventLoop().scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {

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
                        ChannelFuture future = channel.writeAndFlush(mqttPingReqMessage);
                        future.addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                if (!future.isSuccess()) {
                                    logger.error("PINGREQ failed: ");
                                }
                            }
                        });
                    }
                }, 1000, 250, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the periodic PINGREQ.
     */
    public void cancelPingReq() {
        // TODO: is the check necessary?
        if (pingReqScheduleFuture != null) {
            pingReqScheduleFuture.cancel(false);
        }
    }

    /**
     * Constructor.
     */
    public ClientProber() {
        this.pingReqPacketIdGenerator = new AtomicInteger();
        this.pingReqPacketIdGenerator.getAndIncrement();
        this.pingRequestPool = new PingRequestPool();
        this.rttStatistics = new RttStatistics(50);
    }
}

