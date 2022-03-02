package com.zxc.zhihu.async;

import java.util.List;

/**
 * 2021
 */
public interface EventHandler {
    //事件实现类实现这个接口
    void doHandle(EventModel model);

    List<EventType> getSupportEventTypes();
}
