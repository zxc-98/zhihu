package com.zxc.zhihu.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.zxc.zhihu.async.EventHandler;
import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.model.EntityType;
import com.zxc.zhihu.model.Feed;
import com.zxc.zhihu.model.Question;
import com.zxc.zhihu.model.User;
import com.zxc.zhihu.service.FeedService;
import com.zxc.zhihu.service.FollowService;
import com.zxc.zhihu.service.QuestionService;
import com.zxc.zhihu.service.UserService;
import com.zxc.zhihu.util.JedisAdapter;
import com.zxc.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author zxc
 * @date 2021
 */
@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;

    private String buildFeedDate(EventModel eventModel) {
        Map<String, String> map = new HashMap<>();
        // 触发用户是通用的
        User userById = userService.getUserById(eventModel.getActorId());
        if (userById == null) {
            return null;
        }

        //map集合的用户信息
        map.put("userId", String.valueOf(userById.getId()));
        map.put("userHead", userById.getHeadUrl());
        map.put("userName", userById.getName());
        //如果事件模型中是评论问题，或者是关注问题的话。把这个问题相关的问题id，问题的标题放到map中。
        //如果这个是用户A关注了某个问题，或者用户A评论了某个问题。就会把新鲜事推荐给A的粉丝。
       /* if (eventModel.getType()==EventType.COMMENT||
                (eventModel.getType()==EventType.FOLLOW&&eventModel.getEntityType()
                ==EntityType.Question)){
            Question question=questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));
            map.put("questionTitle",question.getTitle());
            return JSONObject.toJSONString(map);
        }*/

        //用户a关注了某个问题
        if (eventModel.getType() == EventType.FOLLOW && eventModel.getEntityType()
                == EntityType.Question) {
            // 通过问题的id获取问题类
            Question question = questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            //将问题的id和问题的标题添加
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }

        //用户a关注了某个人,加入时间轴
        if (eventModel.getType() == EventType.FOLLOW_USER) {
            /*.setEntityId(userid).setExt("username",hostHolder.getUser().getName()).setEntityOwnerId(userid).
             */
            // 放入用户名和用户id
            map.put("followeename", eventModel.getExt("username"));
            map.put("userid", eventModel.getEntityId() + "");
            return JSONObject.toJSONString(map);
        }

        //用户a评论了某个问题,这时候就需要把评论的内容，评论的问题也加上。
        if (eventModel.getType() == EventType.COMMENT) {
            Question question = questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId", String.valueOf(question.getId()));//设置问题的id
            map.put("questionTitle", question.getTitle());//设置评论的问题标题
            map.put("comment", eventModel.getExts().get("comment"));//设置评论的内容
            return JSONObject.toJSONString(map);
        }

        //如果是用户A刚刚回答了问题，那么提醒他的粉丝去评论
        if (eventModel.getType() == EventType.ANSWER_QUESTION) {
            Question question = questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId", String.valueOf(question.getId()));//设置问题的id
            map.put("questionTitle", question.getTitle());//设置评论的问题标题
            return JSONObject.toJSONString(map);
        }

        //如果用户刚刚发布了一个问题的话，那么需要把这个新鲜事放到他自己的时间轴队列中
        if (eventModel.getType() == EventType.ADD_QUESTION) {
            Question question = questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId", String.valueOf(question.getId()));//设置问题的id
            map.put("questionTitle", question.getTitle());//设置评论的问题标题
            return JSONObject.toJSONString(map);
        }

        return null;
    }

    @Override
    public void doHandle(EventModel model) {
        //为了测试，把model的useriD随机一下
        Random random = new Random();
        //设置事件模型的的触发人。当前登录用户进行点赞评论发布问题等，事件模型的触发人就是当前用户
        model.setActorId(model.getActorId());

        //创建一个新鲜事
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());//feed类设置事件类型
        feed.setUserId(model.getActorId());
        feed.setData(buildFeedDate(model));//feed流的数据就是事件触发者触发某个事件相应的数据

        if (feed.getData() == null) {
            return;
        }
        feedService.addFeed(feed);

        //获取所有的粉丝
        List<Integer> follers = followService.getFollers(EntityType.User, model.getActorId(), Integer.MAX_VALUE);
        //系统队列 将所有事件都给管理员发送一份
        follers.add(18);
        //给所有的粉丝推送事件
        for (int follower : follers) {
            //PersonTimelinev:userId
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);
            //把这次新鲜事feed的id推送给粉丝的时间轴timeline中。等该用户从时间轴pull中拉数据
            jedisAdapter.lpush(timelineKey, String.valueOf(feed.getId()));
            // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
        }

        //除了把这些信息推送到粉丝的时间轴redis key中之外，还应该推送到自己的个人时间轴上
        //比如时间轴上有我提出了什么问题，我关注了什么问题，我评论了什么问题，我关注了谁。
        jedisAdapter.lpush(RedisKeyUtil.getBIZ_PersonTimeline(model.getActorId()), String.valueOf(feed.getId()));
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.COMMENT, EventType.FOLLOW, EventType.ANSWER_QUESTION,
                EventType.ADD_QUESTION, EventType.FOLLOW_USER});
    }
}
