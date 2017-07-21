package com.github.weiwei02.jvm.jmm.volatile_test;

/**
 * volatile 线程安全性测试
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/7/2
 */
public class VolatileTest {
    public static volatile int race = 0;
    public static final int THREAD_COUNT=20;

    public static void main(String[] args) {
        Thread threads[] = new Thread[THREAD_COUNT];
        System.out.println(race);
        for (int i =0; i < THREAD_COUNT; i++){
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000 ; j++) {
                    increase();
                }
            });
            threads[i].start();
        }


        //等待所有累加线程都结束
        while (Thread.activeCount() > 1){
            Thread.yield();
        }

        //等待所有线程执行完毕之后，打印race的最终值
        System.out.println(race);
    }

    private static void increase() {
        race++;
    }
}
