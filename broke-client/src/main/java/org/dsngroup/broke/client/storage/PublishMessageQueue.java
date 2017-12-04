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
public class PublishMessageQueue {

    private Deque<String> receivedPublishQueue;

    private int maxSize;

    private double lowWaterMark;

    private double highWaterMark;

    private boolean isBackPressured;

    public boolean isBackPressured() {
        return isBackPressured;
    }

    synchronized String getMessage() {
        if(receivedPublishQueue.size()>0) {
            if (receivedPublishQueue.size()-1<maxSize*lowWaterMark) {
                isBackPressured = false;
            }
            return receivedPublishQueue.poll();
        }
        return null;
    }

    synchronized void putMessage(String message) {
        if(receivedPublishQueue.size() < maxSize) {
            receivedPublishQueue.offer(message);
            if (receivedPublishQueue.size()>maxSize*highWaterMark) {
                isBackPressured = true;
            }
        }
    }

    public PublishMessageQueue(int maxSize, float lowWaterMark, float highWaterMark) {
        this.maxSize = maxSize;
        this.receivedPublishQueue = new ArrayDeque<>(maxSize);
        this.lowWaterMark = lowWaterMark;
        this.highWaterMark = highWaterMark;
        this.isBackPressured = false;
    }

}
