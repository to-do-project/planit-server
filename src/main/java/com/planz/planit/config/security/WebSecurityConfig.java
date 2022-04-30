package com.planz.planit.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planz.planit.config.security.filter.JwtAuthenticationFilter;
import com.planz.planit.config.security.filter.JwtAuthorizationFilter;
import com.planz.planit.src.domain.deviceToken.DeviceTokenRepository;
import com.planz.planit.src.domain.user.UserRepository;
import com.planz.planit.src.service.DeviceTokenService;
import com.planz.planit.src.service.HttpResponseService;
import com.planz.planit.src.service.JwtTokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;


@Log4j2
@EnableWebSecurity  // SpringSecurity 사용을 위한 어노테이션, 기본적으로 CSRF 활성화, @Configuration 어노테이션(스프링 설정 클래스를 선언하는 어노테이션) 포함됨
// SpringSecurity란, Spring기반의 애플리케이션의 보안(인증, 권한, 인가 등)을 담당하는 Spring 하위 프레임워크
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final HttpResponseService httpResponseService;
    private final DeviceTokenService deviceTokenService;
    private final DeviceTokenRepository deviceTokenRepository;

    @Autowired
    public WebSecurityConfig(JwtTokenService jwtTokenService, UserRepository userRepository, ObjectMapper objectMapper, HttpResponseService httpResponseService, DeviceTokenService deviceTokenService, DeviceTokenRepository deviceTokenRepository) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.httpResponseService = httpResponseService;
        this.deviceTokenService = deviceTokenService;
        this.deviceTokenRepository = deviceTokenRepository;
    }


    /**
     * SpringSecurity 설정
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("WebSecurityConfig.configure() 호출");
        // REST API 서버는 stateless하게 개발하기 때문에 사용자 정보를 Session에 저장 안함
        // jwt 토큰을 Cookie에 저장하지 않는다면, CSRF에 어느정도는 안전.
        http
                .csrf().disable()       // csrf 보안 토큰 disable처리

                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 역시 사용하지 않음
                .and()

                .formLogin().disable()
                .httpBasic().disable()  // rest api 만을 고려하여 기본 설정은 해제
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtTokenService, objectMapper, httpResponseService, deviceTokenService, deviceTokenRepository))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtTokenService, userRepository, httpResponseService))

                .authorizeRequests()    // 요청에 대한 사용권한 체크
                .antMatchers("/api/**").access("hasRole('ROLE_USER')")
                .anyRequest().permitAll();
    }
}
