package org.dsngroup.broke.protocol.deprecated;

/**
 * The PUBACK message class
 * */
public class PubAckMessage extends Message {

    /**
     * The constructor, construct the message by header and payload
     * @param header The pre-built header of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public PubAckMessage(PubAckMessageHeader header, Object payload) throws Exception {
        super(Method.PUBACK, header, payload);
    }

    /**
     * The constructor, construct the message by header and payload
     * @param rawHeader The header string of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public PubAckMessage(String rawHeader, Object payload) throws Exception {
        super(Method.PUBACK, new PubAckMessageHeader(rawHeader), payload);
    }
}
