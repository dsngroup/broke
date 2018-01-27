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

package org.dsngroup.broke.client.handler.callback;

import io.netty.buffer.ByteBuf;

/**
 * Interface of the message callback handler.
 */
public interface IMessageCallbackHandler {

    /**
     * Callback function called when a publish message arrived.
     */
    void messageArrive(ByteBuf payload);

    /**
     * Callback function called when the connection to the broker server lost.
     */
    void connectionLost(Throwable cause);
}
