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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Session selector based on round-robin (RR).
 */
public class RRSessionSelector implements ISessionSelector {

    private static final Logger logger = LoggerFactory.getLogger(RRSessionSelector.class);

    private List<List<ClientSession>> existedSessionSets;

    private List<Integer> rrid;

    @Override
    public ClientSession selectSession(ArrayList<ClientSession> sessionList) {
        Set<String> clientIdSet2 = new HashSet();
        for (ClientSession session : sessionList) {
            clientIdSet2.add(session.getClientId());
        }
        for (int i = 0 ; i < existedSessionSets.size() ; i ++ ) {
            List<ClientSession> sessions = existedSessionSets.get(i);
            Set<String> clientIdSet1 = new HashSet();
            for (ClientSession session : sessions) {
                clientIdSet1.add(session.getClientId());
            }
            if (clientIdSet1.containsAll(clientIdSet2) && clientIdSet1.size() == clientIdSet2.size()) {
                int id = rrid.get(i);
                rrid.set(i, ( id + 1 ) % sessions.size());
                return sessions.get(id);
            }
        }
        sessionList.sort(new Comparator<ClientSession>() {
            @Override
            public int compare(ClientSession clientSession, ClientSession t1) {
                return clientSession.getClientId().compareTo(t1.getClientId());
            }
        });
        existedSessionSets.add(sessionList);
        rrid.add(0);
        return sessionList.get(0);
    }

    public RRSessionSelector() {
        existedSessionSets = new ArrayList();
        rrid = new ArrayList();
    }
}
