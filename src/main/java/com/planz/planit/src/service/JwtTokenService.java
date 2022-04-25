package com.planz.planit.src.service;

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

    // Request Header에서 access token 값을 가져옵니다.
    public String getAccessToken(HttpServletRequest request){
        log.info("getAccessToken() 호출");

        String jwtAccessHeader = request.getHeader(ACCESS_TOKEN_HEADER_NAME);

        if (jwtAccessHeader == null || !jwtAccessHeader.startsWith("Bearer ")){
            return null;
        }

        String jwtAccessToken = jwtAccessHeader.replace("Bearer ", "");

        return jwtAccessToken;
    }

    // Request Header에서 refresh token 값을 가져옵니다.
    public String getRefreshToken(HttpServletRequest request){
        log.info("getRefreshToken() 호출");

        String jwtRefreshHeader = request.getHeader(REFRESH_TOKEN_HEADER_NAME);

        if (jwtRefreshHeader == null || !jwtRefreshHeader.startsWith("Bearer ")){
            return null;
        }

        String jwtRefreshToken = jwtRefreshHeader.replace("Bearer ", "");

        return jwtRefreshToken;
    }


    // jwt access Token 생성
    public String createAccessToken(String userPk, UserRole role){
        log.info("createAccessToken() 호출");

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

    // jwt refresh Token 생성
    public String createRefreshToken(String userPk){
        log.info("createRefreshToken() 호출");

        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(now)   //토큰 발생 시간 정보
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME)) //set Expire Time
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 사용할 암호화 알고리즘과 signature에 들어갈 secret 값 세팅
                .compact();

        // redis 저장
        redisService.setRefreshTokenInRedis(userPk, refreshToken, REFRESH_TOKEN_EXPIRE_TIME);

        return refreshToken;
    }


    // 토큰에서 회원 정보 추출
    public String getUserPkInAccessToken(String jwtAccessToken){
        log.info("JwtTokenProvider.getUserPk() 호출");
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtAccessToken).getBody().getSubject();
    }


    // 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken){

        try{
            log.info("validateAccessToken() 호출");
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        }
        catch (Exception e){
            return false;
        }
    }

    // refresh 남은 유효기간 확인
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
