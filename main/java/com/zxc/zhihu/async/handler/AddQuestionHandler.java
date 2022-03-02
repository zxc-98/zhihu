package com.zxc.zhihu.async.handler;

import com.zxc.zhihu.async.EventHandler;
import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


/* @Author zxc
 * @Description 这个处理器的主要功能是，用户添加问题后，自动把这问题放到索引库中
 *
 * @Date 2021
 * @Param
 * @return
 */
@Component
public class AddQuestionHandler implements EventHandler {


    @Autowired
    SearchService searchService;

    @Override
    public void doHandle(EventModel model) {
        searchService.addQuestionToIndexDB(model.getEntityId(),
                model.getExt("title"), model.getExt("content"));//把问题标题内容同步到solr索引库
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.ADD_QUESTION);
    }
}
