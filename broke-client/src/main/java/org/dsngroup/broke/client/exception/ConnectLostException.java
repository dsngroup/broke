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

package org.dsngroup.broke.client.exception;

/**
 * Exception of connection lost.
 * The exception can be thrown when the connection to the broker server lost.
 */
public class ConnectLostException extends Exception {

    /**
     * The constructor.
     * @param cause Cause of the connection lost.
     */
    public ConnectLostException(String cause) {
        super(cause);
    }
}
