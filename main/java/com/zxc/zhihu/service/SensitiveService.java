package com.zxc.zhihu.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 过滤 提问模块：标题和内容中的敏感词
 */
@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    /**
     * 默认敏感词替换符
     */
    private static final String DEFAULT_REPLACEMENT = "***";

    /**
     * 定义前缀树
     * 特点：1.根节点没有字符 2.除了根节点每个节点都只有一个字符 3.每个节点所有的子节点的字符都不相同
     */
    private class TrieNode {

        //表示是否为关键词的结尾: true 关键词搜索结束 ； false 继续
        private boolean end = false;

        //子节点: key:字符 value:子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        //添加子节点
        void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        //获取子节点
        TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        //判断是否是关键词的结尾
        boolean isKeywordEnd() {
            return end;
        }

        // 设置结束关键词
        void setKeywordEnd(boolean end) {
            this.end = end;
        }

    }

    /**
     * 将敏感词添加到前缀树之中
     *
     * @param lineTxt 输入参数：敏感词字符串
     */
    private void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;
        // 循环字符串 树中的节点保存的是字符
        for (int i = 0; i < lineTxt.length(); ++i) {
            Character c = lineTxt.charAt(i);
            // 过滤符号
            if (isSymbol(c)) {
                continue;
            }

            //获取该树的子节点
            TrieNode node = tempNode.getSubNode(c);

            //如果子节点中不存在该字符节点
            if (node == null) {
                node = new TrieNode();//下一个节点
                tempNode.addSubNode(c, node);
            }

            // 指向下一个节点 开始下一个循环
            tempNode = node;

            if (i == lineTxt.length() - 1) {
                // 关键词结束， 设置结束标志
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 敏感词过滤算法
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        //替换词
        String replacement = DEFAULT_REPLACEMENT;

        // 指针1：初始时指向前缀树的root根节点
        TrieNode tempNode = rootNode;
        // 指针2：初始时指向待过滤字符的头结点，表示敏感词的开头
        int begin = 0;
        //指针3：初始时指向待过滤字符的头结点，表示敏感词的末尾
        int position = 0;

        //StringBuilder接收返回值
        StringBuilder result = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            //如果是例如/ *等符号
            if (isSymbol(c)) {
                //如果指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    result.append(c);
                    ++begin;
                }
                //无论符号在开头还是中间，指针3都向下走一步
                ++position;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNode(c);

            //该符号的子节点为空 自然就不是敏感词 敏感词字典树
            if (tempNode == null) {
                result.append(text.charAt(begin));
                // 跳到下一个字符开始测试
                position = begin + 1;
                begin = position;
                // 回到树初始节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) { // 到达敏感词结尾了
                // 在result结果函数中进行敏感词的替换
                result.append(replacement);
                position = position + 1;
                begin = position;
                // 回到树初始节点
                tempNode = rootNode;
            } else {
                //是敏感词的部分了
                position++;
            }
        }

        result.append(text.substring(begin));

        return result.toString();
    }


    /**
     * 根节点
     */
    private TrieNode rootNode = new TrieNode();


    /**
     * 判断是否是一个符号
     */
    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    /**
     * 将敏感词表的内容输入树之中 IO流
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        rootNode = new TrieNode();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                lineTxt = lineTxt.trim();
                addWord(lineTxt);
            }
            read.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());
        }
    }

/*    public static void main(String[] argv) {
        SensitiveService s = new SensitiveService();
        s.addWord("赌博");
        s.addWord("嫖娼");
        System.out.print(s.filter("这里不可以赌博和嫖娼！！"));
    }
    */
}
