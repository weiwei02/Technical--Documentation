package com.githun.weiwei02.kafkatest;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/1
 */
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartitionTest {
    private static Logger LOG = LoggerFactory.getLogger(PartitionTest.class);

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Properties props = new Properties();
//        props.put("bootstrap.servers", "192.168.17.130:9092;172.16.49.173:9093");
        props.put("bootstrap.servers", "ubuntu:9092");

        props.put("retries", 0);
        // props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        // props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("partitioner.class", "com.goodix.kafka.MyPartition"); //自定义分区
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
//        ProducerRecord<String, String> record = new ProducerRecord<String, String>("test", "2223132132",
//                "test23_60");
        int i =1;
        while (i < 10){
            ProducerRecord<String, String> record = new ProducerRecord<String, String>("test", Math.random()* 1000 + ".message");
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    // TODO Auto-generated method stub
                    if (e != null)
                        LOG.error("the producer has a error:" + e.getMessage());
                    else {
                        LOG.info("The offset of the record we just sent is: " + metadata.offset());
                        LOG.info("The partition of the record we just sent is: " + metadata.partition());
                    }

                }
            });
            i++;
        }

        try {
            Thread.sleep(1000);
            producer.close();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

}