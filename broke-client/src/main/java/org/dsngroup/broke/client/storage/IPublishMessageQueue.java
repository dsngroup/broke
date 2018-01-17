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

import io.netty.buffer.ByteBuf;

/**
 * Interface for a publish message queue.
 */
public interface IPublishMessageQueue {

    /**
     * Getter for the back-pressure status of the queue.
     */
    boolean isBackPressured();

    /**
     * Put the message into this queue.
     * @param message Message to put into the queue.
     */
    void putMessage(ByteBuf message);

    /**
     * Getter for the message at the front of the queue (First-In-First-Out).
     */
    ByteBuf getMessage();

    /**
     * Getter for the max number of messages this queue can store.
     */
    int getMaxSize();

    /**
     * Getter for the low watermark percentage in double.
     */
    double getLowWaterMark();

    /**
     * Getter for the high watermark percentage.
     */
    double getHighWaterMark();

    /**
     * Getter for the current capacity of the queue.
     */
    int getCapacity();

    /**
     * Getter for the current consumption rate of the queue.
     * @return consumption rate of the queue.
     */
    double getConsumptionRate();
}
