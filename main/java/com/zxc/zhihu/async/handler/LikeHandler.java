package com.zxc.zhihu.async.handler;


import com.zxc.zhihu.async.EventHandler;
import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.model.Message;
import com.zxc.zhihu.model.User;
import com.zxc.zhihu.service.MessageService;
import com.zxc.zhihu.service.UserService;
import com.zxc.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 2021
 */
@Component
public class LikeHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        //本质上也是 发送异步消息  from:id=12(通知小助手) to:实体类的id(评论所有者id)
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        //创建消息内容
        User user = userService.getUserById(model.getActorId());//获取当前登录用户id
        String content = "用户" + user.getName()
                + "赞了你的评论 <a href=\"http://127.0.0.1:8080/question/" + model.getExt("questionId") + "\">猛戳这里</a>";
        message.setContent(content);

        // 用户点赞又然后再点赞 但是异步传输的小助手消息只发送一条
        // 搜索数据库中是否已经包含了相同的content,如果找到，说明已经发送过信息了 true表示已经存在，不再发信息了；false表示不存在可以发。
        boolean existMessageByContent = messageService.isExistMessageByContent(content);


        // 当前登录用户id不等于实体类所有者(评论所有者)id
        // 排除掉自己的点赞和，同一个人多次点赞同一个信息，发送多条消息。
        if ((model.getActorId() != model.getEntityOwnerId()) && (!existMessageByContent))  //点赞自己，不发送信息。
            messageService.addMessage(message);//

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
