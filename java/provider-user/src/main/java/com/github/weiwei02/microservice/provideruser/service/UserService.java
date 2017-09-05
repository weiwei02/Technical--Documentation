package com.github.weiwei02.microservice.provideruser.service;

import com.github.pagehelper.Page;
import com.github.weiwei02.microservice.provideruser.dao.UserMapper;
import com.github.weiwei02.microservice.provideruser.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/5
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    public List<User> selectAllUser(){
        return userMapper.selectAll();
    }
}
