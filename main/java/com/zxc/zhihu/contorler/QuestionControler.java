package com.zxc.zhihu.contorler;

import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventProducer;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.model.*;
import com.zxc.zhihu.service.*;
import com.zxc.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zxc
 * @date 2021
 */
@Controller
public class QuestionControler {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;

    /**
     * 提出问题
     *
     * @param title    问题的标题
     * @param content  问题的补充内容
     * @param response 相应
     * @param request  请求
     * @return 相应的结果信息
     */
    @RequestMapping(value = "/question/add", method = RequestMethod.POST)
    @ResponseBody
    public String addQuersion(@RequestParam("title") String title, @RequestParam("content") String content, HttpServletResponse response,
                              HttpServletRequest request) {

        try {
            Question question = new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            if (hostHolder.getUser() == null) {
                //说明用户没有登陆，设置匿名用户
                // question.setUserId(WendaUtil.ANONYMOUS_USERID);
                //应该让用户去登陆，然后才能发表评论
                return "redirect:reglogin?next=" + request.getRequestURL();
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }
            if (questionService.addQuestion(question) > 0) {
                //插入问题成功
                //异步队列，发送事件模型，发布问题模型，模仿知乎：某某的问题 等你来回答
                //不关注的话，可以不发问题。为什么我在redis中看不到？
                eventProducer.fireEvent(
                        new EventModel(EventType.ANSWER_QUESTION) //事件类型，发布问题模型 7
                                .setActorId(hostHolder.getUser().getId()) //当前登录者的id
                                .setEntityType(EntityType.Question) //实体类类型 问题
                                .setEntityId(question.getId()) //实体类id 问题id
                                .setEntityOwnerId(hostHolder.getUser().getId()) //实体类所有者id 问题提出者的id
                );


                eventProducer.fireEvent(
                        new EventModel(EventType.ADD_QUESTION) //事件类型 添加问题事件类 6
                                .setActorId(hostHolder.getUser()
                                        .getId()).setEntityType(EntityType.Question)
                                .setEntityId(question.getId()).setEntityOwnerId(hostHolder.getUser().getId())
                                .setExt("title", title).setExt("content", content)//
                );


                return WendaUtil.getJSONString(0);//返回0的json格式
            }

        } catch (Exception e) {

        }
        return WendaUtil.getJSONString(1, "失败");//返回1的json格式和"失败"的json格式
    }

    /**
     * 展示问题 问题评论
     *
     * @param qid
     * @param model
     * @return
     */
    @RequestMapping(value = "/question/{qid}", method = RequestMethod.GET)
    public String showQuestion(@PathVariable("qid") Integer qid, Model model) {

        Question question = questionService.getQuerstionByid(qid);
        model.addAttribute("question", question);

        //获取该问题的所有评论
        List<Comment> commentList = commentService.getCount(1, qid);
        List<ViewObject> comments = new ArrayList<ViewObject>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);

            //显示当前用户对这个评论的喜欢状态
            if (hostHolder.getUser() == null) {
                vo.set("liked", 0); //如果用户没有登陆,不显示喜欢的状态。
            } else {
                //likeService.getLikeStatus 获取当前登录用户对该评论的状态：1.喜欢 -1：点赞 0：无表示
                //参数：1.当前登录用户的id
                //	   2.实体类型 2 (评论类型)
                //	   3.评论的id
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.Comment, comment.getId()));
            }
            //获取每个评论的点赞数
            vo.set("likeCount", likeService.getLikeCount(2, comment.getId()));
            //获取评论的用户
            vo.set("user", userService.getUserById(comment.getUserId()));

            // 将点赞状态 评论的点赞数和评论用户 3个map集合放入ViewObject对象中 将ViweObject对象放入评论集合comments中
            comments.add(vo);
        }

        //将评论集合返回至客户端
        model.addAttribute("comments", comments);
        // model.addAttribute("answerNum",commentList.size());//返回前端回答此问题的人数


        //获取关注此问题的20个用户，显示他们的头像，姓名,
        List<ViewObject> list = new ArrayList<>();
        //存放关注该问题用户的id集合
        List<Integer> follers = followService.getFollers(EntityType.Question, qid, 20);
        //通过id查询用户信息
        for (int id : follers) {
            ViewObject vo = new ViewObject();
            User u = userService.getUserById(id);
            if (u == null) {
                continue;
            }
            //展示模块ViewObject:存放关注者的姓名，头像和id
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            list.add(vo);
        }
        //将关注者的信息传入客户端
        model.addAttribute("followUsers", list);

        //如果登录的话
        if (hostHolder.getUser() != null) {
            //当前观看问题的用户是否为当前问题的关注者
            model.addAttribute("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.Question, qid));
        } else {//没有登陆的话，默认不是关注的
            model.addAttribute("followed", false);
        }
        return "detail";
    }

    /**
     * 根据userid获得该用户所有的提出的问题
     *
     * @param uid
     * @param request
     * @return
     */
    @RequestMapping(value = "/question/getQuestionsByUserId", method = RequestMethod.GET)
    @ResponseBody
    public List<Question> getQuesiontsByUserid(@RequestParam("uid") int uid, HttpServletRequest request) {

        List<Question> questionsByUserId = questionService.getQuestionsByUserId(uid);
        return questionsByUserId;
    }
}
