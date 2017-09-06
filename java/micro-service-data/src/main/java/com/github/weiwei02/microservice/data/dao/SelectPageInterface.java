package com.github.weiwei02.microservice.data.dao;

import java.io.Serializable;
import java.util.List;

/**带有分页的查询方法
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/5
 */
public interface SelectPageInterface<T  extends Serializable> {
    List<T> selectWithPage(Integer pageNum, Integer pageSize, T t);
}
