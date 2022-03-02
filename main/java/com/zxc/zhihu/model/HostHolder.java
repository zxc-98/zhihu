package com.zxc.zhihu.model;

import org.springframework.stereotype.Component;

/**
 * 一个user信息的线程容器
 */
@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();//从线程中删除
    }
}
