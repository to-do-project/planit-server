package com.planz.planit.config.security.filter;

import com.planz.planit.src.service.HttpResponseService;
import com.planz.planit.config.security.auth.PrincipalDetails;
import com.planz.planit.src.domain.user.User;
import com.planz.planit.src.domain.user.UserRepository;
import com.planz.planit.src.service.JwtTokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


import static com.planz.planit.config.BaseResponseStatus.*;

@Log4j2
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String USER_ID_HEADER_NAME = "User-Id";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final HttpResponseService httpResponseService;

    @Autowired
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, UserRepository userRepository, HttpResponseService httpResponseService) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.httpResponseService = httpResponseService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("?????? URI " + request.getRequestURI());

        // ???????????? ?????? or ????????? ?????? ????????? ?????? or ???????????? ????????? ??????
        if (request.getRequestURI().startsWith("/join") || request.getRequestURI().startsWith("/access-token") || request.getRequestURI().startsWith("/log-out") || request.getRequestURI().startsWith("/user/temporary/pwd")) {
            chain.doFilter(request, response);
            return;
        }


        // ????????? ????????? ????????? ??????
        log.info("???????????? ????????? ????????? ????????? ????????? ??? => " + request.getRequestURI());

        // ???????????? userId ????????????
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        // ???????????? jwtAccessToken ????????????
        String jwtAccessToken = jwtTokenService.getAccessToken(request);


        if (checkUserIdInHeader(response, userId) && checkJwtAccessTokenInHeader(response, jwtAccessToken)) {
            User userEntity = findUser(response, userId, jwtAccessToken);

            if (userEntity != null) {
                PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
                log.info("principalDetails's username : " + principalDetails.getUsername());

                // JWT ?????? ????????? ?????????, ????????? ???????????? Authentication ????????? ???????????????.
                Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

                // ????????? ??????????????? ????????? ???????????? Authentication ????????? ??????.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                log.info("???????????? ???????????? Principal- " + authentication.getPrincipal());
                log.info("???????????? ???????????? Authorities- " + authentication.getAuthorities());
                chain.doFilter(request, response);
            }
        }


    }

    // header?????? userId??? ??????, ????????? userId?????? ??????
    private boolean checkUserIdInHeader(HttpServletResponse response, String userId) throws IOException {

        if (userId == null) {
            log.error("userId??? ????????? ??????????????????");
            httpResponseService.errorRespond(response, NOT_EXIST_USER_ID_IN_HEADER);
            return false;
        }

        try {
            Long.valueOf(userId);
        } catch (NumberFormatException e) {
            log.error("???????????? ?????? userId?????????. ??????????????? ??????????????????.");
            httpResponseService.errorRespond(response, INVALID_USER_ID);
            return false;
        }

        return true;

    }


    // header?????? jwtAccessToken??? ??????, ????????? jwtAccessToken?????? ??????
    private boolean checkJwtAccessTokenInHeader(HttpServletResponse response, String jwtAccessToken) throws IOException {
        if (jwtAccessToken == null) {
            log.error("access token??? ????????? ??????????????????.");
            httpResponseService.errorRespond(response, NOT_EXIST_ACCESS_TOKEN_IN_HEADER);
            return false;
        }

        if (jwtTokenService.validateToken(jwtAccessToken) == false) {
            log.error("????????? access token?????????.");
            httpResponseService.errorRespond(response, INVALID_ACCESS_TOKEN);
            return false;
        }

        log.info("jwtAccessToken : " + jwtAccessToken);
        return true;
    }


    private User findUser(HttpServletResponse response, String userId, String jwtAccessToken) throws IOException, NumberFormatException {

        String userPk = jwtTokenService.getUserPkInAccessToken(jwtAccessToken);

        if (userPk == null) {
            log.error("access token??? subject??? ???????????? ????????????.");
            httpResponseService.errorRespond(response, NOT_EXIST_ACCESS_TOKEN_SUBJECT);
            return null;
        }

        if (!userPk.equals(userId)) {
            log.error("accessToken??? userId??? header??? userId??? ???????????? ????????????.");
            httpResponseService.errorRespond(response, NOT_EQUAL_USER_ID);
            return null;
        }

        User userEntity = userRepository.findByUserId(Long.valueOf(userPk)).orElse(null);

        if (userEntity == null) {
            log.error("???????????? ?????? ??????????????????.");
            httpResponseService.errorRespond(response, NOT_EXIST_USER);
            return null;
        }

        return userEntity;
    }

}
