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

import org.dsngroup.broke.broker.storage.*;
import org.dsngroup.broke.protocol.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * The ServerContext class records the associated information of a Server.
 * Server Session Pool: The pool that stores all server sessions
 * Subscriber Pool: The pool that stores all the subscriber sets by topic
 * Message Pool: The pool that stores all the PUBLISH messages
 */
public class ServerContext {

    private ServerSessionPool serverSessionPool;

    private MessagePool messagePool;

    private MessageDispatcher messageDispatcher;

    /**
     * Getter method for server session pool
     * */
    public ServerSessionPool getServerSessionPool() {
        return serverSessionPool;
    }

    /**
     * Getter method for message pool
     * */
    public MessagePool getMessagePool() {
        return messagePool;
    }

    /**
     * Getter method for message dispatcher
     * */
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    /**
     * Constructor of the server context
     * TODO: The ServerContext constructs the associated information of a Server,
     * TODO: will be used depends on what we need in the future.
     * */
    public ServerContext() {
        serverSessionPool = new ServerSessionPool();
        messagePool = new MessagePool();
        messageDispatcher = new MessageDispatcher(serverSessionPool);
    }
}
