package com.githun.weiwei02.kafkatest.oldConsumerAPI;

/**使用Old SimpleConsumerAPI编写consumer

 这是一个更加底层和复杂的API
 使用的场景

 由于使用该API需要自己控制的项比较多，也比较复杂，官方给出了一些合适的适用场景，也可以理解成为这些场景是High Level Consumer API 不能够做到的
 1. 针对一个消息读取多次
 2. 在一个process中，仅仅处理一个topic中的一个partitions
 3. 使用事务，确保每个消息只被处理一次

 需要处理的事情

 1. 必须在程序中跟踪offset值
 2. 必须找出指定Topic Partition中的lead broker
 3. 必须处理broker的变动

 使用SimpleConsumer的步骤

 首先，你必须知道读哪个topic的哪个partition
 然后，找到负责该partition的broker leader，从而找到存有该partition副本的那个broker
 再者，自己去写request并fetch数据
 最终，还要注意需要识别和处理broker leader的改变
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/1
 */
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleExample {
    private static Logger LOG = LoggerFactory.getLogger(SimpleExample.class);
    public static void main(String args[]) {
        SimpleExample example = new SimpleExample();
        Scanner sc = new Scanner(System.in);
//        System.out.println("请输入broker节点的ip地址(如172.16.49.173)");
        String brokerIp = "ubuntu";
        List<String> seeds = new ArrayList<String>();
        seeds.add(brokerIp);
//        System.out.println("请输入broker节点端口号(如9092)");
//        int port = Integer.parseInt( sc.nextLine());
        int port = 9092;
//        System.out.println("请输入要订阅的topic名称(如test)");
//        String topic = sc.nextLine();
        String topic = "test";
        System.out.println("请输入要订阅要查找的分区(如0)");
//        int partition = Integer.parseInt( sc.nextLine());
        int partition = 0;
        System.out.println("请输入最大读取消息数量(如10000)");
//        long maxReads = Long.parseLong( sc.nextLine());
        long maxReads = 1000L;

        try {
            example.run(maxReads, topic, partition, seeds, port);
        } catch (Exception e) {
            LOG.error("Oops:" + e);
            e.printStackTrace();
        }
    }

    private List<String> m_replicaBrokers = new ArrayList<String>();

    public SimpleExample() {
        m_replicaBrokers = new ArrayList<String>();
    }

    public void run(long a_maxReads, String a_topic, int a_partition, List<String> a_seedBrokers, int a_port) throws Exception {
        // find the meta data about the topic and partition we are interested in
        //获取指定Topic partition的元数据
        PartitionMetadata metadata = findLeader(a_seedBrokers, a_port, a_topic, a_partition);
        if (metadata == null) {
            LOG.error("Can't find metadata for Topic and Partition. Exiting");
            return;
        }
        if (metadata.leader() == null) {
            LOG.error("Can't find Leader for Topic and Partition. Exiting");
            return;
        }
        String leadBroker = metadata.leader().host();
        String clientName = "Client_" + a_topic + "_" + a_partition;
        SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
        long readOffset = getLastOffset(consumer,a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(), clientName);

        int numErrors = 0;
        while (a_maxReads > 0) {
            if (consumer == null) {
                consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
            }
            FetchRequest req = new FetchRequestBuilder()
                    .clientId(clientName)
                    .addFetch(a_topic, a_partition, readOffset, 100000) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                    .build();
            FetchResponse fetchResponse = consumer.fetch(req);

            if (fetchResponse.hasError()) {
                numErrors++;
                // Something went wrong!
                short code = fetchResponse.errorCode(a_topic, a_partition);
                LOG.error("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                if (numErrors > 5) break;
                if (code == ErrorMapping.OffsetOutOfRangeCode())  {
                    // We asked for an invalid offset. For simple case ask for the last element to reset
                    readOffset = getLastOffset(consumer,a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(), clientName);
                    continue;
                }
                consumer.close();
                consumer = null;
                leadBroker = findNewLeader(leadBroker, a_topic, a_partition, a_port);
                continue;
            }
            numErrors = 0;

            long numRead = 0;
            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
                long currentOffset = messageAndOffset.offset();
                if (currentOffset < readOffset) {
                    LOG.error("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
                    continue;
                }
                readOffset = messageAndOffset.nextOffset();
                ByteBuffer payload = messageAndOffset.message().payload();

                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                LOG.info("the messag's offset is :"+String.valueOf(messageAndOffset.offset()) + " and the value is :" + new String(bytes, "UTF-8"));
                numRead++;
                a_maxReads--;
            }

            if (numRead == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
        if (consumer != null) consumer.close();
    }

    public static long getLastOffset(SimpleConsumer consumer, String topic, int partition,
                                     long whichTime, String clientName) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(
                requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            LOG.error("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition) );
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }
    /**
     * 找一个leader broker
     * 遍历每个broker，取出该topic的metadata，然后再遍历其中的每个partition metadata，如果找到我们要找的partition就返回
     * 根据返回的PartitionMetadata.leader().host()找到leader broker
     * @param a_oldLeader
     * @param a_topic
     * @param a_partition
     * @param a_port
     * @return
     * @throws Exception
     */
    private String findNewLeader(String a_oldLeader, String a_topic, int a_partition, int a_port) throws Exception {
        for (int i = 0; i < 3; i++) {
            boolean goToSleep = false;
            PartitionMetadata metadata = findLeader(m_replicaBrokers, a_port, a_topic, a_partition);
            if (metadata == null) {
                goToSleep = true;
            } else if (metadata.leader() == null) {
                goToSleep = true;
            } else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
                // first time through if the leader hasn't changed give ZooKeeper a second to recover
                // second time, assume the broker did recover before failover, or it was a non-Broker issue
                //
                goToSleep = true;
            } else {
                return metadata.leader().host();
            }
            if (goToSleep) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
        LOG.error("Unable to find new leader after Broker failure. Exiting");
        throw new Exception("Unable to find new leader after Broker failure. Exiting");
    }
    /**
     *
     * @param a_seedBrokers
     * @param a_port
     * @param a_topic
     * @param a_partition
     * @return
     */
    private PartitionMetadata findLeader(List<String> a_seedBrokers, int a_port, String a_topic, int a_partition) {
        PartitionMetadata returnMetaData = null;

        loop:
        for (String seed : a_seedBrokers) { //遍历每个broker
            SimpleConsumer consumer = null;
            try {
                // 创建Simple Consumer，
                consumer = new SimpleConsumer(seed, a_port, 100000, 64 * 1024, "leaderLookup");
                List<String> topics = Collections.singletonList(a_topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                //发送TopicMetadata Request请求
                kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);
                //取到Topic的Metadata
                List<TopicMetadata> metaData = resp.topicsMetadata();
                //遍历每个partition的metadata
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        // 判断是否是要找的partition
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            //找到就返回
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.info("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        if (returnMetaData != null) {
            m_replicaBrokers.clear();
            for (kafka.cluster.BrokerEndPoint replica : returnMetaData.replicas()) {
                m_replicaBrokers.add(replica.host());
            }
        }
        return returnMetaData;
    }
}