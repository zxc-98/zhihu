package com.zxc.zhihu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.Set;


/**
 * redis类
 */
@Service
public class JedisAdapter implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);
    private JedisPool pool;


    public static void print(int index, Object obj) {
        System.out.println(String.format("%d, %s", index, obj.toString()));
    }

    /**
     * 测试用例 用于熟悉jedis api
     */
  /*  public static void main(String[] argv) {
        Jedis jedis = new Jedis("redis://localhost:6379/9");//redis服务器默认会创建16个数据库 每个客户端都有自己目标的数据库
        jedis.flushDB();//清空当前数据库中的数据

        // get set
        jedis.set("hello", "world");ss
        print(1, jedis.get("hello"));
        jedis.rename("hello", "newhello");
        print(1, jedis.get("newhello"));
        jedis.setex("hello2", 1800, "world");

        //对数据的值进行加减操作
        jedis.set("pv", "100");

        jedis.incr("pv");//101 将key对应的value 加1
        jedis.incrBy("pv", 5);//+5 将key对应的value +5
        print(2, jedis.get("pv"));

        jedis.decrBy("pv", 2);//-2 将key对应的value -2
        print(2, jedis.get("pv"));

        print(3, jedis.keys("*"));*/


    //关于list集合的操作
        /*String listName = "list";
        //jedis.del(listName);
        for (int i = 0; i < 10; ++i) {
            jedis.lpush(listName, "a" + String.valueOf(i));
        }
        //从该范围中取出值 lrange
        print(4, jedis.lrange(listName, 0, 12));
        print(4, jedis.lrange(listName, 0, 3));
        //list集合的长度
        print(5, jedis.llen(listName));
        //将最左边的元素弹出并删除
        print(6, jedis.lpop(listName));
        print(7, jedis.llen(listName));

        print(8, jedis.lrange(listName, 2, 6));
        //list集合中指定索引的元素取出
        print(9, jedis.lindex(listName, 3));
        //在a4后面插入值"xx"
        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.AFTER, "a4", "xx"));
        //在a4前面插入值"bb"
        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.BEFORE, "a4", "bb"));
        print(11, jedis.lrange(listName, 0 ,12));*/

    // 关于hash对象的操作
        /*String userKey = "userxx";
        jedis.hset(userKey, "name", "jim");
        jedis.hset(userKey, "age", "12");
        jedis.hset(userKey, "phone", "18618181818");
        // 获取hash对象的对应键值 hget
        print(12, jedis.hget(userKey, "name"));
        // 获取所有对象的键值对 hgetAll
        print(13, jedis.hgetAll(userKey));
        // 将hash对象的某一键值对删除 hdel
        jedis.hdel(userKey, "phone");
        print(14, jedis.hgetAll(userKey));
        // 判断hash对象的键值对是否存在
        print(15, jedis.hexists(userKey, "email"));
        print(16, jedis.hexists(userKey, "age"));
        // 获取hash对象的键集和值集
        print(17, jedis.hkeys(userKey));
        print(18, jedis.hvals(userKey));
        // 如果键存在就修改值，如果键不存在就添加键值对
        jedis.hsetnx(userKey, "school", "chd");
        jedis.hsetnx(userKey, "name", "zxc");
        print(19, jedis.hgetAll(userKey));*/


    // set集合
        /*String likeKey1 = "commentLike1";
        String likeKey2 = "commentLike2";
        for (int i = 0; i < 10; ++i) {
            jedis.sadd(likeKey1, String.valueOf(i));
            jedis.sadd(likeKey2, String.valueOf(i*i));
        }
        // 遍历整个集合
        print(20, jedis.smembers(likeKey1));
        print(21, jedis.smembers(likeKey2));
        // 两个set集合的并集
        print(22, jedis.sunion(likeKey1, likeKey2));
        // 属于集合1且不属于集合2的集合
        print(23, jedis.sdiff(likeKey1, likeKey2));
        // 两个集合的交集
        print(24, jedis.sinter(likeKey1, likeKey2));

        // 判断集合中是否存在某个元素
        print(25, jedis.sismember(likeKey1, "12"));
        print(26, jedis.sismember(likeKey2, "16"));

        // 将集合中的某个元素删除 srem
        jedis.srem(likeKey1, "5");
        print(27, jedis.smembers(likeKey1));
        // 将集合1中的元素移到集合2 smove
        jedis.smove(likeKey2, likeKey1, "25");
        print(28, jedis.smembers(likeKey1));
        print(28, jedis.smembers(likeKey2));
        // 判断集合中的元素个数 scard
        print(29, jedis.scard(likeKey1));*/

    //zset有序集合对象
        /*String rankKey = "rankKey";
        jedis.zadd(rankKey, 15, "jim");
        jedis.zadd(rankKey, 60, "Ben");
        jedis.zadd(rankKey, 90, "Lee");
        jedis.zadd(rankKey, 75, "Lucy");
        jedis.zadd(rankKey, 80, "Mei");
        // 得出有序集合的数量 zcard
        print(30, jedis.zcard(rankKey));
        // 分数在某一范围内的数量
        print(31, jedis.zcount(rankKey, 61, 100));
        print(32, jedis.zscore(rankKey, "Lucy"));

        // 将'Lucy'的分数加2分 zincrby
        jedis.zincrby(rankKey, 2, "Lucy");
        print(33, jedis.zscore(rankKey, "Lucy"));

        // 如果不存在元素Luc 则是创建该元素 并分数+2
        jedis.zincrby(rankKey, 2, "Luc");
        print(34, jedis.zscore(rankKey, "Luc"));

        // 查询分数从小到大的键
        print(35, jedis.zrange(rankKey, 0, 100));
        print(36, jedis.zrange(rankKey, 0, 10));
        print(36, jedis.zrange(rankKey, 1, 3));
        // 从大到小的键
        print(36, jedis.zrevrange(rankKey, 1, 3));

        // 获取集合和分数的元素
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, "60", "100")) {
            print(37, tuple.getElement() + ":" + String.valueOf(tuple.getScore()));
        }

        // 获取从小到大的排名 zrank
        print(38, jedis.zrank(rankKey, "Ben"));
        // zrevrank 获取Ben从大到小的排名zrevrank
        print(39, jedis.zrevrank(rankKey, "Ben"));*/

    // 有序集合不仅仅有list还有set
        /*String setKey = "zset";
        jedis.zadd(setKey, 1, "a");
        jedis.zadd(setKey, 1, "b");
        jedis.zadd(setKey, 1, "c");
        jedis.zadd(setKey, 1, "d");
        jedis.zadd(setKey, 1, "e");

        // 制定区间内成员的数量
        print(40, jedis.zlexcount(setKey, "-", "+"));
        print(41, jedis.zlexcount(setKey, "(b", "[d"));//2
        print(42, jedis.zlexcount(setKey, "[b", "[d"));//3
        // zrem将有序set集合中某成员删除
        jedis.zrem(setKey, "b");
        print(43, jedis.zrange(setKey, 0, 10));
        // 将集合中某一个范围内的元素删除
        jedis.zremrangeByLex(setKey, "(c", "+");
        print(44, jedis.zrange(setKey, 0 ,2));*/

/*        // 获取Jedis的连接池
        JedisPool pool = new JedisPool();
        for (int i = 0; i < 100; ++i) {
            Jedis j = pool.getResource();
            print(45, j.get("pv"));
            j.close();
        }*/

    // 关于jedis和实体类Bean对象的存取
/*        User user = new User();
        user.setName("xx");
        user.setPassword("ppp");
        user.setHeadUrl("a.png");
        user.setSalt("salt");
        user.setId(1);
        // 将对象转换成JSON字符串集合
        print(46, JSONObject.toJSONString(user));
        // 存入jedis对象  值：user对象的toJSONString方法
        jedis.set("user1", JSONObject.toJSONString(user));

        // 获取jedis的值
        String value = jedis.get("user1");
        // 将JSONString转换成User对象
        User user2 = JSON.parseObject(value, User.class);
        print(47, user2);*/


    // 关于redis的事物
        /*int k = 2;
        try {
            Transaction tx = jedis.multi();
            tx.zadd("qq", 2, "1");
            tx.zadd("qq2", 3, "2");
            List<Object> objs = tx.exec();
            tx.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        k = 2;*/
    /*}*/
    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool("localhost", 6379);

    }

    /**
     * sadd：set集合添加元素
     *
     * @param key
     * @param value
     * @return
     */
    public long sadd(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * set集合中进行元素的删除
     *
     * @param key
     * @param value
     * @return
     */
    public long srem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srem(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * redis中返回set集合中元素的数量
     *
     * @param key
     * @return
     */
    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * 有序集合中元素的数量
     *
     * @param key
     * @return
     */
    public long zscard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * set集合判断是否存在键值对
     *
     * @param key
     * @param value
     * @return
     */
    public boolean sismember(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * 假如在指定时间内没有任何元素被弹出，则返回一个 nil 和等待时长。 反之，返回一个含有两个元素的列表，第一个元素是被弹出元素所属的 key ，第二个元素是被弹出元素的值。
     *
     * @param timeout
     * @param key
     * @return
     */
    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * list集合从左入栈
     *
     * @param key
     * @param value
     * @return
     */
    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * list集合从左到右遍历
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> lrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 有序集合添加元素
     *
     * @param key
     * @param score
     * @param value
     * @return
     */
    public long zadd(String key, double score, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zadd(key, score, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * 有序集合删除元素
     *
     * @param key
     * @param value
     * @return
     */
    public long zrem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrem(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * 获取Jedis的连接
     *
     * @return
     */
    public Jedis getJedis() {
        return pool.getResource();
    }

    /**
     * 开启事务
     *
     * @param jedis
     * @return
     */
    public Transaction multi(Jedis jedis) {
        try {
            return jedis.multi();
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
        }
        return null;
    }

    /**
     * 执行事务
     *
     * @param tx
     * @param jedis
     * @return
     */
    public List<Object> exec(Transaction tx, Jedis jedis) {
        try {
            return tx.exec();
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            tx.discard();//事务回滚
        } finally {
            if (tx != null) {
                try {
                    tx.close();
                } catch (IOException ioe) {
                    // ..
                }
            }

            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 遍历有序集合
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 反向遍历有序集合
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();
            return jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 得到有序集合的数量
     *
     * @param key
     * @return
     */
    public long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * 获取有序集合中的某一值的分值
     *
     * @param key
     * @param member
     * @return
     */
    public Double zscore(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }
}
