package com.github.weiwei02.microservice.data.filter.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**分页注解方法上加此注解可实现自动分页，默认取第一个参数作为当前页，第二个参数作为每页条数
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoPage {
    /**
     * 是否自定义参数分页位置？
     */
    boolean value() default false;

    /**pageNum参数所在位置*/
    int pageNum() default 0;
    /**pageSize参数所在位置*/
    int pageSize() default  1;

}
