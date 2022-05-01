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

    private final String REFRESH_PREFIX = "REFRESH-TOKEN-";

    // 5분 => 1000 * 60 * 5
    private final String EMAIL_AUTH_NUM_PREFIX = "AUTH-NUM-";
    private final long EMAIL_AUTH_NUM_EXPIRE_TIME = 300000;

    private final StringRedisTemplate stringRedisTemplate;
    private final ValueOperations<String, String> valueOperations;

    @Autowired
    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.valueOperations = this.stringRedisTemplate.opsForValue();
    }

    // 나중에 key값이 usrePK값이 아닌 DeviceToken 테이블 PK로 변경되야함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    // refresh 토큰 저장하기
    public Boolean setRefreshTokenInRedis(String deviceTokenId, String refreshToken, Long expireTime){
        String key = REFRESH_PREFIX + deviceTokenId;
        valueOperations.set(key, refreshToken);
        return stringRedisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
    }

    // refresh 토큰 비교하기
    public boolean compareRefreshTokenInRedis(String deviceTokenId, String refreshTokenInHeader){
        String key = REFRESH_PREFIX + deviceTokenId;

        // 해당 userPk로 발급받은 refresh 토큰이 없다면 null 리턴
        String refreshTokenInRedis = valueOperations.get(key);
        if (refreshTokenInRedis == null){
            return false;
        }
        else if (refreshTokenInRedis.equals(refreshTokenInHeader)){
            return true;
        }
        else{
            return false;
        }
    }

    // refresh 토큰 삭제하기
    public void deleteRefreshTokenInRedis(String deviceTokenId){
        String key = REFRESH_PREFIX + deviceTokenId;
        stringRedisTemplate.delete(key);
    }

    // 이메일 인증번호 저장하기
    public Boolean setEmailAuthNumInRedis(String email, String authNum){
        String key = EMAIL_AUTH_NUM_PREFIX + email;
        valueOperations.set(key, authNum);
        return stringRedisTemplate.expire(key, EMAIL_AUTH_NUM_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    // 이메일 인증번호 가져오기
    public String getEmailAuthNumInRedis(String email){
        String key = EMAIL_AUTH_NUM_PREFIX + email;

        // 해당 email로 발급받은 인증번호가 없다면 null 리턴
        return valueOperations.get(key);
    }

}
