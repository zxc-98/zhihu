package com.zxc.zhihu.util;

/**
 * redis键的工具类
 */
public class RedisKeyUtil {
    private static String SPLIT = ":";
    private static String BIZ_LIKE = "LIKE";
    private static String BIZ_DISLIKE = "DISLIKE";
    private static String BIZ_EVENTQUEUE = "EVENT_QUEUE";
    // 获取粉丝
    private static String BIZ_FOLLOWER = "FOLLOWER";
    // 关注对象
    private static String BIZ_FOLLOWEE = "FOLLOWEE";
    private static String BIZ_TIMELINE = "TIMELINE";


    //个人时间轴，这个时间轴上只有自己的信息，比如自己关注了别人，关注了问题，发布了问题。
    private static String BIZ_PersonTimeline = "PersonTimelinev";

    public static String getBIZ_PersonTimeline(int userid) {
        return BIZ_PersonTimeline + SPLIT + String.valueOf(userid);
    }

    //获取点赞的键:LIKE:entityType:entityId
    public static String getLikeKey(int entityType, int entityId) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    //获取点踩的键：DISLIKE:entityType:entityId
    public static String getDisLikeKey(int entityType, int entityId) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    //获取消息队列的键
    public static String getEventQueueKey() {
        return BIZ_EVENTQUEUE;
    }

    // followerKey:FOLLOWER:entityType:entityId
    public static String getFollowerKey(int entityType, int entityId) {
        return BIZ_FOLLOWER + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    // followeeKey:FOLLOWEE:userId:entityType
    public static String getFolloweeKey(int userId, int entityType) {
        return BIZ_FOLLOWEE + SPLIT + String.valueOf(userId) + SPLIT + String.valueOf(entityType);
    }

    // 消息流：推拉模式的键 = PersonTimelinev:userId
    public static String getTimelineKey(int userId) {
        return BIZ_TIMELINE + SPLIT + String.valueOf(userId);
    }
}
