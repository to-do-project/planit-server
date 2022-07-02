package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.planz.planit.config.BaseResponseStatus.REDIS_ERROR;

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


    // refresh 토큰 10일간 저장하기
    public Boolean setRefreshTokenInRedis(String deviceTokenId, String refreshToken, Long expireTime) throws BaseException {
        try {
            String key = REFRESH_PREFIX + deviceTokenId;
            valueOperations.set(key, refreshToken);
            return stringRedisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("setRefreshTokenInRedis() : valueOperations.set() 혹은 stringRedisTemplate.expire() 실행 중 에러 발생");
            throw new BaseException(REDIS_ERROR);
        }

    }

    // refresh 토큰 비교하기
    public boolean compareRefreshTokenInRedis(String deviceTokenId, String refreshTokenInHeader) throws BaseException {
        try {
            String key = REFRESH_PREFIX + deviceTokenId;

            // 해당 userPk로 발급받은 refresh 토큰이 없다면 null 리턴
            String refreshTokenInRedis = valueOperations.get(key);
            if (refreshTokenInRedis == null) {
                return false;
            } else if (refreshTokenInRedis.equals(refreshTokenInHeader)) {
                return true;
            } else {
                return false;
            }
        }
        catch (Exception e){
            log.error("compareRefreshTokenInRedis() : valueOperations.get() 실행 중 에러 발생");
            throw new BaseException(REDIS_ERROR);
        }
    }

    // refresh 토큰 삭제하기
    public void deleteRefreshTokenInRedis(String deviceTokenId) throws BaseException {
        try {
            String key = REFRESH_PREFIX + deviceTokenId;
            stringRedisTemplate.delete(key);
        }
        catch (Exception e){
            log.error("deleteRefreshTokenInRedis() : stringRedisTemplate.delete() 실행 중 에러 발생");
            throw new BaseException(REDIS_ERROR);
        }
    }

    // 이메일 인증번호 5분간 저장하기
    public Boolean setEmailAuthNumInRedis(String email, String authNum) throws BaseException {
        try {
            String key = EMAIL_AUTH_NUM_PREFIX + email;
            valueOperations.set(key, authNum);
            return stringRedisTemplate.expire(key, EMAIL_AUTH_NUM_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        }
        catch (Exception e){
            log.error("setEmailAuthNumInRedis() : valueOperations.set() 혹은 stringRedisTemplate.expire() 실행 중 에러 발생");
            throw new BaseException(REDIS_ERROR);
        }
    }

    // 이메일 인증번호 가져오기
    public String getEmailAuthNumInRedis(String email) throws BaseException {
        try {
            String key = EMAIL_AUTH_NUM_PREFIX + email;

            // 해당 email로 발급받은 인증번호가 없다면 null 리턴
            return valueOperations.get(key);
        }
        catch (Exception e){
            log.error("getEmailAuthNumInRedis() : valueOperations.get() 실행 중 에러 발생");
            throw new BaseException(REDIS_ERROR);
        }
    }

}
