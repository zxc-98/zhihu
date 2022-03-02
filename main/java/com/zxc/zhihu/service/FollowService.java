package com.zxc.zhihu.service;

import com.zxc.zhihu.model.EntityType;
import com.zxc.zhihu.model.Question;
import com.zxc.zhihu.util.JedisAdapter;
import com.zxc.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


/* @Author zxc
 * @Description a关注B。a是b的粉丝。
 * @Date 2019/1/23 9:54
 * @Param
 * @return
 */
@Service
public class FollowService {
    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;

    /**
     * 关注
     *
     * @param userid      关注者id --> 当前登录用户id
     * @param entitiyType 1 问题，2 回答 3 用户
     * @param entityId    被关注实体类id
     * @return 返回值应该是是否关注成功
     */
    public boolean follow(int userid, int entitiyType, int entityId) {

        if (userid == entityId && (entitiyType == EntityType.User)) {
            //说明是关注自己。
            return false;
        }

        //两者之间的差别是什么
        //followerKey=FOLLOWER:entityType:entityId  value=userid  (键：被关注者 值：粉丝)
        String followerKey = RedisKeyUtil.getFollowerKey(entitiyType, entityId);
        //followeeKey:FOLLOWEE:userid:entityType   value=entityId (键：追随者 值：偶像)
        String followeeKey = RedisKeyUtil.getFolloweeKey(userid, entitiyType);

        Date date = new Date();

        // 获取jedis的连接
        Jedis jedis = jedisAdapter.getJedis();
        // 开启事务
        Transaction tx = jedisAdapter.multi(jedis);

        // 事务1 键(followerKey) 分值(date.getTime()) 成员(userid)
        // followerKey添加粉丝  时间表示权重  键：被关注者 值：粉丝
        tx.zadd(followerKey, date.getTime(), String.valueOf(userid));

        // 事务2 键(followeeKey) 分值(date.getTime()) 成员(entityId)
        // followeeKey添加偶像
        tx.zadd(followeeKey, date.getTime(), String.valueOf(entityId));

        // 执行事务  返回值是什么？
        List<Object> ret = jedisAdapter.exec(tx, jedis);


        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
    }

    /**
     * 取消关注
     *
     * @param userid      关注者id --> 当前登录用户id
     * @param entitiyType 1 问题，2 回答 3 用户
     * @param entityId    被关注实体类id
     * @return 返回值应该是是否取消关注成功
     */
    public boolean unfollow(int userid, int entitiyType, int entityId) {

        //followerKey=FOLLOWER:entityType:entityId  value=userid  (键：被关注者 值：粉丝)
        String followerKey = RedisKeyUtil.getFollowerKey(entitiyType, entityId);
        //followeeKey:FOLLOWER:userid:entityType   value=entityId (键：追随者 值：偶像)
        String followeeKey = RedisKeyUtil.getFolloweeKey(userid, entitiyType);

        Date date = new Date();

        // 获取jedis的连接
        Jedis jedis = jedisAdapter.getJedis();
        // 开启事务
        Transaction tx = jedisAdapter.multi(jedis);

        // 从redis中将followerKey中的userid(粉丝)删除
        tx.zrem(followerKey, String.valueOf(userid));
        // 从redis中将followeeKey中的entityId删除
        tx.zrem(followeeKey, String.valueOf(entityId));

        List<Object> ret = jedisAdapter.exec(tx, jedis);

        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
    }

    /**
     * 从redis中获取被关注对象的粉丝
     *
     * @param entityType 1 问题，2 回答 3 用户
     * @param entityId   被关注实体类id
     * @param count      总数
     * @return 关注该对象的粉丝Id的list集合(一共count个)
     */
    public List<Integer> getFollers(int entityType, int entityId, int count) {
        //followerKey=FOLLOWER:entityType:entityId  value=userid  (键：被关注者 值：粉丝)
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        //这个set集合中放的是粉丝的userid
        Set<String> zrange = jedisAdapter.zrange(followerKey, 0, count);
        //将存放uid的set集合转换成list集合
        List<Integer> folloerIdsFromSet = getFollowerIdsFromSet(zrange);
        return folloerIdsFromSet;
    }

    /**
     * 从偏移量offset开始找count个粉丝,倒叙查找，从权重最大的开始找，也就是最新关注的粉丝
     *
     * @param entityType 1 问题，2 回答 3 用户
     * @param entityId   被关注实体类id
     * @param offset     偏移量
     * @param count      总数
     * @return 从偏移量开始的count个粉丝(按关注时间降序)
     */
    public List<Integer> getFollers(int entityType, int entityId, int offset, int count) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        Set<String> zrange = jedisAdapter.zrevrange(followerKey, offset, offset + count);
        //这个set集合中放的是粉丝的userid
        List<Integer> folloerIdsFromSet = getFollowerIdsFromSet(zrange);
        return folloerIdsFromSet;
    }


    /**
     * 从redis中获取用户的偶像或关注的问题
     *
     * @param userid     用户id
     * @param entityType 实体类型 1 问题，2 回答 3 用户
     * @param count      总数
     * @return 被关注对象的id集合
     */
    public List<Integer> getFollees(int userid, int entityType, int count) {
        //1 获取用户关注类型的key,按照时间从旧开始查找count个
        String followeeKey = RedisKeyUtil.getFolloweeKey(userid, entityType);//
        Set<String> zrange = jedisAdapter.zrange(followeeKey, 0, count);

        return getFollowerIdsFromSet(zrange);
    }

    /**
     * 从偏移量offset开始找count个关注的问题，或者人,倒叙查找，从权重最大的开始找，也就是最新的关注类型的id
     *
     * @param entityType
     * @param userId
     * @param offset
     * @param count
     * @return
     */
    public List<Integer> getFollees(int entityType, int userId, int offset, int count) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Set<String> zrevrange = jedisAdapter.zrevrange(followeeKey, offset, offset + count);
        return getFollowerIdsFromSet(zrevrange);
    }

    //测试类
    /*public static void main(String[] args) {
        JedisPool     pool = new JedisPool("192.168.25.128",6379);
        Jedis resource = pool.getResource();
        Set<String> zrevrange = resource.zrevrange(RedisKeyUtil.getFolloweeKey(19, EntityType.User), 0, 10);
        System.out.println(RedisKeyUtil.getFolloweeKey(19, EntityType.User));
       // FOLLOWEE:3:19
        System.out.println(zrevrange.size());
        resource.close();;
    }*/

    /**
     * 获取实体的粉丝个数
     *
     * @param entityType 实体类类型 1 问题，2 回答 3 用户
     * @param entityId   实体类id
     * @return 返回关注该实体的粉丝数
     */
    public long getFolloerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zcard(followerKey);
    }

    /**
     * 获取用户的偶像或者关注问题数
     *
     * @param userid     用户id
     * @param entityType 实体类类型 1 问题，2 回答 3 用户
     * @return 用户的偶像或者关注问题数
     */
    public long getFollweeCount(int userid, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userid, entityType);
        return jedisAdapter.zcard(followeeKey);
    }

    /**
     * 把set<string>集合转化成 list<integer>集合
     *
     * @param set set<string>
     * @return list<integer>
     */
    public List<Integer> getFollowerIdsFromSet(Set<String> set) {
        List<Integer> list = new ArrayList<>();
        for (String id : set) {
            list.add(Integer.parseInt(id));
        }

        return list;
    }

    /**
     * 判断某个用户是否关注了某个实体
     *
     * @param userid     用户id
     * @param entityType 实体类类型 1 问题，2 回答 3 用户
     * @param entityId   实体类id 问题id或者偶像id
     * @return
     */
    public boolean isFollower(int userid, int entityType, int entityId) {
        // followerKey=FOLLOWER:entityType:entityId  value=userid  (键：被关注者 值：粉丝)
        //String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        // 给出键值对应的分值
        String followeeKey = RedisKeyUtil.getFolloweeKey(userid, entityType);

        return jedisAdapter.zscore(followeeKey, String.valueOf(entityId)) != null;
    }

    /**
     * 获取当前登录用户的关注者提出的问题集合
     *
     * @param uid 用户id
     * @return
     */
    public List<Question> getFollowQuestionsByUserId(int uid) {
        //followeeKey:FOLLOWER:userid:entityType   value=entityId (键：追随者 值：偶像)
        //返回用户id关注问题的id集合
        Set<String> zrange = jedisAdapter.zrange(RedisKeyUtil.getFolloweeKey(uid, EntityType.Question), 0, -1);
        List<Integer> questionids = getFollowerIdsFromSet(zrange);

        //返回用户id关注的所有的问题实体类的集合
        List<Question> questionLists = new ArrayList<>();
        for (Integer qid : questionids) {
            questionLists.add(questionService.getQuerstionByid(qid));
        }
        return questionLists;
    }
}
