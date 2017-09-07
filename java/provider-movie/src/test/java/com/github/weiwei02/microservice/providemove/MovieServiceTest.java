package com.github.weiwei02.microservice.providemove;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/9/7
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Import(ProviderMovieApplication.class)
public class MovieServiceTest {
    @Autowired
    MovieService movieService;

    @Test
    public void findUserByName() throws Exception {
        for (int i = 0; i < 1000 ; i++) {
            System.out.println(movieService.findUserByName("ceshi" + i));
        }
    }

}