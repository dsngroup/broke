package org.dsngroup.broke.protocol;

/**
 * The PUBLISH message class
 * */
public class SubscribeMessage extends Message{

    /**
     * The constructor, construct the message by header and payload
     * @param header The pre-built header of the message
     * @param payload The payload of the message
     * @throws Exception Parsing failed.
     * */
    public SubscribeMessage(SubscribeMessageHeader header, Object payload) {
        super(Method.SUBSCRIBE, header, payload);
    }

    public SubscribeMessage(String rawHeader, Object payload) throws Exception {
        super(Method.SUBSCRIBE, new SubscribeMessageHeader(rawHeader), payload);
    }


    /**
     * Get the topic from the message
     * @return topic
     * */
    public String getTopic() {
        return ((SubscribeMessageHeader)header).getTopic();
    }

    /**
     * Get the group id from the message
     * @return group id
     * */
    public String getGroupId() {
        return ((SubscribeMessageHeader)header).getGroupId();
    }
}
