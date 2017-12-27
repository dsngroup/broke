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

package org.dsngroup.broke.client.storage;

import java.util.ArrayDeque;
import java.util.Deque;

@SuppressWarnings("The fake publish module should move out to the outer scope.")
public class PublishMessageQueue implements IPublishMessageQueue {

    private Deque<String> receivedPublishQueue;

    private int maxSize;

    private double lowWaterMark;

    private double highWaterMark;

    private boolean isBackPressured;

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
    public String getMessage() {
        if(receivedPublishQueue.size() > 0) {
            if (receivedPublishQueue.size()-1 < maxSize*lowWaterMark) {
                isBackPressured = false;
            }
            synchronized (receivedPublishQueue) {
                return receivedPublishQueue.poll();
            }
        }
        return null;
    }

    @Override
    public void putMessage(String message) {
        synchronized (receivedPublishQueue) {
            receivedPublishQueue.offer(message);
        }
        if (receivedPublishQueue.size() > maxSize*highWaterMark) {
            isBackPressured = true;
        }
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
        this.receivedPublishQueue = new ArrayDeque<>(maxSize);
        this.lowWaterMark = lowWaterMark;
        this.highWaterMark = highWaterMark;
        this.isBackPressured = false;
    }

}
