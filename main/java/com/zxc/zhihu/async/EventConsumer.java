package com.zxc.zhihu.async;

import com.alibaba.fastjson.JSON;
import com.zxc.zhihu.util.JedisAdapter;
import com.zxc.zhihu.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件消费者
 * 使用了单线程循环获取队列里的事件，并且寻找对应的handler进行处理
 */
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    //日志工厂获取日志信息
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    // map类型 --> 键：事件类型 值：事件处理器队列(队列中放着一个某类型的事件处理器) 例如：map(EventType.LIKE,LikeHandler)
    private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();
    private ApplicationContext applicationContext;

    @Autowired
    JedisAdapter jedisAdapter;

    /*
        封装所有的事件处理器实现类：config将事件类型和事件处理器对应起来
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //获取EventHandler接口的所有实现类
        //{loginExceptionHandler=com.zxc.zhihu.async.handler.LoginExceptionHandler@36eecefc, addQuestionHandler=com.zxc.zhihu.async.handler.AddQuestionHandler@72daf372, followHandler=com.zxc.zhihu.async.handler.FollowHandler@471ab4b, feedHandler=com.zxc.zhihu.async.handler.FeedHandler@435b081f, likeHandler=com.zxc.zhihu.async.handler.LikeHandler@382a7a6}
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        //System.out.println(beans);
        if (beans != null) {
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();

                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
            //{UNFOLLOW=[com.zxc.zhihu.async.handler.FollowHandler@3b6c1316],
            // ADD_QUESTION=[com.zxc.zhihu.async.handler.AddQuestionHandler@7fc6e052,com.zxc.zhihu.async.handler.FeedHandler@1263cdb2],
            // FOLLOW_USER=[com.zxc.zhihu.async.handler.FeedHandler@1263cdb2],
            // LIKE=[com.zxc.zhihu.async.handler.LikeHandler@6ceba981],
            // ANSWER_QUESTION=[com.zxc.zhihu.async.handler.FeedHandler@1263cdb2],
            // LOGIN=[com.zxc.zhihu.async.handler.LoginExceptionHandler@3744f3a9],
            // COMMENT=[com.zxc.zhihu.async.handler.FeedHandler@1263cdb2],
            // FOLLOW=[com.zxc.zhihu.async.handler.FollowHandler@3b6c1316, com.zxc.zhihu.async.handler.FeedHandler@1263cdb2]}
            //System.out.println(config);
        }

        //在异步事件处理时，为保证实时性，直接开启了线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //获取消息队列的键:EVENT_QUEUE
                    String key = RedisKeyUtil.getEventQueueKey();
                    //获取消息队列键中对应的值集合(有消息队列生产者可知,值为：Json类型的事件模型集合) value = [key,JsonString类型的事件模型]
                    List<String> events = jedisAdapter.brpop(0, key);
                    //System.out.println(events);
                    //用户登录：[EVENT_QUEUE, {"actorId":20,"entityId":0,"entityOwnerId":0,"entityType":0,"exts":{"email":"1036795393@qq.com","username":"1002"},"type":"LOGIN"}]
                    //[EVENT_QUEUE, {"actorId":20,"entityId":49,"entityOwnerId":31,"entityType":3,"exts":{"username":"1002"},"type":"FOLLOW"}]
                    //[EVENT_QUEUE, {"actorId":20,"entityId":49,"entityOwnerId":0,"entityType":0,"exts":{"comment":"hhhhh"},"type":"COMMENT"}]
                    //[EVENT_QUEUE, {"actorId":20,"entityId":40,"entityOwnerId":20,"entityType":0,"exts":{"questionId":"49"},"type":"LIKE"}]
                    //[EVENT_QUEUE, {"actorId":20,"entityId":34,"entityOwnerId":34,"entityType":3,"exts":{"username":"1017"},"type":"FOLLOW_USER"}]
                    for (String message : events) {
                        if (message.equals(key)) {//去除EVENT_QUEUE的干扰
                            continue;
                        }
                        //Json的事件模型-->事件模型类
                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件");
                            continue;
                        }
                        //进行事件模型的处理
                        for (EventHandler handler : config.get(eventModel.getType())) {
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
