package org.dsngroup.broke;

import org.dsngroup.broke.client.BlockClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(SampleSubscribe.class);

    public static void main(String[] args) {
        // The args[0] = address, args[1] = port
        // TODO: better parsed from one string.
        try {
            BlockClient blockClient = new BlockClient(args[0], Integer.parseInt(args[1]));
            blockClient.connect(0, 0,"connect from a subscriber");
            blockClient.subscribe("Foo", 0, 0, "group0", "bar");
            for (int i = 1; ; i++) {
                if (System.in.read()!=0){
                    blockClient.subscribe("Foo", 0, 0, "group"+String.valueOf(i), "bar");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Not enough arguments");
            System.exit(1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("Wrong settings of message options");
            System.exit(1);
        }
    }
}
