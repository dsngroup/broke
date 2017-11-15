package org.dsngroup.broke.protocol.deprecated;

/**
 * The root class of all message headers
 * All message headers contain QoS and CriticalOption attribute.
 * // TODO: No message header should be created using this class, maybe change to protected type
 * */
public class MessageHeader {
    protected QoS qos;
    protected CriticalOption criticalOption;

    /**
     * Attribute initializer function for QoS and CriticalOption.
     * This function is currently called by child constructors only.
     * TODO: should this set to a constructor? But can a constructor being called multiple times?
     * @param optionSplit A key-value option pair
     * */
    protected void setOptions(String[] optionSplit){
        switch (optionSplit[0].toUpperCase()) {
            case "QOS":
                qos = QoS.values()[Integer.parseInt(optionSplit[1])];
                break;
            case "CRITICAL-OPTION":
                criticalOption = CriticalOption.values()[Integer.parseInt(optionSplit[1])];
                break;
            default:
                System.err.println("No such field: "+optionSplit[0]);
                throw new RuntimeException("Wrong option fields.");
        }
    }

    /**
     * Get the qos
     */
    public QoS getQos() {
        return qos;
    }

    /**
     * Get the criticalOption
     */
    public CriticalOption getCriticalOption() {
        return criticalOption;
    }
}
