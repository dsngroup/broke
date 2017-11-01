package org.dsngroup.broke.broker.storage;

import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.broker.SubscriberContext;
import org.dsngroup.broke.protocol.PublishMessage;

import java.util.*;

/**
 * Subscriber pool class that stores a map.
 * The map maps the topic to its subscriber set.
 * This can be a singleton.
 * */
public class SubscriberPool {

    /**
     * The subscriber pool
     * A map with topic key to a map of subscribers
     * The map of subscribers maps the ChannelHandlerContext instance to a subscriber context.
     * (Because the ChannelHandlerContext of each subscriber is unique).
     * */
    private static Map<String, Map<ChannelHandlerContext, SubscriberContext>> subscriberPool = new HashMap();

    /**
     * Register the subscriber to an interested topic
     * @param topic Interested topic
     * @param groupId consumer group id
     * @param channelCxt subscriber's ChannelHandlerContext {@see ChannelHandlerContext}
     * @return the subscriber's instance
     * */
    public static SubscriberContext register(String topic, String groupId, ChannelHandlerContext channelCxt) throws Exception{

        if (subscriberPool.get(topic)==null)
            subscriberPool.put(topic, new TreeMap<>( new ChannelHandlerComparator()));

        SubscriberContext subscriber = new SubscriberContext(topic, groupId, channelCxt);

        subscriberPool.get(topic).put(channelCxt, subscriber);

        // TODO: remove this test
        // System.out.println( "Test the subscriber pool: \n"+subscriberPool.get(topic).get(channelCxt).getClass().toString() );
        return subscriber;
    }

    /**
     * Unregister the subscriber from the interested topic keyed by subscriber's ChannelHandlerContext
     * @param topic topic to unsubscribe
     * @param subscriberChannelCxt the subscriber's ChannelHandlerContext
     * @Exception no such topic or subscriber
     * */
    public static void unRegister(String topic, ChannelHandlerContext subscriberChannelCxt) throws Exception{
        subscriberPool.get(topic).remove(subscriberChannelCxt);
    }

    public static void sendToSubscribers(PublishMessage publishMessage) {

        for (Map.Entry<ChannelHandlerContext, SubscriberContext> subscriberEntry : subscriberPool.get(publishMessage.getTopic()).entrySet() ) {
            // TODO: this is a test message sent to the subscriber
            subscriberEntry.getKey().writeAndFlush(publishMessage.getPayload());
            // TODO: this is a test message that prints out the subscriber information
            System.out.println( "[publish to subscribers] Hashcode: "+subscriberEntry.getKey().hashCode() );
        }
    }

    /**
     * Not allowed to construct
     * */
    private SubscriberPool() {}

}

/**
 * The comparator for comparing ChannelHandlerContext in HashMap
 * */
class ChannelHandlerComparator implements Comparator<ChannelHandlerContext> {
    @Override
    public int compare(ChannelHandlerContext o1, ChannelHandlerContext o2) {
        if (o1 == o2)
            return 0;
        else
            return o1.hashCode()-o2.hashCode();
    }
}
