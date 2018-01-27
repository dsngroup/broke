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

import org.dsngroup.broke.broker.metadata.ClientSession;

import java.util.ArrayList;

public class LSMDSessionSelector implements ISessionSelector{
    @Override
    public ClientSession selectSession(ArrayList<ClientSession> sessionList) {
        double[] scoreArray = new double[sessionList.size()];
        for (int i = 0; i < sessionList.size(); i++) {
            boolean isBackPressured = sessionList.get(i).isBackPressured();
            double networkDelay = sessionList.get(i).getAverageRTT();
            double queuingDelay = sessionList.get(i).getEstimatedQueuingDelay();
            scoreArray[i] = getPublishScore(isBackPressured, networkDelay, queuingDelay);
        }

        double sumScore = 0;
        for (int i = 0; i < scoreArray.length; i++) {
            sumScore += scoreArray[i];
        }

        double rand = (Math.random()) * sumScore;
        int selectedIdx = 0;
        double aggr = 0;
        for (int i = 0; i < scoreArray.length; i++) {
            aggr += scoreArray[i];
            if (aggr >= rand) {
                selectedIdx = i;
                break;
            }
        }

        if (scoreArray[selectedIdx] == 0) {
            return null;
        } else {
            return sessionList.get(selectedIdx);
        }
    }

    /**
     * Getter for the current publish score.
     */
    private double getPublishScore(boolean isBackPressured, double networkDelay, double queuingDelay) {
        double bpFactor = isBackPressured ? 0.1 : 1.0;

        double networkDelayFactor = 20 * Math.pow(2, (200.0d - networkDelay) / 30.0d);
        /*
        if (networkDelay < 10) {
            networkDelayFactor = 100;
        } else if (networkDelay < 30) {
            networkDelayFactor = 50;
        } else if (networkDelay < 70) {
            networkDelayFactor = 25;
        } else if (networkDelay < 120) {
            networkDelayFactor = 12;
        } else {
            networkDelayFactor = 1;
        }
        */
        return networkDelayFactor * bpFactor;
    }

}
