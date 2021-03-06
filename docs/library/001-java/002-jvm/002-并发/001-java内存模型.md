# Technical--Documentation
共享技术文档

##  项目简介
  为为日常工作和学习的总结。

### 技术文档主站简介

   主站文档是我在前人的脚步上对研发的学习和总结纪录，在以后的日子里，我会将自己研发工作中所可能遇见问题和心得在这里纪录下来，公开的分享。世界的存在很美好，开源的存在很精彩。从今天起，我便借助开源的力量，向改变人类的生活方式这条路迈进。

#####  点击这里直接进入[为为技术文档主站](https://weiwei02.github.io/Technical--Documentation/)，或者访问 [https://weiwei02.github.io/Technical--Documentation/](https://weiwei02.github.io/Technical--Documentation/)

##### 与文档配套的 [Technical--Documentation](https://github.com/weiwei02/Technical--Documentation)项目的地址是 https://github.com/weiwei02/Technical--Documentation

### 非原创声明
   >本文并非我的原创文章，而是我学习jvm时的笔记。文中的材料与数据大部分来自于其它资料，详细请查看本文的引用章节。


## JAVA内存模型

   犹记得大学时操作系统课上，我们迷茫的眼神注视着带着厚眼镜教授向我们一遍遍的强调，一个程序最少有一个进程组成，进程是操作系统提供独立资源供应用程序运行的基本单位。另外老师向我们讲到，为了更好的提高计算机的并行计算能力，计算机科学家们又设计了线程。线程是比进程更小的单位，一个进程可以由多个线程组成。同时线程也是在得到cpu时间片时可运行的最小的单位。尤记得在这些理论基础下，我慢慢的学会了使用C在LINUX环境下使用多进程和多线程进行编程。这些并发API都是LINUX提供的标准API，程序在编译链接之后可以直接调用通过系统内核创建进程或线程，在类UNIX操作系统下这样去做可以让程序有更高的性能。但是这种编程方式所编写出来的代码是与操作系统绑定的，我在linux下明明可以完美运行的代码，在windows下连编译甚至都做不到。除非是用 windows API重新把与操作系统进行交互的那些代码给替换掉，否则就不要想着让程序去跨平台运行了。  
   在进行并发编程时相对于C，我更喜欢java的编程体验。java消除操作系统之间的差异，原生的对多线程应用提供了很好的支持，特别是在jdk1.5之后，jdk还提供了currency包，更好的让程序员们无需去关注并发的难点与细节，更专心的关注应用的业务需求实现。  
   在多线程编程中最长遇到的问题就是线程安全问题，其中又以内存中的数据安全问题最为常见(这个数据安全指的是发生脏读、幻读等并发编程中会遇到的错误)。为了消除不同硬件和操作系统对内存操作的差异，在硬件设备和操作系统的内存模型之上，java虚拟机规范定义了一种java内存模型(JAVA Memory Model,简称JMM)，将内存分为了工作内存和主内存。JMM主要定义了JVM中在内存中操作变量的规则和细节，用来解决在并发竞争对变量操作时可能会发生的各种问题。JMM完全兼容CPU的多级cache机制，并且支持编译器的代码重排序。  
   注意：JMM内存模型的概念不同于jvm中6大内存区域的概念，两者不可强行混为一谈。

### 主内存和工作内存
  JMM规定了所有的全局变量都存于主内存(Main Memory)中，一般情况下这些全局变量都会被保存到堆里。每个线程都有自己的工作内存(Working Memory)，工作内存中会保存当前线程所需用到的主内存中全局变量的拷贝和自己的局部变量。一般情况下工作内存指的是栈内存，从物理上来讲，工作内存一般情况下都会工作于cpu的cache里。一个线程对全局变量的任何操作必须在自己的工作内存中进行，不允许直接操作工作内存。不同线程不能访问对方的工作内容，只能通过主内存进行数据交换。

  ![JMM关系图]()

  > 工作内存在对主内存中变量做拷贝时，如果变量是基本类型的，则会拷贝其值；如果是引用类型的，那么仅仅会拷贝其引用

### JMM内存操作
JMM定义了8种主内存与工作内存之间的具体交互操作，虚拟机在实现JMM时必须保证每种内存操作都是原子的。表1详细列出了JMM的8种内存操作。

| 指令    |  操作名  | 作用区域 | 描述
| :------ | :------ | :------ |
| lock    | 锁定    | 主内存   | 把一个变量标识为一个线程独占的状态
| unlock  | 解锁    | 主内存   | 把一个处于锁定状态的变量释放出来
| read    | 读取    | 主内存   | 把一个变量的值从主内存读取到工作内存中，以便于随后的load指令使用
| load    | 载入    | 工作内存 | 把read指令从主内存中读取的变量值放入到工作内存的变量副本中
| use     | 使用    | 工作内存 | 将工作内存中一个变量的值传递给执行引擎
| assign  | 赋值    | 工作内存 | 把一个从执行引擎接收到的值赋给工作内存的变量
| store   | 存储    | 工作内存 | 把一个工作内存中变量的值传送到主内存中，以便于随后的write指令使用
| write   | 写入    | 主内存   | 把store指令从工作内存传出的变量的值写入到主内存的变量中
表1 JMM内存操作指令表

-- 20170702 01:48 编辑到JMM内存操作指令表

JMM规定了在使用以上8种内存操作时必须遵守以下规则：
* read,load或store,write必须成对出现。如：执行read操作从主内存读取一个变量后，必须在工作内存使用load载入这个变量。两者之间的顺序不可错，但两者之间可以穿插其它指令。同理，在工作内存中对一个变量使用了store指令，必须在主内存中使用write指令进行写入。
* 不允许一个线程丢弃它最近做的assign操作。变量在工作内存中改变了之后必须将这个变化同步到主内存中。
* 不允许一个线程没有发生过assign操作就将数据从工作内存同步到主内存。
* 只能在主内存中新建全局变量，不能在工作内存中直接使用一个未被初始化的变量。可以使用assign和load指令在工作内存对一个变量进行初始化操作。
* 一个变量在同一时刻只能被一个线程执行lock操作。
* 如果对一个变量执行lock，那将清空工作内存中此变量的值。在执行引擎使用这个变量之前，需要重新load或assign重新初始化变量的值。
* unlock只能解锁被本线程锁定的变量。
* 对一个变量执行unlock之前，必须将此变量同步回主内存。

### volatile 关键字
volatile是java语言所提供的关键字，使java最轻量级的内存同步的措施。与 synchronized 块相比，volatile 变量所需的编码较少，并且运行时开销也较少，但是它所能实现的功能也仅是 synchronized 的一部分。   

java中锁提供了两种主要特性：互斥（mutual exclusion） 和可见性（visibility）。互斥即一次只允许一个线程持有某个特定的锁，因此可使用该特性实现对共享数据的协调访问协议，这样，一次就只有一个线程能够使用该共享数据。可见性要更加复杂一些，它必须确保释放锁之前对共享数据做出的更改对于随后获得该锁的另一个线程是可见的 。  
volatile 变量具有 synchronized 的可见性特性，但是不具备锁的原子特性。所以即便volatile没有不一致的问题，但volatile变量在并发的运算下并不是原子操作，所以依然可能会有安全问题。

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

   代码1 volatile 线程安全测试

   程序的执行结果如图2所示。
   ![volatile 线程安全测试]()



## 引用

  本文是对class文件的学习笔记，笔记的内容并非是原创，而是大量参考其它资料。在写作本文的过程中引用了以下资料，为为在此深深谢过以下资料的作者。
  1. 《The Java Virtual Machine Specification》
  2. 《深入理解Java虚拟机：JVM高级特性与最佳实践/周志明著.——2版.——北京：机械工业出版社，2013.6》


  ## 关于



<link rel="stylesheet" href="[path to fork.css]">
<div class="fork-me-wrapper">

  <div class="fork-me">

    <a class="fork-me-link" href="https://github.com/edull24/fork-me-on-github">

      <span class="fork-me-text">Fork Me On GitHub</span>

    </a>

  </div>

</div>
