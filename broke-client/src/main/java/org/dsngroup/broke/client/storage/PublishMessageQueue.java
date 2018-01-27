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

package org.dsngroup.broke.client.storage;

import io.netty.buffer.ByteBuf;

import java.util.Deque;
import java.util.concurrent.*;

@SuppressWarnings("The fake publish module should move out to the outer scope.")
public class PublishMessageQueue implements IPublishMessageQueue {

    private BlockingQueue<ByteBuf> receivedPublishQueue;

    private final int maxSize;

    private final double lowWaterMark;

    private final double highWaterMark;

    private boolean isBackPressured;

    private long consumeStartTime = System.currentTimeMillis();

    private volatile long consumeCount = 0;

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public double getLowWaterMark() {
        return lowWaterMark;
    }

    @Override
    public double getHighWaterMark() {
        return highWaterMark;
    }

    @Override
    public int getCapacity() {
        return receivedPublishQueue.size();
    }

    @Override
    public boolean isBackPressured() {
        return isBackPressured;
    }

    @Override
    public ByteBuf getMessage() {
        if (receivedPublishQueue.size()-1 < maxSize*lowWaterMark) {
            setIsBackPressured(false);
        }
        if( ! receivedPublishQueue.isEmpty() ) {
            consumeCount++;
            // This is non-blocking. Return null if empty.
            return receivedPublishQueue.poll();
        }
        return null;
    }

    @Override
    public void putMessage(ByteBuf message) {
        // TODO: undo String to ByteBuf message type modification
        message.retain();
        receivedPublishQueue.offer(message);
        if (receivedPublishQueue.size() > maxSize*highWaterMark) {
            setIsBackPressured(true);
        }
    }

    @Override
    public double getConsumptionRate() {
        // TODO: Better consumption rate monitoring mechanism.
        double duration = (System.currentTimeMillis() - consumeStartTime) / 1000.0d;
        long thisConsumeCount = consumeCount;
        consumeCount = 0;
        consumeStartTime = System.currentTimeMillis();
        return thisConsumeCount / duration;
    }

    private synchronized void setIsBackPressured(boolean isBackPressured) {
        this.isBackPressured = isBackPressured;
    }

    /**
     * The constructor of the publish message queue.
     * @param maxSize The max number of messages the queue can contain.
     *                However, messages that exceed this value will still be stored.
     * @param highWaterMark The high watermark
     * @param lowWaterMark The low watermark.
     */
    public PublishMessageQueue(int maxSize, double lowWaterMark, double highWaterMark) {
        this.maxSize = maxSize;
        this.receivedPublishQueue = new ArrayBlockingQueue<>(10 * maxSize);
        this.lowWaterMark = lowWaterMark;
        this.highWaterMark = highWaterMark;
        this.isBackPressured = false;
    }

}
