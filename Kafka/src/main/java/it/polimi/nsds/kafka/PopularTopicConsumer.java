package it.polimi.nsds.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PopularTopicConsumer {
    private static final String topic = "inputTopic";
    private static final String serverAddr = "localhost:9092";

    private static HashMap<String, Integer> map;

    public static void main(String[] args) {
        String groupId = args[0];

        map = new HashMap<>();
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // TODO: complete the implementation by adding code here
        //Consumer
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(true));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(1000));

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());

        KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, Integer> record : records) {
                map.put(record.key(), map.get(record.key()) != null ? map.get(record.key()) +1 : 1);
            }

            int max = 0;
            for (Map.Entry<String,Integer> entry : map.entrySet())
            {
                if (entry.getValue() > max)
                {
                    max = entry.getValue();
                }
            }

            //inefficient but simple
            for (Map.Entry<String,Integer> entry : map.entrySet())
            {
                if (entry.getValue() == max)
                {
                    System.out.print(entry.getKey()+ ", ");
                }
            }
            System.out.print("Max Value: " + max + "\n");
        }

    }
}
