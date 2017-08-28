package com.github.weiwei02.concurrence;

import java.util.Timer;
import java.util.concurrent.Semaphore;

/**信号量同步基本测试demo
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/8/28
 */
public class SemaphoreBaseTest {
    /**
     * 测试结果：
     * Thread-0begin timer=1503851316569
     Thread-0停止了3秒
     Thread-1begin timer=1503851316571
     Thread-1停止了2秒
     Thread-4begin timer=1503851316571
     Thread-4停止了3秒
     Thread-5begin timer=1503851316571
     Thread-5停止了6秒
     Thread-2begin timer=1503851316572
     Thread-2停止了9秒
     Thread-1end timer=1503851318936
     Thread-9begin timer=1503851318936
     Thread-9停止了5秒
     Thread-0end timer=1503851320229
     Thread-8begin timer=1503851320229
     Thread-8停止了3秒
     Thread-4end timer=1503851320538
     Thread-3begin timer=1503851320538
     Thread-3停止了4秒
     Thread-5end timer=1503851322839
     Thread-6begin timer=1503851322839
     Thread-6停止了7秒
     Thread-8end timer=1503851323979
     Thread-7begin timer=1503851323979
     Thread-7停止了4秒
     Thread-9end timer=1503851324051
     Thread-3end timer=1503851324717
     Thread-2end timer=1503851325818
     Thread-7end timer=1503851328359
     Thread-6end timer=1503851330795
     * */
    public static void main(String[] args) {
        Service service = new Service();
        ThreadA[] threadA = new ThreadA[10];
        for (int i = 0; i < 10; i++) {
            threadA[i] = new ThreadA(service);
            threadA[i].start();
        }
    }
}

class Service{
    //拥有10个资源许可的信号量对象
    private Semaphore semaphore = new Semaphore(10);
    void testMethod(){
        try {
            semaphore.acquireUninterruptibly(2);
            System.out.println(Thread.currentThread().getName() + "begin timer=" + System.currentTimeMillis());
            int sleepValue = (int) (Math.random() * 10000);
            System.out.println(Thread.currentThread().getName() + "停止了" + (sleepValue / 1000) + "秒");
            Thread.sleep(sleepValue);
            System.out.println(Thread.currentThread().getName() + "end timer=" + System.currentTimeMillis());
            semaphore.release(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ThreadA extends Thread{
    private Service service;

    public ThreadA(Service service){
        super();
        this.service = service;
    }

    @Override
    public void run() {
        service.testMethod();
    }
}

