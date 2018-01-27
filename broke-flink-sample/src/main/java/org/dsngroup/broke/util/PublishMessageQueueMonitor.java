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

package org.dsngroup.broke.util;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.dsngroup.broke.client.storage.IPublishMessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A thread that reports the status of publish message queue periodically.
 * E.g. Current back-pressure status, queue capacity and default max size of the queue.
 */
public class PublishMessageQueueMonitor extends AbstractRichFunction implements Runnable, Serializable {

    public static final Logger logger = LoggerFactory.getLogger(PublishMessageQueueMonitor.class);

    IPublishMessageQueue publishMessageQueue;

    @Override
    public void run() {
        while (true) {
            // TODO: debug
            logger.info("BP status: " + publishMessageQueue.isBackPressured()
                    + " capacity: " + publishMessageQueue.getCapacity()
                    + " consumption rate: " + publishMessageQueue.getConsumptionRate()
                    + " time: " + System.currentTimeMillis());
            try {
                Thread.sleep(250);
            } catch (Exception e) {
                logger.error("Monitor thread failed: " + e.getMessage());
            }
        }
    }

    /**
     * Constructor of the queue monitor.
     * Set the publish message queue to monitor.
     * @param publishMessageQueue Publish message queue to monitor.
     */
    public PublishMessageQueueMonitor(IPublishMessageQueue publishMessageQueue) {
        this.publishMessageQueue = publishMessageQueue;
    }
}

