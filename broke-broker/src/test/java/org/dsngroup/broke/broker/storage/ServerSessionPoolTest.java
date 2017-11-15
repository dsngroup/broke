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

package org.dsngroup.broke.broker.storage;

import org.dsngroup.broke.broker.ServerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ServerSessionPoolTest {

    @Test
    public void doNotCleanSessionTest() {

        ServerContext serverContext = new ServerContext();

        ServerSessionPool serverSessionPool = serverContext.getServerSessionPool();

        // Do not clean session
        ServerSession serverSession_1 = serverSessionPool.getSession("client_123", false);
        ServerSession serverSession_2 = serverSessionPool.getSession("client_123", false);

        // Two of the sessions should be the same session
        assertEquals(serverSession_1, serverSession_2);

    }

    @Test
    public void cleanSessionTest() {

        ServerContext serverContext = new ServerContext();

        ServerSessionPool serverSessionPool = serverContext.getServerSessionPool();

        // Clean session
        ServerSession serverSession_1 = serverSessionPool.getSession("client_123", true);
        ServerSession serverSession_2 = serverSessionPool.getSession("client_123", true);

        // Second session is a newly created session. Not the same as the first session.
        assertNotEquals(serverSession_1, serverSession_2);

    }

    @Test
    public void createManySessionTest() {

        ServerContext serverContext = new ServerContext();

        ServerSessionPool serverSessionPool = serverContext.getServerSessionPool();
        try {
            for (int i = 0; i < 10000; i++) {
                serverSessionPool.getSession("client_" + i, false);
            }
            for (int i = 0; i < 10000; i++) {
                serverSessionPool.getSession("client_" + i, false);
            }
            for (int i = 0; i < 10000; i++) {
                serverSessionPool.getSession("client_" + i, true);
            }
        } catch (Exception e) {
            fail("Create many session test failed: "+e.getStackTrace().toString());
        }
    }

}
