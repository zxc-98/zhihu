package com.zxc.zhihu.contorler;

import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventProducer;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.model.Comment;
import com.zxc.zhihu.model.HostHolder;
import com.zxc.zhihu.model.Question;
import com.zxc.zhihu.service.CommentService;
import com.zxc.zhihu.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/21 16:32
 */
@Controller
public class CommentControler {


    @Autowired
    HostHolder hostHolder;
    @Autowired
    CommentService commentService;

    //事件生产者
    @Autowired
    EventProducer eventProducer;
    @Autowired
    QuestionService questionService;
    //,需要参数问题的id,和评论的内容

    /**
     * 添加对问题的评论
     *
     * @param questionId 问题的id
     * @param content    问题的内容
     * @param request    http请求
     * @param model      model模型(addAttribute)
     * @return
     */
    @RequestMapping(path = {"/addComment"}, method = RequestMethod.POST)
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content, HttpServletRequest request, Model model) {

        try {
            Comment comment = new Comment();

            comment.setContent(content);
            comment.setCreatedDate(new Date());
            comment.setEntityType(EventType.COMMENT.getValue()); //1表示对问题的评论。
            comment.setEntityId(questionId);

            if (hostHolder.getUser() == null) {
                //用户没有登陆，让用户去登陆，然后返回这个页面。
                return "redirect:reglogin?next=/question/" + questionId;
            } else {
                //用户已经登陆的话。
                comment.setUserId(hostHolder.getUser().getId());
                commentService.addComment(comment);//添加评论成功。


                //通过问题的id获取评论集合
                List<Comment> comments = commentService.getCount(EventType.COMMENT.getValue(), questionId);
                //修改question的评论个数
                int commentCount = comments.size();
                questionService.modifyCountNum(questionId, commentCount);

                model.addAttribute("comments", comments);//评论所有的评论添加到model中

                // 发送异步事件
                // 方便新鲜事提醒粉丝，具体评论了什么内容
                eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
                        .setEntityId(questionId).setExt("comment", comment.getContent())); //把评论的内容放进入

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question/" + questionId;

    }

    /**
     * 根据用户id获取评论集合，按照日期进行展示
     *
     * @param uid 用户id
     * @return map集合
     */
    @RequestMapping(path = "/getCommitsByUserId", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List> getCommentsByUserId(@RequestParam("uid") int uid) {
        //根据用户id获取评论集合
        List<Comment> list = commentService.getCommentsByUserId(uid);

        //评论-->问题id-->问题集合
        List<Question> question = new ArrayList<>();
        for (Comment comment : list) {
            question.add(questionService.getQuerstionByid(comment.getEntityId()));
        }

        //将评论集合和问题集合放入map集合之中
        Map<String, List> map = new HashMap<>();
        //评论信息
        map.put("comment", list);
        //评论标题
        map.put("question", question);
        return map;
    }


}
