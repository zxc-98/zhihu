package com.zxc.zhihu.service;

import com.zxc.zhihu.dao.FeedMapper;
import com.zxc.zhihu.model.Feed;
import com.zxc.zhihu.util.JedisAdapter;
import com.zxc.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeedService {

    @Autowired
    FeedMapper feedMapper;
    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 插入一条Feed新鲜事
     *
     * @param feed
     * @return
     */
    public boolean addFeed(Feed feed) {
        int insert = feedMapper.insert(feed);
        return insert > 0; //插入数据大于o表示插入成功
    }

    //根据id查询Feed流信息
    public Feed getFeedById(int id) {
        Feed feed = feedMapper.selectByPrimaryKey(id);
        return feed;
    }

    /**
     * 查询关注用户的最新新鲜事
     *
     * @param maxId 数据库feed表全局遍历
     * @param ids   关注用户们的id
     * @param count 返回新鲜事总数
     * @return feed类集合
     */
    public List<Feed> getUsersFeed(int maxId, List<Integer> ids, int count) {
        return feedMapper.queryUserFeeds(maxId, ids, count);
    }

    /**
     * 根据用户id获取个人时间轴
     *
     * @param uid 根据用户id获取个人时间轴
     * @return 用户的时间轴 feed类集合
     */
    public List<Feed> getMyPersonTimeLine(int uid) {
        List<String> lrange = jedisAdapter.lrange(RedisKeyUtil.getBIZ_PersonTimeline(uid), 0, Integer.MAX_VALUE);
        List<Feed> list = new ArrayList<>();
        for (String feedid : lrange) {
            Feed feed = feedMapper.selectByPrimaryKey(Integer.parseInt(feedid));
            list.add(feed);
        }
        return list;//返回所有的feed信息
    }

    public static void main(String[] args) {

    }
}
