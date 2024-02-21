package com.xuecheng.content.config;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisHandler implements InitializingBean {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<CoursePublish> coursePublishList = coursePublishMapper.selectList(new LambdaQueryWrapper<CoursePublish>());
        //缓存预热
        coursePublishList.forEach(coursePublish -> {
            String key = "content:course:publish:" + coursePublish.getId();
            redisTemplate.opsForValue().set(key, JSON.toJSONString(coursePublish));
        });
    }
}
