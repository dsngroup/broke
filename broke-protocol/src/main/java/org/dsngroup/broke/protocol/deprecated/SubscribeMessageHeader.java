package org.dsngroup.broke.protocol.deprecated;

/**
 * The header class of the PUBLISH message
 * The class has an addition topic attribute than the basic message.
 * */
public class SubscribeMessageHeader extends MessageHeader{

    private String topic;
    private String groupId;

    /**
     * The constructor, construct the message header by a raw header string
     * @param rawHeader raw header string
     * @throws RuntimeException Wrong option fields.
     * */
    public SubscribeMessageHeader(String rawHeader) throws Exception{
        String[] fields = rawHeader.split(",");
        for (int i = 0; i < fields.length; i++) {
            String[] optionSplit = fields[i].split(":", 2);
            // TODO: There may be more options
            switch (optionSplit[0].toUpperCase()) {
                case "TOPIC":
                    topic = optionSplit[1];
                    break;
                case "GROUP-ID":
                    groupId = optionSplit[1];
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

    /**
     * Get the group id from this message.
     * @return group id
     */
    public String getGroupId() {
        return groupId;
    }

}
