package com.knu.ddip.DBTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redisConnectionTest() {
        redisTemplate.opsForValue().set("connectionTest", "success");
        String result = redisTemplate.opsForValue().get("connectionTest");

        Assertions.assertEquals("success", result, "Redis 연결 실패");
        System.out.println("Redis 연결 성공: " + result);

        redisTemplate.delete("connectionTest");
    }
}
