package com.ysx;

import com.ysx.common.constant.Constant;
import com.ysx.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/12/18:28
 * @Description:
 */

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisUtil redisUtil;

    @Resource(name="redisTemplate")
    private RedisTemplate<String, Object> template;

    @Test
    void test(){
        HashOperations<String, Object, Object> ops = template.opsForHash();
        /*HASH表名 key名*/
    }

}
