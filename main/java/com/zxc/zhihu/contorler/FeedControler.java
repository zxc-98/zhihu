package com.zxc.zhihu.contorler;

import com.zxc.zhihu.model.EntityType;
import com.zxc.zhihu.model.Feed;
import com.zxc.zhihu.model.HostHolder;
import com.zxc.zhihu.service.FeedService;
import com.zxc.zhihu.service.FollowService;
import com.zxc.zhihu.util.JedisAdapter;
import com.zxc.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FeedControler {
    @Autowired
    FollowService followService;
    @Autowired
    FeedService feedService;
    @Autowired
    HostHolder hostHolder;

    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 拉模式
     * 用户登录后，直接从数据库中拉，用户关注的人最新信息
     *
     * @param model
     * @return
     */
    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String pullfeeds(Model model) {
        //判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followeeUid = new ArrayList<>();
        //如果用户非空，获得该用户的关注人物
        if (uid != 0) {
            //从redis中获取关注用户的id
            //followeeUid中存放用户的所有关注人
            followeeUid = followService.getFollees(uid, EntityType.User, Integer.MAX_VALUE);
        }

        //查询数据库，这些关注的人的最新新鲜事
        List<Feed> usersFeed = feedService.getUsersFeed(Integer.MAX_VALUE, followeeUid, 10);//查询用户关注人的十条新鲜事
        model.addAttribute("feeds", usersFeed);
        return "feeds";
    }

    //推模式，比如a发送一条信息之后，推送给所有的粉丝。

    /**
     * 1 从redis中的个人用户时间轴中，找到最新的十条feed流id
     * 2 查询feed数据库，把这些feed找出来，放到list集合中。
     * 3 返回给页面这个用户最新的10条动态。
     * 比如说登录的是1002用户 用户id=20
     *
     * @param model
     * @return
     */
    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model) {
        // 判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        // 时间轴的键:PersonTimelinev:userId
        String timelinekey = RedisKeyUtil.getTimelineKey(uid);
        //当前用户时间轴，最新的十条feed流id，然后查出具体的Feed信息放大list集合中，返回给前端。
        List<String> feedIds = jedisAdapter.lrange(timelinekey, 0, 10);
        List<Feed> list = new ArrayList<>();
        for (String feedid : feedIds) {
            // 从数据库feed表中获取feed流记录
            Feed feedById = feedService.getFeedById(Integer.parseInt(feedid));
            if (feedById != null) {
                list.add(feedById);
            }
        }
        //把新鲜事交给detail页面
        model.addAttribute("feeds", list);

        return "feeds";
    }


    /**
     * 用户登录后，直接从数据库中我自己的个人时间轴信息
     *
     * @param model
     * @param id
     * @return
     */
    @RequestMapping(value = "/mytimeline1", method = {RequestMethod.GET, RequestMethod.POST})
    private String pullfeedsMyselfTimeLine(Model model, @RequestParam("uid") int id) {
        //判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followeeUid = new ArrayList<>();
        List<Feed> list = null;
        if (uid != 0) {
            //根据用户id获取个人时间轴
            list = feedService.getMyPersonTimeLine(id);
        }
        // 前后端进行交互 先大多使用ajax加载数据
        model.addAttribute("feeds", list);
        return "personalfeeds";
    }

    /**
     * 和上面对比:在控制台中输出个人时间轴信息的创建时间
     *
     * @param model 前后端进行交互 现在大多使用ajax加载数据
     * @return
     */
    @RequestMapping(value = "/mytimeline", method = {RequestMethod.GET, RequestMethod.POST})
    public String test(Model model) {
        //判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followeeUid = new ArrayList<>();
        List<Feed> list = null;
        if (uid != 0) {
            list = feedService.getMyPersonTimeLine(uid);
        }
        //
        for (Feed feed : list) {
            System.out.println("feed=" + feed.getCreatedDate());
        }
        model.addAttribute("feeds", list);
        return "personalfeeds";
    }

}
