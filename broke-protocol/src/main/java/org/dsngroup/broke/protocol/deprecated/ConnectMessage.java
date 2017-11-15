package org.dsngroup.broke.protocol.deprecated;

/**
 * The CONNNECT message class
 * */
public class ConnectMessage extends Message{

    /**
     * The constructor, construct the message by header and payload
     * @param header The pre-built header of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public ConnectMessage(ConnectMessageHeader header, Object payload) throws Exception {
        super(Method.CONNECT, header, payload);
    }

    /**
     * The constructor, construct the message by header and payload
     * @param rawHeader The header string of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public ConnectMessage(String rawHeader, Object payload) throws Exception {
        super(Method.CONNECT, new ConnectMessageHeader(rawHeader), payload);
    }
}
