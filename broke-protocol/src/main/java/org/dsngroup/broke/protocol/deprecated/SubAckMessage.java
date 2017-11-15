package org.dsngroup.broke.protocol.deprecated;

public class SubAckMessage extends Message {
    /**
     * The constructor, construct the message by header and payload
     * @param header The pre-built header of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public SubAckMessage(SubAckMessageHeader header, Object payload) throws Exception {
        super(Method.SUBACK, header, payload);
    }

    /**
     * The constructor, construct the message by header and payload
     * @param rawHeader The header string of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public SubAckMessage(String rawHeader, Object payload) throws Exception {
        super(Method.SUBACK, new SubAckMessageHeader(rawHeader), payload);
    }
}
