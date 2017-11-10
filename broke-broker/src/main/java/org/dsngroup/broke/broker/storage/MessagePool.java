/*
 * Copyright (c) 2017 original authors and authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dsngroup.broke.broker.storage;

import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MessagePool class, used as singleton class.
 * TODO: change to singleton pattern
 */
public class MessagePool {

    // TODO: Assignable pool size
    // TODO: Extends this to be offset based data structure.
    // TODO: May consider to replace ConcurrentHashMap into more performant data structure.
    private static Map<String, ArrayList<String>> messagePool = new ConcurrentHashMap<>();

    /**
     * Insert storage on a specific topic.
     * @param topic the topic(key) of the broker.
     * @param content the storage of the associated topic.
     */
    public static void putContentOnTopic(String topic, String content) {

        // Initialize the array list for the first-time topic
        if (messagePool.get(topic) == null)
            messagePool.put(topic, new ArrayList());

        // Ignore the return value
        messagePool.get(topic).add(content);

        // TODO: remove this test
        // System.out.println( "Add message:\ntopic"+topic+"payload: "+messagePool.get(topic).get( messagePool.get(topic).size()-1 ) );
    }

    /**
     * Get storage from a specific topic.
     * @param topic the topic(key) of the broker.
     * @return the storage of the associated topic.
     */
    public static String getContentFromTopic(String topic) {
        // return the last one by default
        return messagePool.get(topic).get( messagePool.get(topic).size()-1 );
    }

    private MessagePool() {}

}
