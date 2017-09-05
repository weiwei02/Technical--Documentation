package com.github.weiwei02.microservice.provideruser.dao;

import com.github.weiwei02.microservice.data.dao.SuperMapper;
import com.github.weiwei02.microservice.provideruser.model.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends SuperMapper<User> {
}