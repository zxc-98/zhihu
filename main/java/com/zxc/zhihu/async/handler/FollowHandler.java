package com.zxc.zhihu.async.handler;

import com.zxc.zhihu.async.EventHandler;
import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.model.EntityType;
import com.zxc.zhihu.model.Message;
import com.zxc.zhihu.service.MessageService;
import com.zxc.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author zxc
 * @date 2021
 */
@Component
public class FollowHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Override
    public void doHandle(EventModel model) {
        EventType type = model.getType(); //获取事件类型

        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);//数据库中id=12的用户是通知小助手
        message.setToId(model.getEntityOwnerId());//实体类所有者id
        Date date = new Date();
        message.setCreatedDate(date);

        if (type == EventType.FOLLOW) {//如果事件类型为关注类型，可以是关注问题，或者是关注人


            if (model.getEntityType() == EntityType.User) {
                //说明，用户a关注了关注了用户b
                message.setContent("知乎用户" + model.getExt("username") + "关注了你" +
                        "<a href=\"http://127.0.0.1:8080/user/" + model.getActorId() + "\">点击这里，快速查看谁关注了你</a>");
            } else if (model.getEntityType() == EntityType.Question) {
                //说明，用户a关注了关注了用户b发布的问题
                message.setContent("知乎用户" + model.getExt("username") + "关注了你的问题" +
                        "<a href=\"http://127.0.0.1:8080/question/" + model.getEntityId() + "\">点击这里，查看你的提问</a>");

            }


        } else if (type == EventType.UNFOLLOW) { //取消关注这里决定是否进行通知。一般不进行通知，比如a取消了对b的关注，那么不应该通知b
            message.setContent("知乎用户" + model.getExt("username") + "取消对您的关注或者对您问题的关注");
        }

        messageService.addMessage(message);

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.FOLLOW, EventType.UNFOLLOW); //处理关注和不关注的问题
    }
}
