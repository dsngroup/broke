package org.dsngroup.broke.protocol.deprecated;

/**
 * The header class of the CONNECT message
 * The class has an addition topic attribute than the basic message.
 * */
public class ConnectMessageHeader extends MessageHeader {

    /**
     * The constructor, construct the message header by a raw header string
     * @param rawHeader raw header string
     * @throws RuntimeException Wrong option fields.
     * */
    public ConnectMessageHeader(String rawHeader) throws Exception {
        String[] fields = rawHeader.split(",");
        for (int i = 0; i < fields.length; i++) {
            String[] optionSplit = fields[i].split(":", 2);
            // TODO: There may be more options
            switch (optionSplit[0].toUpperCase()) {
                default:
                    setOptions(optionSplit);
            }
        }
    }
}
