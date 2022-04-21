package com.planz.planit.src.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.user.LoginResDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.planz.planit.config.BaseResponseStatus.INVALID_ACCESS_TOKEN;

@Service
public class HttpResponseService {

    private final ObjectMapper objectMapper;

    @Autowired
    public HttpResponseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 에러가 발생한 경우
    public void errorRespond(HttpServletResponse response, BaseResponseStatus status) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        objectMapper.writeValue(out, new BaseResponse<>(status));
    }

    // 정상적인 경우
    public void successRespond(HttpServletResponse response, LoginResDTO loginResDTO) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        objectMapper.writeValue(out, new BaseResponse<LoginResDTO>(loginResDTO));
    }
}
