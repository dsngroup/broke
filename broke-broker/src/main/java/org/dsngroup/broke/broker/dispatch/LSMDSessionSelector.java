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

package org.dsngroup.broke.broker.dispatch;

import org.dsngroup.broke.broker.metadata.ClientSession;

import java.util.ArrayList;

public class LSMDSessionSelector implements ISessionSelector{
    @Override
    public ClientSession selectSession(ArrayList<ClientSession> sessionList) {
        double[] scoreArray = new double[sessionList.size()];
        for (int i = 0; i < sessionList.size(); i++) {
            scoreArray[i] = sessionList.get(i).getPublishScore();
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

}
