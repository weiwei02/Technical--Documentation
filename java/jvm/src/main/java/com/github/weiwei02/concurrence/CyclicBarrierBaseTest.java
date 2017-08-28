package com.github.weiwei02.concurrence;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**  周期障碍  可以循环的实现线程要一起去做某个任务
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/8/28
 */
public class CyclicBarrierBaseTest {
    static class Service{
        //拥有10个资源许可的信号量对象
        private CyclicBarrier cyclicBarrier = new CyclicBarrier(5, () -> System.out.println("所有的线程都执行完了"));
        void testMethod(){
            try {
                System.out.println("执行到" + Thread.currentThread().getName());
                cyclicBarrier.await();
                System.out.println("执行完" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    static class MyThread extends Thread{
        private Service service;
        MyThread(Service service){
            super();
            this.service = service;
        }

        @Override
        public void run() {
            service.testMethod();
        }
    }

    public static void main(String[] args) {
        Service service = new Service();
        for (int i = 0; i < 10; i++) {
            MyThread myThread = new MyThread(service);
            myThread.setName("线程" + i);
            myThread.start();
        }
    }


}

