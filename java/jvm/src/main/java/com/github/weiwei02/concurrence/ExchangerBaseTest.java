package com.github.weiwei02.concurrence;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 线程数据交换测试
 * Exchanger 主要用于在两个线程中交换数据 其 exchange() 方法被调用后等待其他线程来获取数据，如果一直没有其他线程获取则阻塞等待
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/8/28
 */
public class ExchangerBaseTest {
    public static void main(String[] args) {
        Exchanger<String> exchanger = new Exchanger<>();
        ThreadA A = new ThreadA(exchanger, "A");
        ThreadA B = new ThreadA(exchanger, "B");
        A.start();
        B.start();
    }


    static class ThreadA extends Thread{
        private Exchanger<String> exchanger;
        private String name;
        ThreadA(Exchanger<String> exchanger,String name){
            super();
            this.exchanger = exchanger;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                System.out.println(name);
                System.out.println("在线程" + name +"中得到的值=" + exchanger.exchange("中国人" + name));
                System.out.println(name + " end");
            } catch (InterruptedException e) {
                e.printStackTrace();
//            } catch (TimeoutException e) {
//                e.printStackTrace();
//            }
            }
        }
    }
}
