package com.zxc.zhihu.dao;

import com.zxc.zhihu.model.Feed;
import com.zxc.zhihu.model.FeedExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FeedMapper {
    int countByExample(FeedExample example);

    int deleteByExample(FeedExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Feed record);

    int insertSelective(Feed record);

    List<Feed> selectByExample(FeedExample example);

    /**
     * 根据id获取feed类
     *
     * @param id 用户id
     * @return 数据库feed类
     */
    Feed selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Feed record, @Param("example") FeedExample example);

    int updateByExample(@Param("record") Feed record, @Param("example") FeedExample example);

    int updateByPrimaryKeySelective(Feed record);

    int updateByPrimaryKey(Feed record);

    /**
     * 比如用户id=1（张三） 需要查找他关注的用户List<ids> 最近发生的新鲜事，count个
     *
     * @param maxId 最大id
     * @param ids   关注用户的id集合
     * @param count count个新鲜事
     * @return 返回关注用户们最新的count条新鲜事
     */
    List<Feed> queryUserFeeds(@Param("maxId") int maxId, @Param("ids") List<Integer> ids,
                              @Param("count") int count);
}