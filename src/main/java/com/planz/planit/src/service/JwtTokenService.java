package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
public class JwtTokenService {

    // Base64로 인코딩된 JWT 시크릿키
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    // access token 유효 시간 30분 = 1000 * 60 * 30
    @Value("${jwt.access-token-expire-time}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    // refresh token 유효 시간 10 일 = 1000 * 60 * 60 * 24 * 10
    @Value("${jwt.refresh-token-expire-time}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    // refresh token 재발급 기준 시간 2일 = 1000 * 60 * 60 * 24 * 2
    @Value("${jwt.refresh-token-reissue-time}")
    private long REFRESH_TOKEN_REISSUE_TIME;

    @Value("${jwt.access-token-header-name}")
    private String ACCESS_TOKEN_HEADER_NAME;

    @Value("${jwt.refresh-token-header-name}")
    private String REFRESH_TOKEN_HEADER_NAME;

    private final RedisService redisService;

    @Autowired
    public JwtTokenService(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * Request Header에서 access token 값을 가져와서 리턴한다. (Bearer 문자열은 제외하고 토큰 값만 리턴)
     * 1. Request Header에 access token이 없으면 null 리턴
     * 2. access token이 Bearer로 시작하지 않으면 null 리턴
     */
    public String getAccessToken(HttpServletRequest request){
        String jwtAccessHeader = request.getHeader(ACCESS_TOKEN_HEADER_NAME);

        if (jwtAccessHeader == null || !jwtAccessHeader.startsWith("Bearer ")){
            return null;
        }

        String jwtAccessToken = jwtAccessHeader.replace("Bearer ", "");

        return jwtAccessToken;
    }


    /**
     * Request Header에서 refresh token 값을 가져와서 리턴한다. (Bearer 문자열은 제외하고 토큰 값만 리턴)
     * 1. Request Header에 refresh token이 없으면 null 리턴
     * 2. refresh token이 Bearer로 시작하지 않으면 null 리턴
     */
    public String getRefreshToken(HttpServletRequest request){
        String jwtRefreshHeader = request.getHeader(REFRESH_TOKEN_HEADER_NAME);

        if (jwtRefreshHeader == null || !jwtRefreshHeader.startsWith("Bearer ")){
            return null;
        }

        String jwtRefreshToken = jwtRefreshHeader.replace("Bearer ", "");

        return jwtRefreshToken;
    }


    /**
     * jwt access Token을 생성해서 리턴한다.
     * 1. payload에 userId와 role이 저장된다.
     * 2. 만료 시간은 30분
     */
    public String createAccessToken(String userPk, UserRole role){
        Date now = new Date();

        Claims claims = Jwts.claims().setSubject(userPk); // JWT payload에 저장되는 정보단위
        claims.put("role", role.name());

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)  // payload 저장
                .setIssuedAt(now)   //토큰 발생 시간 정보
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME)) //set Expire Time
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 사용할 암호화 알고리즘과 signature에 들어갈 secret 값 세팅
                .compact();
    }


    /**
     * jwt refresh Token을 생성해서 리턴한다.
     * 1. 생성한 jwt refresh token은 redis에 저장한다.
     * 2. 만료 시간은 10일
     */
    public String createRefreshToken(String deviceTokenId) throws BaseException {
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(now)   //토큰 발생 시간 정보
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME)) //set Expire Time
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 사용할 암호화 알고리즘과 signature에 들어갈 secret 값 세팅
                .compact();

        // redis 저장
        redisService.setRefreshTokenInRedis(deviceTokenId, refreshToken, REFRESH_TOKEN_EXPIRE_TIME);

        return refreshToken;
    }

    /**
     * jwt access Token에서 userId 추출하기
     */
    public String getUserPkInAccessToken(String jwtAccessToken){
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtAccessToken).getBody().getSubject();
    }


    /**
     * 토큰의 유효성 + 만료일자 확인하기 (access token, refresh token)
     */
    public boolean validateToken(String jwtToken){

        try{
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * refresh token의 남은 유효기간 확인
     * 1. refresh token의 유효기간이 2일 이하로 남았으면 true 리턴
     * 2. refresh token의 유효기간이 3일 이상으로 남았으면 false 리턴
     */
    public boolean isRefreshReissue(String jwtRefreshToken){
        try{
            Date now = new Date();
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtRefreshToken);
            return claims.getBody().getExpiration().before(new Date(now.getTime() + REFRESH_TOKEN_REISSUE_TIME));
        }
        catch (Exception e){
            return true;
        }

    }
}
