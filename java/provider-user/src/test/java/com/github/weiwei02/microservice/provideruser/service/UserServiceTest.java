package com.github.weiwei02.microservice.provideruser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.weiwei02.microservice.provideruser.ProviderUserApplication;
import com.github.weiwei02.microservice.provideruser.dao.UserMapper;
import com.github.weiwei02.microservice.provideruser.model.User;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/5
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Import(ProviderUserApplication.class)
public class UserServiceTest {
    @Autowired UserService userService;
    @Autowired
    UserMapper userMapper;
    @Test
    public void selectAllUser() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(System.out, userService.selectAllUser());
    }
@Test
    public void delete() throws Exception {
    }

    @Test
    public void insert(){
        User user = new User();
        user.setAge(11);
        user.setName("韩信");
        user.setUsername("hanxin");
        user.setBalance(new BigDecimal(1200));
        userMapper.insertUseGeneratedKeys(user);
    }
@Test
    public void insertList() throws InterruptedException {
    List<User> users = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setAge((int) (Math.random() * 50));
            user.setName("测试" + i);
            user.setUsername("ceshi" + i);
            user.setBalance(new BigDecimal(Math.random() * 1000));
            users.add(user);
        }
    userMapper.insertList(users);
        Thread.sleep(5000);
    }

   /* @Test
    public void insertListInCluster(){
    List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setAge((int) (Math.random() * 50));
            user.setName("测试" + i);
            user.setUsername("ceshi" + i);
            user.setBalance(new BigDecimal(Math.random() * 1000));
            users.add(user);
        }
    userMapper.insertListInCluster(users);
    }*/
@Test
    public void insertList1(){
    List<User> users = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setAge((int) (Math.random() * 50));
            user.setName("测试" + i);
            user.setUsername("ceshi" + i);
            user.setBalance(new BigDecimal(Math.random() * 1000));
            userMapper.insertUseGeneratedKeys(user);
        }
    }


    @Test
    public void selectWithPage() throws IOException {
        User user = new User();
        user.setAge(10);
        ObjectMapper objectMapper = new ObjectMapper();
        Page<User> page =  (Page<User>)userService.selectWithPage(1, 10, user);
        System.out.println(page.getTotal());
        System.out.println(page.getPages());
        System.out.println(page.size());
        objectMapper.writeValue(System.out,page);
    }

    @Test
    public void insert1(){
        User user = new User();
        user.setAge((int) (Math.random() * 50));
        user.setName("吴梦雅");
        user.setUsername("wu");
        user.setBalance(new BigDecimal(Math.random() * 1000));
        userMapper.insertUseGeneratedKeys(user);
    }
}