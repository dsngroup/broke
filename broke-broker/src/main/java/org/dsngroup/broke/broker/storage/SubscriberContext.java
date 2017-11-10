package org.dsngroup.broke.broker.storage;

import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * The subscriber information class
 * */
public class SubscriberContext {

    private String topic;
    private ChannelHandlerContext subscriberChannelCxt;
    private String groupId;

    /**
     * The constructor
     * @param subscriberChannelCxt channel handler context of this subscriber {@see ChannelHandlerContext}
     * @param groupId the consumer group id
     * */
    public SubscriberContext(String topic, String groupId, ChannelHandlerContext subscriberChannelCxt) {
        this.topic = topic;
        this.groupId = groupId;
        this.subscriberChannelCxt = subscriberChannelCxt;
    }

    /**
     * Override hashCode() for HashSet
     * @return hash code
     * */
    @Override
    public int hashCode() {
        return Objects.hash(groupId, subscriberChannelCxt);
    }

    /**
     * Overrride equals() for Set comparator
     * Compare using the reference of subscriberChannelHandlerContext
     * @param obj Other object
     * */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SubscriberContext other = (SubscriberContext) obj;
        if(this.subscriberChannelCxt != other.subscriberChannelCxt)
            return false;
        return true;
    }

}
