package it.polimi.nsds.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class AtMostOncePrinter {
    private static final String topic = "inputTopic";
    private static final String serverAddr = "localhost:9092";
    private static final int threshold = 500;

    private static final String autoCommit = "false";

    private static final String offsetResetStrategy = "latest";


    public static void main(String[] args) {
        String groupId = args[0];

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // TODO: complete the implementation by adding code here
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());

        KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        while(true)
        {
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            consumer.commitSync();
            //could fail from here onward
            for (final ConsumerRecord<String, Integer> record : records) {
                if (record.value() > threshold){
                    System.out.print("Consumer group: " + groupId + "\t");
                    System.out.println("Partition: " + record.partition() +
                            "\tOffset: " + record.offset() +
                            "\tKey: " + record.key() +
                            "\tValue: " + record.value()
                    );
                }
            }
        }
    }
}
