package com.github.weiwei02.jvm.jmm.volatile_test;

/**函数式接口： 只有一个  抽象方法  的接口
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/8/26
 */
public interface DefaultInterface {

    int add(int a, int b);


    default int add2(int a, int b){
        return a - b;
    }
}

