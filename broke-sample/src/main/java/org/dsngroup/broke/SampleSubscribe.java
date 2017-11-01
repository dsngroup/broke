package org.dsngroup.broke;

import org.dsngroup.broke.client.BlockClient;

public class SampleSubscribe {

    public static void main(String[] args) {
        // The args[0] = address, args[1] = port
        // TODO: better parsed from one string.
        try {
            BlockClient blockClient = new BlockClient(args[0], Integer.parseInt(args[1]));
            blockClient.subscribe("Foo", 0, 0, "group0", "bar");
            for (int i = 1; ; i++) {
                if (System.in.read()!=0){
                    blockClient.subscribe("Foo", 0, 0, "group"+String.valueOf(i), "bar");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // TODO: We'll log System.out and System.err in the future
            System.out.println("Not enough arguments");
            System.exit(1);
        } catch (Exception e) {
            // TODO: use log instead of printStackTrace()
            e.printStackTrace();
            // TODO: We'll log System.out and System.err in the future
            System.out.println("Wrong settings of message options");
            System.exit(1);
        }
    }
}
