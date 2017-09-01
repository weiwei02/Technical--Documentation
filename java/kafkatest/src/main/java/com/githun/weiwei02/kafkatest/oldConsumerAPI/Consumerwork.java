package com.githun.weiwei02.kafkatest.oldConsumerAPI;

/**  使用Old Consumer High Level API编写consumer
 * 具体处理消息的类
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/1
 */
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;


public class Consumerwork implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(Consumerwork.class);
    @SuppressWarnings("rawtypes")
    private KafkaStream m_stream;
    private int m_threadNumber;
    @SuppressWarnings("rawtypes")
    public Consumerwork(KafkaStream a_stream,int a_threadNumber) {
        // TODO Auto-generated constructor stub
        m_threadNumber = a_threadNumber;
        m_stream = a_stream;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        // TODO Auto-generated method stub
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext())
            try {
                MessageAndMetadata<byte[], byte[]> thisMetadata=it.next();
                String jsonStr = new String(thisMetadata.message(),"utf-8") ;
                LOG.info("Thread " + m_threadNumber + ": " +jsonStr);
                LOG.info("partion"+thisMetadata.partition()+",offset:"+thisMetadata.offset());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
}