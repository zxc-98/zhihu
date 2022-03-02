package com.zxc.zhihu.contorler;

import com.alibaba.fastjson.JSONObject;
import com.zxc.zhihu.model.HostHolder;
import com.zxc.zhihu.model.Message;
import com.zxc.zhihu.model.User;
import com.zxc.zhihu.model.ViewObject;
import com.zxc.zhihu.service.MessageService;
import com.zxc.zhihu.service.UserService;
import com.zxc.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author zxc
 * @date 2021
 */
@Controller
public class MessageControler {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    /**
     * 写消息
     *
     * @param toname
     * @param letterContent
     * @return
     */
    @RequestMapping(value = "/msg/addMessage", method = RequestMethod.POST)
    @ResponseBody
    public String sendPrivateMessage(@RequestParam("toName") String toname,
                                     @RequestParam("content") String letterContent) {//参数：
        //toname：接收端姓名
        //content：内容
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999, "请先登录");
            }
            //查询接收端用户是否存在
            User userByName = userService.getUserByName(toname);
            if (userByName == null) {
                return WendaUtil.getJSONString(1, "用户名不存在");
            }
            //查询内容是否为空
            if (letterContent.trim().length() == 0) {
                return WendaUtil.getJSONString(1, "发送信息不能为空");
            }
            //构造消息对象
            Message message = new Message();
            message.setFromId(hostHolder.getUser().getId());//当前登录的用户就是消息发送方
            message.setToId(userByName.getId());
            message.setContent(letterContent);
            message.setCreatedDate(new Date());

            int i = messageService.addMessage(message);
            return WendaUtil.getJSONString(0);//插入消息成功
        } catch (Exception e) {
            e.printStackTrace();
            return WendaUtil.getJSONString(1, "发信失败");
        }

    }

    /**
     * 发送和接收消息显示
     *
     * @param model
     * @return
     */
    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String getConversationList(Model model) {
        if (hostHolder.getUser() == null) {
            return "redirect:/reglogin";
        }
        int localUserId = hostHolder.getUser().getId();
        //获取接受和发送消息一共10条信息集合list 发送方和接受方 在数据库message查询时 SELECT * FROM message WHERE from_id=20 or to_id=20 ： or:把发送消息和接受消息都拿出来了
        List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
        List<ViewObject> conversations = new ArrayList<ViewObject>();//显示体集合
        for (Message message : conversationList) {
            ViewObject vo = new ViewObject();
            vo.set("message", message);
            //判断当前登录用户是消息的发送方还是接受方 targetId:表示接收用户id
            int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();
            //获取接收端用户
            vo.set("user", userService.getUserById(targetId));
            //如果has_read状态为0的话 说明没读 获取当前消息已读状态  ? 怎么判断消息读了？
            //获取未读消息的总数
            vo.set("unread", messageService.getConversationUnreadCount(localUserId, message.getConversationId()));
            conversations.add(vo);
        }
        model.addAttribute("conversations", conversations);
        return "letter";
    }


    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String getConversationDetail(Model model, @RequestParam("conversationId") String conversationId) {
        try {
            //根据conversationId:20_38...获取
            List<Message> messageList = messageService.getConversationDetail(conversationId, 0, 10);

            List<ViewObject> messages = new ArrayList<ViewObject>();
            for (Message message : messageList) {
                ViewObject vo = new ViewObject();
                vo.set("message", message);
                vo.set("user", userService.getUserById(message.getFromId()));
                messages.add(vo);
            }
            model.addAttribute("messages", messages);
        } catch (Exception e) {
            // logger.error("获取详情失败" + e.getMessage());
        }
        return "letterDetail";
    }

    //删除message
    @RequestMapping(value = "/msg/deleteMessage", method = RequestMethod.POST)
    @ResponseBody
    public String deleteMessage(@RequestParam("messageid") int messageid) {

        Map<String, String> map = new HashMap<>();

        //1 判断用户是否已经登陆，如果没登陆的话，请先登录
        if (hostHolder.getUser() == null) {
            return "redirect:reglogin";//重定向至重新登录界面
        } else {
            //从数据库中将消息删除
            boolean success = messageService.deleteMessageById(messageid);

            //获取conversationId 对话id
            String conversationId = messageService.getMessageById(messageid).getConversationId();
            //获取json数据的目的是什么？ 回答：返回json数据就是跳转到"url"的页面
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", "http://localhost:8080/msg/detail?conversationId=" + conversationId);
            return jsonObject.toJSONString();
        }


    }
}
