package org.dsngroup.broke.protocol;


/**
 * Class for building message instance from a raw string message
 * */
public class MessageBuilder {

    /**
     * The constructor, prohibit this class to be instantiated outside the class.
     * */
    private MessageBuilder(){}


    /**
     * Static Message build method
     * TODO: this build function may be useless.
     * @param rawMessage raw message string
     * @return An instance of one of the Message's child class
     * */
    public static Message build(String rawMessage) throws Exception {
        // TODO: Safely deal with the wrong format of message.
        // Use CLRT to split for the fields
        String[] fields = rawMessage.split("\r\n");

        for(String field : fields) {
            // TODO: We'll log System.out and System.err in the future
            System.out.println(field);
        }

        if (fields.length != 3) {
            // TODO: We'll log System.out and System.err in the future
            System.err.println("Error message format!");
            throw new RuntimeException("Error number of message fields: "+fields.length);
        }

        // Parse the request line
        return parseFields(fields[0], fields[1], fields[2]);
    }


    /**
     * Static Message build method
     * @param requestField raw request field string
     * @param headerField raw header field string
     * @param payload raw payload field string
     * @return An instance of one of the Message's child class
     * */
    public static Message build(String requestField, String headerField, String payload) throws Exception {
        return parseFields(requestField, headerField, payload);
    }

    /**
     * Parse the message fields.
     * @param requestField the first line of the message field.
     * @param headerField The variables between 1st and 2nd CRLD in the String
     * @param payload The message after the 2nd CRLD
     * @throws Exception the message parsing failed.
     */
    private static Message parseFields(String requestField, String headerField, String payload) throws Exception {
        // Parse the request field and choose a corresponding message constructor.
        switch (requestField) {
            case "CONNECT":
                return new ConnectMessage(headerField, payload);
            case "CONNACK":
                return new ConnAckMessage(headerField, payload);
            case "PUBLISH":
                return new PublishMessage(headerField, payload);
            case "PUBACK":
                return new PubAckMessage(headerField, payload);
            case "SUBSCRIBE":
                return new SubscribeMessage(headerField, payload);
            /* TODO: More types of message methods
            case "SUBACK":
                return Method.SUBACK;
            case "UNSUBSCRIBE":
                return Method.UNSUBSCRIBE;
            case "UNSUBACK":
                return Method.UNSUBACK;
            */
            default:
                throw new RuntimeException("Wrong request method");
        }
    }

    public static boolean isRequestField(String msg) {
        for (Method method: Method.values() ) {
            if ( method.toString().equals( msg.toUpperCase() ) )
                return true;
        }
        return false;
    }
}
