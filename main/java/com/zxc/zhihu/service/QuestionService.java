package com.zxc.zhihu.service;

import com.zxc.zhihu.dao.QuestionMapper;
import com.zxc.zhihu.model.Question;
import com.zxc.zhihu.model.QuestionExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author zxc
 * @date 2021
 */
@Service
public class QuestionService {
    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    SearchService SearchService;


    /**
     * @param userid
     * @param off
     * @param limit
     * @return
     */
    public List<Question> getLastQuersion(int userid, int off, int limit) {
        List<Question> lastQuestion = questionMapper.getLastQuestion(userid, off, limit);
        return lastQuestion;
    }

    public int addQuestion(Question question) {

        //过滤掉html 防止xss注入 <script>alert('papapa')</script> 这样的名称 将其转义
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));

        //敏感词过滤
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));

        // 将问题数据放入数据库中
        int i = questionMapper.insertSelective(question);

        return i > 0 ? 1 : 0;
    }

    public Question getQuerstionByid(int id) {
        Question question = questionMapper.selectByPrimaryKey(id);
        return question;
    }

    //修改数据库某个问题的评论个数
    public int modifyCountNum(int questinid, int count) {

        Question question = new Question();
        question.setCommentCount(count);//更新的内容项目

        QuestionExample questionExample = new QuestionExample();
        QuestionExample.Criteria criteria = questionExample.createCriteria();
        criteria.andIdEqualTo(questinid);//更新的条件

        int i = questionMapper.updateByExampleSelective(question, questionExample);
        return i;
    }

    //根据用户id获得他提问的所有问题
    public List<Question> getQuestionsByUserId(int uid) {
        QuestionExample questionExample = new QuestionExample();
        QuestionExample.Criteria criteria = questionExample.createCriteria();
        criteria.andUserIdEqualTo(uid);

        List<Question> questions = questionMapper.selectByExampleWithBLOBs(questionExample);
        return questions;
    }
}
