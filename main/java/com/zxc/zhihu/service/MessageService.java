package com.zxc.zhihu.service;

import com.zxc.zhihu.dao.MessageMapper;
import com.zxc.zhihu.model.Message;
import com.zxc.zhihu.model.MessageExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zxc
 * @date 2021
 */
@Service
public class MessageService {
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    SensitiveService sensitiveService;

    //插入消息
    public int addMessage(Message message) {
        message.setContent(sensitiveService.filter(message.getContent()));//过滤敏感词

        int i = messageMapper.insertSelective(message);

        return i > 0 ? message.getId() : 0;

    }

    public void test() {
        MessageExample messageExample = new MessageExample();
        MessageExample.Criteria criteria = messageExample.createCriteria();

    }

    public List<Message> getConversationList(int uid, int offset, int limit) {
        List<Message> conversationList = messageMapper.getConversationList(uid, offset, limit);
        return conversationList;
    }

    public int getConversationUnreadCount(int uid, String convertisnid) {
        int conversationUnreadCount = messageMapper.getConversationUnreadCount(uid, convertisnid);
        return conversationUnreadCount;
    }

    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return messageMapper.getConversationDetail(conversationId, offset, limit);

    }

    public boolean deleteMessageById(int messageid) {
        int i = messageMapper.deleteByPrimaryKey(messageid);
        return i > 0 ? true : false;
    }

    public Message getMessageById(int messageid) {
        Message message = messageMapper.selectByPrimaryKey(messageid);
        return message;
    }

    public boolean isExistMessageByContent(String content) {

        return messageMapper.hasExistMessageByContent(content) > 0 ? true : false;
    }
}

