package com.githun.weiwei02.kafkatest.oldConsumerAPI;

/**启动Consumer主类
 * 1. topicCountMap.put(topic, new Integer(a_numThreads)) 是告诉Kafka我有多少个线程来处理消息。

 (1). 这个线程数必须是小等于topic的partition分区数；可以通过./kafka-topics.sh --describe --zookeeper "172.16.49.173:2181" --topic "producer_test"命令来查看分区的情况
 (2). kafka会根据partition.assignment.strategy指定的分配策略来指定线程消费那些分区的消息；这里没有单独配置该项即是采用的默认值range策略（按照阶段平均分配）。比如分区有10个、线程数有3个，则线程 1消费0,1,2,3，线程2消费4,5,6,线程3消费7,8,9。另外一种是roundrobin(循环分配策略)，官方文档中写有使用该策略有两个前提条件的，所以一般不要去设定。
 (3). 经过测试：consumerMap.get(topic).size()，应该是获得的目前该topic有数据的分区数
 (4). stream即指的是来自一个或多个服务器上的一个或者多个partition的消息。每一个stream都对应一个单线程处理。因此，client能够设置满足自己需求的stream数目。总之，一个stream也许代表了多个服务器partion的消息的聚合，但是每一个 partition都只能到一个stream
 2. Executors.newFixedThreadPool(a_numThreads)是创建一个创建固定容量大小的缓冲池：每次提交一个任务就创建一个线程，直到线程达到线程池的最大大小。线程池的大小一旦达到最大值就会保持不变，如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。

 3. props.put(“auto.offset.reset”, “smallest”) 是指定从最小没有被消费offset开始；如果没有指定该项则是默认的为largest，这样的话该consumer就得不到生产者先产生的消息。
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/1
 */
import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.Properties;
import java.util.concurrent.ExecutorService;
        import java.util.concurrent.Executors;
        import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import kafka.consumer.ConsumerConfig;
        import kafka.consumer.KafkaStream;
        import kafka.javaapi.consumer.ConsumerConnector;
public class ConsumerGroup {
    private final ConsumerConnector consumer;
    private final String topic;
    private ExecutorService executor;
    private static Logger LOG = LoggerFactory.getLogger(ConsumerGroup.class);
    public ConsumerGroup(String a_zookeeper, String a_groupId, String a_topic) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig(a_zookeeper, a_groupId));
        this.topic = a_topic;
    }
    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("请输入zookeeper集群地址(如zk1:2181,zk2:2181,zk3:2181)：");
//        String zooKeeper = sc.nextLine();
        String zooKeeper = "ubuntu:32782";
//        System.out.println("请输入指定的消费group名称：");
//        String groupId = sc.nextLine();
//        System.out.println("请输入指定的消费topic名称：");
//        String topic = sc.nextLine();
        String topic = "test";
//        System.out.println("请输入指定的消费处理线程数：");
        int threads = 10;
        LOG.info("Starting consumer kafka messages with zk:" + zooKeeper + " and the topic is " + topic);
        ConsumerGroup example = new ConsumerGroup("ubuntu:32782", "consumer2", "test");
        example.run(threads);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {

        }
        // example.shutdown();
    }

    private void shutdown() {
        // TODO Auto-generated method stub
        if (consumer != null)
            consumer.shutdown();
        if (executor != null)
            executor.shutdown();
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                LOG.info("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            LOG.info("Interrupted during shutdown, exiting uncleanly");
        }
    }

    private void run(int a_numThreads) {
        // TODO Auto-generated method stub
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        //
        executor = Executors.newFixedThreadPool(a_numThreads);

        // now create an object to consume the messages
        //
        int threadNumber = 0;
        LOG.info("the streams size is "+streams.size());
        for (final KafkaStream stream : streams) {
            executor.submit(new Consumerwork(stream, threadNumber));
            //      consumer.commitOffsets();
            threadNumber++;
        }

    }

    private ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        // TODO Auto-generated method stub
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "60000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");
//      props.put("rebalance.max.retries", "5");
//      props.put("rebalance.backoff.ms", "15000");
        return new ConsumerConfig(props);
    }

}