package com.planz.planit.src.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class RedisService {

    private final String REFRESH_PREFIX = "REFRESH-";
    private final StringRedisTemplate stringRedisTemplate;
    private final ValueOperations<String, String> valueOperations;

    @Autowired
    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.valueOperations = this.stringRedisTemplate.opsForValue();
    }

    // refresh 토큰 저장하기
    public Boolean setRefreshTokenInRedis(String userPk, String refreshToken, Long expireTime){
        String key = REFRESH_PREFIX + userPk;
        valueOperations.set(key, refreshToken);
        return stringRedisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
    }

    // refresh 토큰 가져오기
    public String getRefreshTokenInRedis(String userPk){
        String key = REFRESH_PREFIX + userPk;

        // 해당 userPk로 발급받은 refresh 토큰이 없다면 null 리턴
        return valueOperations.get(key);
    }

    // refresh 토큰 삭제하기
    public void deleteRefreshTokenInRedis(String userPk){
        String key = REFRESH_PREFIX + userPk;
        stringRedisTemplate.delete(key);
    }
}
