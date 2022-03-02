package com.zxc.zhihu.service;

import com.zxc.zhihu.util.JedisAdapter;
import com.zxc.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * zxc
 * 2021
 */
@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;

    //一个问题，或者回答对应两个set集合，分别是点赞集合，点踩集合。根据问题类型，问题id生成对应的key

    /**
     * 点赞
     *
     * @param userid    用户id
     * @param type      类型
     * @param commentId 评论的id
     * @return
     */
    public long like(Integer userid, int type, int commentId) {
        //某个人userid，喜欢类型为type，id=commmentid的问题或者回答,1表示问题，2表示回答
        //喜欢该问题的评论的redis键:LIKE:entityType:entityId
        String likeQuestionCommentKey = RedisKeyUtil.getLikeKey(type, commentId);
        //
        jedisAdapter.sadd(likeQuestionCommentKey, userid + "");//把点赞或者点踩的用户id放到set集合中

        //点踩的集合
        String disLikeKey = RedisKeyUtil.getDisLikeKey(type, commentId);
        //删除当前的用户
        jedisAdapter.srem(disLikeKey, userid + "");


        //返回所有的点赞人数
        return jedisAdapter.scard(likeQuestionCommentKey);
    }

    /**
     * 点踩
     *
     * @param useid     用户id
     * @param type      类型
     * @param commentId 评论的id
     * @return 点踩总数
     */
    public long dislike(Integer useid, int type, int commentId) {

        //评论点踩
        String disLikeKey = RedisKeyUtil.getDisLikeKey(type, commentId);
        jedisAdapter.sadd(disLikeKey, useid + "");


        //评论点赞
        String likeQuestionCommentKey = RedisKeyUtil.getLikeKey(type, commentId);
        jedisAdapter.srem(likeQuestionCommentKey, useid + "");


        //返回所有的点赞人数
        return jedisAdapter.scard(disLikeKey);
    }

    /**
     * 获取点赞数
     *
     * @param type 事件类型
     * @param cid  当前用户id
     * @return 点赞数
     */
    public long getLikeCount(int type, int cid) {
        String likeKey = RedisKeyUtil.getLikeKey(type, cid);
        return jedisAdapter.scard(likeKey);
    }

    /**
     * 判断当前用户点赞状态
     *
     * @param userid
     * @param type
     * @param cid
     * @return
     */
    public long getLikeStatus(int userid, int type, int cid) {
        //redis判断当前用户是否在点赞的集合中，如果在的话，返回1。表示喜欢
        String likeKey = RedisKeyUtil.getLikeKey(type, cid);//LIKE:type:cid
        if (jedisAdapter.sismember(likeKey, userid + "")) {
            return 1;
        }


        //判断是否在点踩的集合中，如果在的话，就返回-1，否则说明。用户不喜欢，也不讨厌。返回0
        String disLikeKey = RedisKeyUtil.getDisLikeKey(type, cid);
        return jedisAdapter.sismember(disLikeKey, userid + "") ? -1 : 0;
    }


}
