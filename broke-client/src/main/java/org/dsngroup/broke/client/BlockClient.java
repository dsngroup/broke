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

package org.dsngroup.broke.client;

import org.dsngroup.broke.protocol.*;
import org.dsngroup.broke.protocol.MessageBuilder;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * The BlockClient should be deprecated after the asynchronous client is implemented.
 * Only used for test!
 */
public class BlockClient {

    private String targetBrokerAddress;

    private int targetBrokerPort;

    private Socket clientSocket;

    private OutputStream clientOutputStream;

    /**
     * @param topic topic to publish
     * @param qos qos of transmission
     * @param criticalOption TODO: critical option mechanism
     * @param payload payload of the message
     */
    public void publish(String topic, int qos, int criticalOption, String payload) throws Exception {
        // TODO: Send this message
        clientOutputStream.write(("PUBLISH\r\nQoS:"+qos+",Topic:"+topic+"\r\n"+payload+"\r\n")
                .getBytes(Charset.forName("UTF-8")));
        // clientOutputStream.write(msg.toString().getBytes(Charset.forName("UTF-8")));
        // TODO: We'll log System.out and System.err in the future
        System.out.println("Publish to topic: " + topic + "\nPayload: " + payload );
    }

    /**
     * The default constructor
     */
    public BlockClient() {
        this("0.0.0.0", 8181);
    }

    /**
     * The constructor for creating a BlockClient
     * 1. Build a connection to client
     * 2. Send a CONNECT message to server.
     * @param targetBrokerAddress The address of broker server.
     * @param targetBrokerPort The port of broker server.
     */
    public BlockClient(String targetBrokerAddress, int targetBrokerPort) {
        this.targetBrokerAddress = targetBrokerAddress;
        this.targetBrokerPort = targetBrokerPort;
        // TODO: Connect to the Broker Server, a.k.a. Send CONNECTION message
        try {
            clientSocket = new Socket();
            clientSocket.setKeepAlive(true);
            clientSocket.connect(new InetSocketAddress(this.targetBrokerAddress, this.targetBrokerPort));
            clientOutputStream = clientSocket.getOutputStream();

            clientOutputStream.write( "CONNECT\r\nQoS:0\r\nnothing\r\n".getBytes(Charset.forName("UTF-8")) );

        } catch (Exception e) {
            // TODO: use log instead of printStackTrace()
            e.printStackTrace();
        }
    }


    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            // TODO: use log instead of printStackTrace()
            e.printStackTrace();
        }
    }
}
