package com.github.weiwei02.microservice.provideruser.web;

import com.github.weiwei02.microservice.provideruser.model.User;
import com.github.weiwei02.microservice.provideruser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/6
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @RequestMapping("/hello")
    public String hello(){
        return "hello world!";
    }

    @RequestMapping("/search/{username}")
    public User searchUserByName( @PathVariable("username") String username){
        logger.info(username);
        return userService.selectByName(username);
    }
}
