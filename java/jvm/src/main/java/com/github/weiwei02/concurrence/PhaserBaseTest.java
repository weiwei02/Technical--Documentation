package com.github.weiwei02.concurrence;

import java.util.concurrent.Phaser;

/** 移项器
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/8/28
 */
public class PhaserBaseTest {
    static class Service{
        //拥有10个资源许可的信号量对象
        private Phaser phaser = new Phaser(3){
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println(String.format("有一个线程到达了  目前屏障数 %d, 目前已注册屏障数 %d", phase, registeredParties) );
                return true;
            }
        };
        void testMethod(){
            System.out.println("1执行" + Thread.currentThread().getName());
            phaser.arrive();
            System.out.println("2执行" + Thread.currentThread().getName());
//            System.out.println("移相器被用过 " + phaser.getPhase());

            phaser.arriveAndAwaitAdvance();
            try {
                phaser.awaitAdvance(2);// 当正在移相器的屏障数为2 则执行当前线程下面的代码
                phaser.awaitAdvanceInterruptibly(2);// 当正在移相器的屏障数不为2 则执行当前线程下面的代码
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("3执行" + Thread.currentThread().getName());
        }
        void testMethod2() throws InterruptedException {
            System.out.println("移相器已注册的障碍数" + phaser.getRegisteredParties());
//            phaser.register();
            phaser.bulkRegister(10);
            System.out.println("移相器已注册的障碍数" + phaser.getRegisteredParties());



            System.out.println("4执行" + Thread.currentThread().getName());
//            Thread.sleep(5000);
            phaser.arriveAndAwaitAdvance();
//            phaser.arriveAndDeregister();
            System.out.println("5执行" + Thread.currentThread().getName());
        }
    }

    static class MyThread1 extends Thread{
        private Service service;
        MyThread1(Service service){
            super();
            this.service = service;
        }

        @Override
        public void run() {
            service.testMethod();
        }
    }static class MyThread2 extends Thread{
        private Service service;
        MyThread2(Service service){
            super();
            this.service = service;
        }

        @Override
        public void run() {
            try {
                service.testMethod2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Service service = new Service();
            MyThread1 myThread1 = new MyThread1(service);
            MyThread1 myThread2 = new MyThread1(service);
            MyThread2 myThread3 = new MyThread2(service);
            myThread1.setName("线程1" );
            myThread2.setName("线程2" );
            myThread3.setName("线程3" );


            myThread1.start();
            myThread2.start();
            myThread3.start();
    }

}
