package com.zxc.zhihu.async;

import com.alibaba.fastjson.JSONObject;
import com.zxc.zhihu.util.JedisAdapter;
import com.zxc.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 事件生产者
 */
@Service
public class EventProducer {
    @Autowired
    JedisAdapter jedisAdapter;//redis类

    /**
     * 事件生产者发送事件
     *
     * @param eventModel 传入参数是事件模型类
     * @return
     */
    public boolean fireEvent(EventModel eventModel) {
        try {
            //将事件模型类-->JSON类型
            String json = JSONObject.toJSONString(eventModel);//value=问题模型类JSON类型
            String key = RedisKeyUtil.getEventQueueKey();// key="EVENT_QUEUE"
            jedisAdapter.lpush(key, json);//存入redis
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
