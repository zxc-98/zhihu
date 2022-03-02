package com.zxc.zhihu.async;

/**
 * 事件类型  枚举类
 */
public enum EventType {
    LIKE(0),//点赞
    COMMENT(1),//评论
    LOGIN(2),//登录
    MAIL(3),//邮件
    FOLLOW(4),//关注
    UNFOLLOW(5),//取消关注
    ADD_QUESTION(6),//添加问题模型
    ANSWER_QUESTION(7), //回答问题模型
    FOLLOW_USER(8); //关注用户
    // PERSONAL_TIMELINE(8);//个人时间轴信息

    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
