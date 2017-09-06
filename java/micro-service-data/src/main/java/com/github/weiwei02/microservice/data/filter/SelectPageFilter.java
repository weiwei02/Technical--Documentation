package com.github.weiwei02.microservice.data.filter;

import com.github.pagehelper.PageHelper;
import com.github.weiwei02.microservice.data.dao.BaseEntity;
import com.github.weiwei02.microservice.data.filter.annotations.AutoPage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AopInvocationException;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.List;

/**自动分页处理器
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/5
 */
@Aspect
@Component
public class SelectPageFilter {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 为service类中以select 开头 以 WithPage结尾，且返回值是List的方法添加切点
     * */
    @Pointcut("execution(public java.util.Collection<java.io.Serializable+>+ com.github.weiwei02.microservice..*Service.select*WithPage(java.lang.Integer, java.lang.Integer,..))")
    public void selectWithPage(){}

    /**加有@AutoPage分页注解的方法也是切点，并且根据注解的参数位置指针去取分页参数*/
    @Pointcut("@annotation(com.github.weiwei02.microservice.data.filter.annotations.AutoPage)")
    public void pageAnnotation(){}

    @Around("selectWithPage() || pageAnnotation()")
    public List aroundSelectWithPage(ProceedingJoinPoint jp){
        logger.info("--------------开始分页------------");
        Integer[] pageParams = new Integer[2];
        Object[] args = jp.getArgs();
        searchPageAnnotation(args, pageParams, ((MethodSignature)(jp.getSignature())).getMethod());
        searchEntityAndParam(pageParams, args);
        return PageHelper.startPage(pageParams[0], pageParams[1]).doSelectPage(() -> {
            try {
                jp.proceed();
            } catch (Throwable throwable) {
                throw new AopInvocationException(throwable.getMessage());
            }
        });
    }

    /**
     * 如果切点并没有使用 @AutoPage 注解，则判断切点是否有 BaseEntity类型的参数，如果有的话，则从参数里取分页信息
     * */
    private void searchEntityAndParam(Integer[] pageParams, Object[] args) {
        if (pageParams[0] ==null || pageParams[1] ==null){
            pageParams[0] = (Integer) args[0];
            pageParams[1] = (Integer) args[1];
            if (pageParams[0] ==null || pageParams[1] == null)
                throw new InvalidParameterException("未在分页方法中找到 BaseEntity 对象，或未加@Page注解，无法进行自动分页");
        }
    }

    /**
     * 如果切点方法有使用 @AutoPage 注解，则依照注解说明去取参数
     * */
    private void searchPageAnnotation(Object[] args, Integer[] params, Method targetMethod) {
        Annotation[] annotations = targetMethod.getDeclaredAnnotations();
        for (Annotation annotation : annotations){
            if (annotation instanceof AutoPage){
                AutoPage page = (AutoPage) annotation;
                if (page.value()){
                    params[0] = (Integer) args[page.pageNum()];
                    params[1] = (Integer) args[page.pageSize()];
                }
                return;
            }
        }
    }


    /**
     * 查找并返回BaseEntity对象
     * */
    private BaseEntity searchEntity(Object[] args) {
        BaseEntity entity;
        for (Object arg : args){
            if (arg instanceof BaseEntity){
                entity = (BaseEntity) arg;
                return entity;
            }
        }
        return null;
    }
}
