package com.github.weiwei02.concurrence;

import java.util.concurrent.CountDownLatch;

/**  相当于门闩， 当等待的线程达到门闩的限定值时，让多个线程同时执行竞争资源
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/8/28
 */
public class CountDownLatchBaseTest {
    public static void main(String[] args) throws InterruptedException {
        Serveice service = new Serveice();
        MyThread a = new MyThread(service);
        a.start();
        Thread.sleep(5000);
        System.out.println("休眠完成");
        service.downMethod();
        MyThread b = new MyThread(service);
        b.start();

    }
}
class Serveice{
    private CountDownLatch countDownLatch = new CountDownLatch(10);

    public void testMethod(){
        System.out.println("A");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("B");
    }

    public void downMethod(){
        System.out.println("down");
        countDownLatch.countDown();
    }
}


class MyThread extends Thread{
    private Serveice service;
    public MyThread(Serveice service){
        super();
        this.service = service;
    }

    @Override
    public void run() {
        service.testMethod();
    }
}
