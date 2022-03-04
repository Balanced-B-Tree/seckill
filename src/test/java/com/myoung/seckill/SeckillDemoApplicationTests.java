package com.myoung.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class SeckillDemoApplicationTests {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedisScript script;

    @Test
    public void testLock01() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean isLock = valueOperations.setIfAbsent("k1", "v1");
        if (isLock) {
            valueOperations.set("name", "ssh");
            String name = (String) valueOperations.get("name");
            System.out.println("name = " + name);
            redisTemplate.delete("k1");
        } else {
            System.out.println("有线程在占用");
        }
    }

    @Test
    public void testLock02() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 获取锁，使用setIfAbsent 方法，设置无效时间，防止释放不了锁
        boolean isLock = valueOperations.setIfAbsent("k1", "v1", 5, TimeUnit.SECONDS);
        if (isLock) {
            valueOperations.set("name", "ssh");
            String name = (String) valueOperations.get("name");
            System.out.println("name = " + name);
            // 释放锁
            redisTemplate.delete("k1");
        } else {
            System.out.println("有线程在占用");
        }
    }
    @Test
    public void testLock03() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String value = UUID.randomUUID().toString();
        // 获取锁，使用setIfAbsent 方法，设置无效时间，防止释放不了锁
        boolean isLock = valueOperations.setIfAbsent("k1", value, 5, TimeUnit.SECONDS);
        if (isLock) {
            valueOperations.set("name", "ssh");
            String name = (String) valueOperations.get("name");
            System.out.println("name = " + name);
            Boolean result = (Boolean) redisTemplate.execute(script, Collections.singletonList("k1"), value);
            // 释放锁
            if (result){
                redisTemplate.delete("k1");
            }

        } else {
            System.out.println("有线程在占用");
        }
    }

}
