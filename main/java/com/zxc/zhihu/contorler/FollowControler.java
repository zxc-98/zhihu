package com.zxc.zhihu.contorler;

import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventProducer;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.model.*;
import com.zxc.zhihu.service.CommentService;
import com.zxc.zhihu.service.FollowService;
import com.zxc.zhihu.service.QuestionService;
import com.zxc.zhihu.service.UserService;
import com.zxc.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zxc
 * @date 2021
 */
@Controller
public class FollowControler {
    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    /**
     * 用户a关注用户b
     *
     * @param userid 需要传入的参数为用户id
     * @return 1 表示成功 0 表示不成功
     */
    @RequestMapping(value = "/followUser", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String followerUser(@RequestParam("userId") int userid) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        // 传入的参数：关注者(当前登录的用户) 关注的类型(枚举类型，表示关注的是用户) 被关注的用户id(客户端传入)
        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.User, userid);

        //添加成功的话，就给偶像发一条信息。说明某某用户关注了您
        if (ret) {
            //关注成功发送信息
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(userid).setExt("username", hostHolder.getUser().getName()).setEntityOwnerId(userid));

            //给自己的时间轴加上。我关注了谁谁谁
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW_USER)
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(userid).setExt("username", userService.getUserById(userid).getName()).setEntityOwnerId(userid));
        }
        //返回用户hostholder.getuser的关注人数

        //1 表示成功
        return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(
                followService.getFolloerCount(hostHolder.getUser().getId(), EntityType.User)
        ));
    }

    /**
     * 用户a取消关注用户b
     *
     * @param userid 用户id
     * @return 1 表示成功 0 表示不成功
     */
    @RequestMapping(value = "/unfollowUser", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userid) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        //将相关的键值对从数据库中删除
        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.User, userid);

        // 发送事件模型
        if (ret) {
            //取消关注成功后发送信息
            eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(userid).setExt("username", hostHolder.getUser().getName()).setEntityOwnerId(userid));
        }

        //1 表示成功
        return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(
                followService.getFolloerCount(hostHolder.getUser().getId(), EntityType.User)
        ));
    }

    /**
     * 用户a关注问题b
     *
     * @param questionId 问题id
     * @return 返回结果的JSON语句
     */
    @RequestMapping(value = "/followQuestion", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        // 获取问题id
        Question questionById = questionService.getQuerstionByid(questionId);

        // 问题不存在
        if (questionById == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        // 传入的参数：关注者id(当前登录的用户) 关注的类型(枚举类型，表示关注的是问题) 被关注的问题id(客户端传入)
        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.Question, questionId);

        // 发送关注问题的事件类型
        // 添加成功的话，就给关注者发一条信息。说明某某用户关注了您的问题
        if (ret) {
            //取消关注成功后发送信息
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW) //不关注的话，可以不发问题。
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(questionId).setExt("username", hostHolder.getUser().getName()).setEntityOwnerId(questionById.getUserId()));
            //关注了某个问题 FOLLOW Question
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW) //不关注的话，可以不发问题。
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.Question)
                    .setEntityId(questionId).setExt("username", hostHolder.getUser().getName()).setEntityOwnerId(questionById.getUserId()));

        }
        // 获取当前用户的信息和粉丝个数
        Map<String, Object> info = new HashMap<>();
        info.put("headUrl", hostHolder.getUser().getHeadUrl());
        info.put("name", hostHolder.getUser().getName());
        info.put("id", hostHolder.getUser().getId());
        info.put("count", followService.getFolloerCount(EntityType.Question, questionId));

        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    /**
     * 用户a取消关注问题b
     *
     * @param questionId 问题id
     * @return 返回结果的JSON语句
     */
    @RequestMapping(value = "/unfollowQuestion", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        // 获取问题id
        Question querstionByid = questionService.getQuerstionByid(questionId);

        if (querstionByid == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        // 将相关的键值对从数据库中删除
        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.Question, questionId);

        // 发送事件模型
        if (ret) {
            //取消关注成功后发送信息
            eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW) //不关注的话，可以不发问题。
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(questionId).setExt("username", hostHolder.getUser().getName()).setEntityOwnerId(querstionByid.getUserId()));
        }
        //获取当前用户的id和粉丝总数 并返回
        Map<String, Object> info = new HashMap<>();
        info.put("id", hostHolder.getUser().getId());
        info.put("count", followService.getFolloerCount(EntityType.Question, questionId));

        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    /**
     * 查看某用户的粉丝列表 用户粉丝界面设置
     *
     * @param model  html事件模型 将数据传输给前端
     * @param userid 用户id
     * @return 返回followers的html界面
     */
    @RequestMapping(value = "/user/{uid}/followers", method = RequestMethod.GET)
    public String followers(Model model, @PathVariable("uid") int userid) {
        // 获取粉丝的id列表
        List<Integer> followers = followService.getFollers(EntityType.User, userid, 10);

        if (hostHolder.getUser() != null) {
            // 获取当前登录用户所有粉丝的信息
            model.addAttribute("followers", getUsersInfo(hostHolder.getUser().getId(), followers));
        } else {
            model.addAttribute("followers", getUsersInfo(
                    0, followers
            ));
        }

        // 当前用户的粉丝个数
        model.addAttribute("followerCount", followService.getFolloerCount(
                EntityType.User, userid
        ));
        // 设置访问的用户的基本信息
        model.addAttribute("curUser", userService.getUserById(userid));
        return "followers";
    }

    /**
     * 用户的关注页面 后台设置
     *
     * @param model  html事件模型 将数据传输给前端
     * @param userId 用户id
     * @return 返回followees的html界面
     */
    @RequestMapping(value = "/user/{uid}/followees", method = RequestMethod.GET) //查看某个用户所有的关注列表
    public String followees(Model model, @PathVariable("uid") int userId) {
        // 获取偶像的id列表
        List<Integer> followeeIds = followService.getFollees(EntityType.User, userId, 0, 10);

        if (hostHolder.getUser() != null) {
            //关注偶像的信息
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followeeIds));
        } else {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));
        }
        // 获取用户的偶像数
        model.addAttribute("followeeCount", followService.getFollweeCount(userId, EntityType.User));
        // 设置访问的用户的基本信息
        model.addAttribute("curUser", userService.getUserById(userId));
        return "followees";
    }

    /**
     * 找出List<Integer> userIds 对象所有的信息，包括当前访问者和他的信息，以及他的关注人数，粉丝人数等等
     *
     * @param localUserid 本地用户id
     * @param userIds     粉丝ids
     * @return
     */
    private List<ViewObject> getUsersInfo(int localUserid, List<Integer> userIds) {
        List<ViewObject> userInfos = new ArrayList<>();
        for (Integer uid : userIds) {
            //查询每个用户信息，包括用户基本信息，用户粉丝，用户关注，用户评论
            User userById = userService.getUserById(uid);
            if (userById == null) continue;
            ViewObject vo = new ViewObject();
            //设置用户基本信息
            vo.set("user", userById);
            //设置用户评论个数
            vo.set("commentCount", commentService.commentByUserId(uid));
            //设置用户粉丝个数
            vo.set("followerCount", followService.getFolloerCount(
                    EntityType.User, uid
            ));
            //设置每个用户赞的个数
            vo.set("zanNum", commentService.getAllZanCommentByUserId(uid));
            //设置偶像的个数
            vo.set("followeeCount", followService.getFollweeCount(
                    uid, EntityType.User
            ));
            if (localUserid != 0) {//查看当前登录用户id和粉丝之间的关系
                vo.set("followed", followService.isFollower(localUserid, EntityType.User,
                        uid));
            } else {
                vo.set("followed", false);
            }
            userInfos.add(vo);

        }

        return userInfos;

    }

    /**
     * 根据用户id获取他关注的问题
     *
     * @param uid 用户id
     * @return 关注的问题list集合
     */
    @RequestMapping("/getFollowQuerstionsByUserId")
    @ResponseBody
    public List<Question> getFollowQuestionsByUserId(@RequestParam("uid") int uid) {
        List<Question> list = followService.getFollowQuestionsByUserId(uid);
        return list;
    }
}
