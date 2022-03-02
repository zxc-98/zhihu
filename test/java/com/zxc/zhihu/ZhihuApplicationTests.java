package com.zxc.zhihu;


import com.zxc.zhihu.service.FeedService;
import com.zxc.zhihu.service.FollowService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ZhihuApplication.class)
@WebAppConfiguration
public class ZhihuApplicationTests {

    @Autowired
    FeedService feedService;

    @Autowired
    FollowService followService;

    @Test
    public void contextLoads() {
        List<Integer> follees = followService.getFollees(19, 3, Integer.MAX_VALUE);
        for (Integer follee : follees) {
            System.out.println(follee);
        }
    }

}


