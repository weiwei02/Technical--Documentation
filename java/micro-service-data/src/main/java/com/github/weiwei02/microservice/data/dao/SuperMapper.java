package com.github.weiwei02.microservice.data.dao;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/5
 */
public interface SuperMapper<T>  extends Mapper<T>, MySqlMapper<T> {

}
