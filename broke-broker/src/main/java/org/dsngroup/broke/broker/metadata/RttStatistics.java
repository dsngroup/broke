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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

// TODO: metadata
public class RttStatistics {

    // Queue that stores history RTTs for statistic calculation.
    private ArrayDeque<Double> rttQueue;

    private final int maxRttNumber;

    // Average of RTTs
    private double rttAvg;

    // Standard deviation of RTTs
    private double rttDev;

    private static final Logger logger = LoggerFactory.getLogger(RttStatistics.class);

    // TODO: synchronize may be unnecessary?
    public synchronized double getRttAvg() {
        return rttAvg;
    }

    // TODO: synchronize may be unnecessary?
    public synchronized void updateRttStatistics(double newRtt) {
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

    public RttStatistics(int maxRttNumber) {
        this.maxRttNumber = maxRttNumber;
        this.rttQueue = new ArrayDeque<>();
    }

}
