package org.dsngroup.broke;

import org.dsngroup.broke.broker.Server;
import org.dsngroup.broke.broker.ServerContext;
import org.dsngroup.broke.client.BlockClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class IntegrationTest {
    @Test
    public void simpleClientServerTest() {
        try {
            /* TODO: An unterminated test because the server cannot terminate gracefully.
            Server server = new Server(8181, new ServerContext());
            server.run();
            BlockClient client = new BlockClient("0.0.0.0", 8181);
            client.connect(0, 0, "Bar");
            System.exit(0);
            */
        } catch (Exception e) {
            fail("[Fail] simple client server test ");
        }
    }
}
