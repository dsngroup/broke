package org.dsngroup.broke.protocol.deprecated;

import org.dsngroup.broke.protocol.deprecated.MessageHeader;

/**
 * The header class of the PUBLISH message
 * The class has an addition topic attribute than the basic message.
 * */
public class PublishMessageHeader extends MessageHeader {

    private String topic;

    /**
     * The constructor, construct the message header by a raw header string
     * @param rawHeader raw header string
     * @throws RuntimeException Wrong option fields.
     * */
    public PublishMessageHeader(String rawHeader) throws Exception {
        String[] fields = rawHeader.split(",");
        for (int i = 0; i < fields.length; i++) {
            String[] optionSplit = fields[i].split(":", 2);
            // TODO: There may be more options
            switch (optionSplit[0].toUpperCase()) {
                case "TOPIC":
                    topic = optionSplit[1];
                    break;
                default:
                    setOptions(optionSplit);
            }
        }
    }

    /**
     * Get the topic from this message.
     * @return topic
     */
    public String getTopic() {
        return topic;
    }
}
