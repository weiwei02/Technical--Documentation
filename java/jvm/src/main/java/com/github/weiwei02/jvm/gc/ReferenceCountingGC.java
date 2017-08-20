package com.github.weiwei02.jvm.gc;

/**
 * 事实上目前的商用jvm中没有任何虚拟机采用引用计数算来来进行垃圾回收，引用计数算法无法解决对象之间的循环引用的问题
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/8/19
 */
public class ReferenceCountingGC {
    public Object instance = null;
    private static final int _1MB = 1024 * 1024;

    private byte[] bigSize = new byte[_1MB];


    /**gc内存快照信息如下：
     * [GC (System.gc()) [PSYoungGen: 6511K->1016K(9216K)] 6511K->1412K(19456K), 0.0014686 secs] [Times: user=0.02 sys=0.01, real=0.00 secs]
     [Full GC (System.gc()) [PSYoungGen: 1016K->0K(9216K)] [ParOldGen: 396K->1327K(10240K)] 1412K->1327K(19456K), [Metaspace: 3468K->3468K(1056768K)], 0.0054196 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
     Heap
     PSYoungGen      total 9216K, used 166K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
     eden space 8192K, 2% used [0x00000000ff600000,0x00000000ff629918,0x00000000ffe00000)
     from space 1024K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x00000000fff00000)
     to   space 1024K, 0% used [0x00000000fff00000,0x00000000fff00000,0x0000000100000000)
     ParOldGen       total 10240K, used 1327K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
     object space 10240K, 12% used [0x00000000fec00000,0x00000000fed4bf90,0x00000000ff600000)
     Metaspace       used 3489K, capacity 4496K, committed 4864K, reserved 1056768K
     class space    used 383K, capacity 388K, committed 512K, reserved 1048576K
     * */
    public static void testGC(){
        ReferenceCountingGC objA = new ReferenceCountingGC();
        ReferenceCountingGC objB = new ReferenceCountingGC();
        objA.instance = objB;
        objB.instance = objA;

//        System.out.println("字符串" == new StringBuffer("字符").append("串").toString());

        objA = null;
        objB = null;

        //设置gc 测试 objA和objB是否可以被回收
        System.gc();
    }

    /**
     *  -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
     * */
    public static void main(String[] args) {
        testGC();
    }
}
