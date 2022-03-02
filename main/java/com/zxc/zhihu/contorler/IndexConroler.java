package com.zxc.zhihu.contorler;

import com.zxc.zhihu.dao.UserMapper;
import com.zxc.zhihu.model.*;
import com.zxc.zhihu.service.CommentService;
import com.zxc.zhihu.service.FollowService;
import com.zxc.zhihu.service.QuestionService;
import com.zxc.zhihu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/20 16:54
 */
@Controller
public class IndexConroler {

    @Autowired
    UserMapper userMapper;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    FollowService followService;
    @Autowired
    HostHolder hostHolder;

    /* @Author zxc
     * @Description To Do,查询某个用户最新的十条记录
     * @Date 2021
     * @Param
     * @return
     */
    @RequestMapping(value = "/user/{userid}", method = RequestMethod.GET)
    public String showUserIndex(@PathVariable int userid, Model model) {
        //获得该用户最新的十条动态
        model.addAttribute("vos", getLastQuestion(userid, 0, 10));

        /*${profileUser.user.name}
        ${profileUser.followerCount}粉丝 /
       ${profileUser.followeeCount}关注 /
        ${profileUser.commentCount} 回答 / 548 赞同*/

        User userById = userService.getUserById(userid);
        ViewObject viewObject = new ViewObject();
        //设置用户信息，包含了用户名
        viewObject.set("user", userById);
        //设置用户回答的个数 vo.question.commentCount
        viewObject.set("commentCount", commentService.commentByUserId(userid));
        //设置用户粉丝数
        viewObject.set("followerCount", followService.getFolloerCount(
                EntityType.User, userid
        ));
        //设置跟随了多少人。
        viewObject.set("followeeCount", followService.getFollweeCount(
                userid, EntityType.User
        ));

        //判断当前用户是不是访问页面用户的粉丝
        if (hostHolder.getUser() != null) {
            viewObject.set("followed",
                    followService.isFollower(hostHolder.getUser().getId(),
                            EntityType.User, userid));
        } else {
            viewObject.set("followed", false);
        }

        /*http://localhost:8080/user/19*/
        /* 查询redis数据库，该用户获得过多少个赞*/
        long zanNum = commentService.getAllZanCommentByUserId(userid);
        model.addAttribute("zanNum", zanNum);

        model.addAttribute("profileUser", viewObject);
        return "profile";
    }


    /* @Author zxc
     * @Description To Do 查询最新的十条记录
     * @Date 2021
     * @Param
     * @return
     */
    @RequestMapping(path = {"/", "index"}, method = RequestMethod.GET)
    public String showIndex(Model model) {
        //查询推荐  的十个问题，显示到index网页中
        model.addAttribute("vos", getQuestion(0, 0, 100));

        //followCount
        return "index";

    }

    @RequestMapping(value = "insertUser", method = RequestMethod.GET)
    public String addUser() {
        User user = new User();
        user.setId(8);
        user.setName("zxc");
        user.setPassword("123");
        user.setSalt("100");
        user.setHeadUrl("http");
        userMapper.insert(user);

        return "index";
    }

    /**
     * 根据用户id，查询最新的十条问题
     *
     * @param id
     * @param off
     * @param limit
     * @return
     */
    private List<ViewObject> getLastQuestion(int id, int off, int limit) {
        List<Question> lastQuersion = questionService.getLastQuersion(id, off, limit);
        List<ViewObject> vos = new ArrayList<>();
        for (int i = 0; i < lastQuersion.size(); i++) {
            /*Random random = new Random();
            int randomNum = random.nextInt(100);*/
            Question question = lastQuersion.get(i);
            ViewObject viewObject = new ViewObject();
            viewObject.set("question", question);
            viewObject.set("user", userService.getUserById(question.getUserId()));
            long folloerCount = followService.getFolloerCount(EntityType.Question, question.getId());
            //设置首页，每次显示十个问题，每个问题的关注人数。根据 实体类型EntityType.question 实体id
            viewObject.set("followCount", folloerCount);
            vos.add(viewObject);
        }
        return vos;
    }

    /**
     * 查询推荐的15个问题
     *
     * @param id
     * @param off
     * @param limit
     * @return
     */
    private List<ViewObject> getQuestion(int id, int off, int limit) {
        List<Question> lastQuersion = questionService.getLastQuersion(id, off, limit);
        List<ViewObject> vos = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Random random = new Random();
            int randomNum = random.nextInt(100);
            Question question = lastQuersion.get(randomNum);
            ViewObject viewObject = new ViewObject();
            viewObject.set("question", question);
            viewObject.set("user", userService.getUserById(question.getUserId()));
            long folloerCount = followService.getFolloerCount(EntityType.Question, question.getId());
            //设置首页，每次显示十个问题，每个问题的关注人数。根据 实体类型EntityType.question 实体id
            viewObject.set("followCount", folloerCount);
            vos.add(viewObject);
        }
        return vos;
    }
}
