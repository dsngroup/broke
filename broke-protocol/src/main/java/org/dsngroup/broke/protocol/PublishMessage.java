package org.dsngroup.broke.protocol;

/**
 * The PUBLISH message class
 * */
public class PublishMessage extends Message{

    /**
     * The constructor, construct the message by header and payload
     * @param header The pre-built header of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public PublishMessage(PublishMessageHeader header, Object payload) throws Exception {
        super(Method.PUBLISH, header, payload);
    }


    /**
     * The constructor, construct the message by header and payload
     * @param rawHeader The header string of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public PublishMessage(String rawHeader, Object payload) throws Exception {
        super(Method.PUBLISH, new PublishMessageHeader(rawHeader), payload);
    }

    /**
     * Get the topic from this message.
     * @return topic
     */
    public String getTopic() {
        return ((PublishMessageHeader)header).getTopic();
    }
}
