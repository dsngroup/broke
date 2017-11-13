package org.dsngroup.broke.broker.storage;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.protocol.PublishMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Subscriber pool class that stores a map.
 * The map maps the topic to its subscriber set.
 * This can be a singleton.
 * TODO: change to singleton pattern
 * */
public class SubscriberPool {

    /**
     * The subscriber pool
     * A map with topic key to a map of subscribers
     * The map of subscribers maps the ChannelHandlerContext instance to a subscriber context.
     * (Because the ChannelHandlerContext of each subscriber is unique).
     * */
    private Map<String, Map<ChannelHandlerContext, SubscriberContext>> subscriberPool;

    /**
     * Register the subscriber to an interested topic
     * @param topic Interested topic
     * @param groupId consumer group id
     * @param channelCxt subscriber's ChannelHandlerContext {@see ChannelHandlerContext}
     * @return the subscriber's instance
     * */
    public SubscriberContext register(String topic, String groupId, ChannelHandlerContext channelCxt) throws Exception{

        if (subscriberPool.get(topic)==null)
            subscriberPool.put(topic, new ConcurrentHashMap<>());

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
    public void unRegister(String topic, ChannelHandlerContext subscriberChannelCxt) throws Exception{
        // TODO: How to remove a closed subscriber channel context?
        // TODO: e.g. on channel close, call unRegister(channelHandlerContext);
        // TODO: Currently, message sending to inactive subscribers is
        // TODO: avoided by explicitly checking by channel().isActive() method
        subscriberPool.get(topic).remove(subscriberChannelCxt);
    }

    public void sendToSubscribers(PublishMessage publishMessage) throws Exception {

        Map<ChannelHandlerContext,SubscriberContext> subscriberMap
                = subscriberPool.get(publishMessage.getTopic());
        if(subscriberMap != null) {
            for (Map.Entry<ChannelHandlerContext, SubscriberContext> subscriberEntry : subscriberMap.entrySet()) {
                // TODO: this is a test message that prints out the subscriber information
                System.out.println("[publish to subscribers] Hashcode: " + subscriberEntry.getKey().hashCode());

                ChannelHandlerContext subscriberChannelHandlerContext = subscriberEntry.getKey();
                // Send SUBACK to active clients
                if (subscriberChannelHandlerContext.channel().isActive()) {
                    // TODO: header definition & Encapsulation
                    subscriberChannelHandlerContext.writeAndFlush(Unpooled.wrappedBuffer(("SUBACK\r\nQoS:0"
                            + ",Critical-Option:0"
                            + ",Topic:"+publishMessage.getTopic()
                            + "\r\n"+publishMessage.getPayload()
                            + "\r\n").getBytes())).sync();
                }
            }
        }
    }

    /**
     * Not allowed to construct
     * */
    public SubscriberPool() {
        subscriberPool = new ConcurrentHashMap<>();
    }

}

/**
 * The comparator for comparing ChannelHandlerContext in HashMap
 * TODO: remove this comparator because hashmap doesn't need this.
 * TODO: but observer how does the hashmap deals channel handler context as the key.
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
