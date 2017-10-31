package org.dsngroup.broke.protocol;

/**
 * The CONNACK message class
 * */
public class ConnAckMessage extends Message {

    /**
     * The constructor, construct the message by header and payload
     * @param header The pre-built header of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public ConnAckMessage(ConnAckMessageHeader header, Object payload) throws Exception {
        super(Method.CONNACK, header, payload);
    }

    /**
     * The constructor, construct the message by header and payload
     * @param rawHeader The header string of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public ConnAckMessage(String rawHeader, Object payload) throws Exception {
        super(Method.CONNACK, new ConnAckMessageHeader(rawHeader), payload);
    }
}
