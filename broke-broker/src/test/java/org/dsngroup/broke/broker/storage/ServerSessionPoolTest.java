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
