package com.github.weiwei02.microservice.data.config.filter;

import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/4
 */
@ConditionalOnClass( javax.servlet.http.HttpServlet.class)
@WebFilter(filterName="druidWebStatFilter",urlPatterns="/*",
        initParams={
                @WebInitParam(name="exclusions",value="*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*")// 忽略资源
        })
public class DruidStatFilter extends WebStatFilter {
}
