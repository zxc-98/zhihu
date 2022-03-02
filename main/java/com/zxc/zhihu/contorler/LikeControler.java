package com.zxc.zhihu.contorler;

import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventProducer;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.model.Comment;
import com.zxc.zhihu.model.HostHolder;
import com.zxc.zhihu.service.CommentService;
import com.zxc.zhihu.service.LikeService;
import com.zxc.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zxc
 * @date 2021
 */
@Controller
public class LikeControler {

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;

    /**
     * 评论点赞
     *
     * @param commentId 前端评论id
     * @return json结果语句
     */
    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(99, "请先登录");//
        }

        Comment commentById = commentService.getCommentById(commentId);

        //异步开始发送信息
        eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.getUser().getId())
                .setEntityType(EventType.LIKE.getValue()).setEntityId(commentId).setEntityOwnerId(commentById.getUserId())
                .setExt("questionId", commentById.getEntityId() + ""));


        long likeNum = likeService.like(hostHolder.getUser().getId(), EventType.LIKE.getValue(), commentId);

        return WendaUtil.getJSONString(0, likeNum + "");//返回问答喜欢的个数
    }


    /**
     * 评论点踩
     *
     * @param commentId 前端评论id
     * @return
     */
    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(99);
        }
        long dislike = likeService.dislike(hostHolder.getUser().getId(), EventType.LIKE.getValue(), commentId);

        return WendaUtil.getJSONString(0, dislike + "");//返回问答喜欢的个数
    }
}
