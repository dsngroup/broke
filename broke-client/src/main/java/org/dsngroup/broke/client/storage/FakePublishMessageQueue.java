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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message queue for storing PUBLISH messages from the server
 * The isBackPressured status is emulated as follow:
 * 1. After the PUBLISH received is more than maxSize, set isBackPressured to true
 * 2. Start a thread that will set the isBackPressured to false after a fixed time.
 */
@SuppressWarnings("The fake publish module should move out to the outer scope.")
public class FakePublishMessageQueue implements IPublishMessageQueue {

    private int count;

    private int maxSize;

    private double lowWaterMark;

    private double highWaterMark;

    private boolean isBackPressured;

    private static final Logger logger = LoggerFactory.getLogger(FakePublishMessageQueue.class);

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
        return 0;
    }

    @Override
    public boolean isBackPressured() {
        return isBackPressured;
    }

    @Override
    public synchronized void putMessage(ByteBuf message) {
        count ++ ;
        if(count == maxSize) {
            isBackPressured = true;
            // Set the isBackPressured to false after 5 seconds.
            new BackPressureSetterThread(5000).start();
        }
    }

    @Override
    public synchronized ByteBuf getMessage() {
        return null;
    }

    @Override
    public double getConsumptionRate() {
        return -1;
    }

    /**
     * Constructor of the fake publish message queue.
     */
    public FakePublishMessageQueue(int maxSize, double lowWaterMark, double highWaterMark) {
        this.maxSize = maxSize;
        this.lowWaterMark = lowWaterMark;
        this.highWaterMark = highWaterMark;
    }

    /**
     * Set the back-pressure status to false after a while.
     */
    class BackPressureSetterThread extends Thread {

        private int execTime;

        @Override
        public void run() {
            try {
                Thread.sleep(execTime);
                isBackPressured = false;
                count = 0;
                // TODO: debug
                logger.info("cancel back pressure");
            } catch (Exception e) {
                logger.error("Back-pressure setter thread: " + e.getMessage());
            }
        }

        BackPressureSetterThread(int execTime) {
            this.execTime = execTime;
        }
    }

}

