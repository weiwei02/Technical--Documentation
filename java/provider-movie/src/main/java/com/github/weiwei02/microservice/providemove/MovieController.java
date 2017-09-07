package com.github.weiwei02.microservice.providemove;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/7
 */
@RestController
public class MovieController {
    @Autowired
    MovieService movieService;

    @GetMapping("/user/{username}")
    public String findUser(@PathVariable("username") String username){
        return movieService.findUserByName(username);
    }
}
