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

package org.syyllab.broke;

/**
 * The Server class used as an entry instance.
 * An example creation,
 * <code>
 *     Server srv = new Server(port, ctx).run();
 * </code>
 */
public class Server {

    private int port;

    private final ServerContext ctx;

    /**
     * The Server constructor construct a basic information of a Server.
     * @param port the binding port.
     * @param ctx the {@link SeverContext} instance for associated information.
     */
    public Server(int port, ServerContext ctx) {
        this.port = port;
        this.ctx = ctx;
    }

    /**
     * Run the server.
     * @throws Exception
     * @return Server for chaining methods.
     */
    public Server run() throws Exception {
        return this;
    }

}
