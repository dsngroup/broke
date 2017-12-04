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

package org.dsngroup.broke.broker;

import org.dsngroup.broke.broker.dispatch.MessageDispatcher;
import org.dsngroup.broke.broker.metadata.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The ServerContext class records the associated information of a Server.
 * Server Session Pool: The pool that stores all server sessions
 * Subscriber Pool: The pool that stores all the subscriber sets by topic
 * Message Pool: The pool that stores all the PUBLISH messages
 */
public class ServerContext {

    private ServerSessionPool serverSessionPool;

    // TODO: The message pool is not used anymore.
    @Deprecated
    private MessagePool messagePool;

    private MessageDispatcher messageDispatcher;

    /**
     * Getter method for server session pool
     */
    public ServerSessionPool getServerSessionPool() {
        return serverSessionPool;
    }

    /**
     * Getter method for message pool
     */
    @Deprecated
    public MessagePool getMessagePool() {
        return messagePool;
    }

    /**
     * Getter method for message dispatcher
     */
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    // Some explicitly configurable settings.
    private int boundPort;

    private int numOfBoss;

    private int numOfWorker;

    // Default settings
    private final int DEFAULT_BOUND_PORT = 8181;

    private final int DEFAULT_NUM_OF_BOSS = 1;

    private final int DEFAULT_NUM_OF_WORKER = 4;

    private void loadProperties() {
        // Sets up default properties
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("BOUND_PORT", String.valueOf(DEFAULT_BOUND_PORT));
        defaultProperties.setProperty("NUM_OF_BOSS", String.valueOf(DEFAULT_NUM_OF_BOSS));
        defaultProperties.setProperty("NUM_OF_WORKER", String.valueOf(DEFAULT_NUM_OF_WORKER));

        // Sets up properties from config
        Properties properties = new Properties(defaultProperties);

        try {
            InputStream contextStream =  this.getClass().getClassLoader()
                    .getResourceAsStream("ServerContext.properties");
            properties.load(contextStream);
            contextStream.close();
        } catch (Exception e) {
            // Don't log anything, use the default number
        }

        boundPort = Integer.parseInt(properties.getProperty("BOUND_PORT"));
        numOfBoss = Integer.parseInt(properties.getProperty("NUM_OF_BOSS"));
        numOfWorker = Integer.parseInt(properties.getProperty("NUM_OF_WORKER"));
    }

    public int getBoundPort() {
        return boundPort;
    }

    public int getNumOfBoss() {
        return numOfBoss;
    }

    public int getNumOfWorker() {
        return numOfWorker;
    }

    /**
     * Constructor of Server Context.
     */
    public ServerContext() {
        loadProperties();
        serverSessionPool = new ServerSessionPool();
        messagePool = new MessagePool();
        messageDispatcher = new MessageDispatcher(serverSessionPool);
    }
}
