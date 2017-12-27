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

package org.dsngroup.broke;

import org.apache.flink.api.common.functions.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsngroup.broke.source.BrokeSource;

import java.io.InputStream;
import java.util.Properties;

/**
 * The Flink word count benchmark that receives messages from the broker server.
 */
public class BrokeWordCount {

    public static final Logger logger = LoggerFactory.getLogger(BrokeWordCount.class);

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();

        try {
            InputStream inputStream = BrokeWordCount.class.getClassLoader()
                    .getResourceAsStream("FlinkClient.properties");
            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        String serverAddress = properties.getProperty("SERVER_ADDRESS");
        int serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        String subscribeTopic = properties.getProperty("SUBSCRIBE_TOPIC");
        int groupId = Integer.parseInt(properties.getProperty("GROUP_ID"));
        if (args.length > 0) {
            serverAddress = args[0].split(":")[0];
            serverPort = Integer.parseInt(args[0].split(":")[1]);
        }

        // Broke data source.
        DataStream<String> dataStream =
                env.addSource(new BrokeSource(serverAddress, serverPort, subscribeTopic, groupId));

        // Windowed word count data stream.
        DataStream<WordWithCount> windowCount = dataStream
                .flatMap(new FlatMapFunction<String, WordWithCount>() {
                    @Override
                    public void flatMap(String value, Collector<WordWithCount> out) throws Exception {
                        for(String word: value.split("\\s")) {
                            out.collect(new WordWithCount(word, 1L));
                        }
                    }
                })
                .keyBy("word")
                .timeWindow(Time.seconds(5), Time.seconds(1))
                .reduce(new ReduceFunction<WordWithCount>() {
                    @Override
                    public WordWithCount reduce(WordWithCount a, WordWithCount b) throws Exception {
                        return new WordWithCount(a.word, a.count + b.count);
                    }
                });

        // Print the word count result.
        // windowCount.print().setParallelism(1);

        try {
            // Execute the topology and block here.
            env.execute("Broke word count");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Data structure of the word count in POJO.
     */
    public static class WordWithCount {

        public String word;
        public long count;

        public WordWithCount(String word, long count) {
            this.word = word;
            this.count = count;
        }

        public WordWithCount() {}

        @Override
        public String toString() {
            return word + " : " + count;
        }
    }
}
