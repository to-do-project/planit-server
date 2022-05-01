package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.deviceToken.dto.DeviceTokenReqDTO;
import com.planz.planit.src.domain.user.dto.*;
import com.planz.planit.src.service.JwtTokenService;
import com.planz.planit.src.service.UserService;
import com.planz.planit.utils.ValidationRegex;
import com.planz.planit.utils.ValidationUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.IOException;

import static com.planz.planit.config.BaseResponseStatus.*;

@Log4j2
@RestController
public class UserController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;


    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public UserController(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    // 닉네임 형식 및 중복 확인
    @GetMapping("/join/dupli/nickname")
    public BaseResponse<String> checkNickname(@RequestParam("nickname") String nickname){

        if (ValidationRegex.isRegexNickname(nickname) == false){
            return new BaseResponse<>(INVALID_NICKNAME_FORM);
        }

        if(userService.isEmptyNickname(nickname)){
            return new BaseResponse<>("사용가능한 닉네임입니다.");
        }
        else{
            return new BaseResponse<>(ALREADY_EXIST_NICKNAME);
        }
    }

    // 이메일 형식 및 중복 확인
    @GetMapping("/join/dupli/email")
    public BaseResponse<String> checkEmail(@RequestParam("email") String email){

        if(ValidationRegex.isRegexEmail(email) == false){
            return new BaseResponse<>(INVALID_EMAIL_FORM);
        }

        if(userService.isEmptyEmail(email)){
            return new BaseResponse<>("사용가능한 이메일입니다.");
        }
        else{
            return new BaseResponse<>(ALREADY_EXIST_EMAIL);
        }
    }


    @PostMapping("/join/auth/new-num")
    public BaseResponse<String> createAuthNum(@Valid @RequestBody CreateAuthNumReqDTO reqDTO,
                                              BindingResult br){
        // 형식적 validation
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try{
            userService.createAuthNum(reqDTO.getEmail());
            return new BaseResponse<>("해당 이메일로 인증번호를 발송했습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }

    @PostMapping("/join/auth/check-num")
    public BaseResponse<String> checkAuthNum(@Valid @RequestBody CheckAuthNumReqDTO reqDTO,
                                             BindingResult br){
        // 형식적 validation
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try{
            String result = userService.checkAuthNum(reqDTO.getEmail(), reqDTO.getAuthNum());
            return new BaseResponse<>(result);
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }

    // 회원가입
    @PostMapping("/join")
    public BaseResponse<LoginResDTO> join(HttpServletResponse response,
                                          @Valid @RequestBody JoinReqDTO reqDTO,
                                          BindingResult br) {
        log.info("UserController.join() 호출");

        // 형식적 validation
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try{
            LoginResDTO result = userService.join(reqDTO, response);
            return new BaseResponse<>(result);
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    // access token 재발급
    @PostMapping("/access-token")
    public BaseResponse<String> reissueAccessToken(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   @Valid @RequestBody DeviceTokenReqDTO reqDTO,
                                                   BindingResult br) {

        // 형식적 validation => DTO 수정 필요
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        String userId = request.getHeader(USER_ID_HEADER_NAME);
        String refreshToken = jwtTokenService.getRefreshToken(request);

        try {
            // 형식적 Validation
            ValidationUtils.checkUserIdInHeader(userId);

            if(refreshToken == null){
                return new BaseResponse<>(NOT_EXIST_REFRESH_TOKEN_IN_HEADER);
            }

            userService.reissueAccessToken(userId, refreshToken, reqDTO.getDeviceToken(), response);
            return new BaseResponse<>("JWT Access Token을 새로 발급했습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }


    @PostMapping("/log-out")
    public BaseResponse<String> logout(HttpServletRequest request,
                                       @Valid @RequestBody DeviceTokenReqDTO reqDTO,
                                       BindingResult br){
        // 형식적 validation => DTO 수정 필요
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            // 형식적 Validation
            ValidationUtils.checkUserIdInHeader(userId);

            userService.logout(userId, reqDTO);
            return new BaseResponse<>("로그아웃이 완료되었습니다. 클라이언트 단의 access token과 refresh token을 삭제해주세요.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    // 회원탈퇴
    @DeleteMapping("/api/user")
    public BaseResponse<String> withdrawal(HttpServletRequest request,
                                           @Valid @RequestBody WithdrawalReqDTO reqDTO,
                                           BindingResult br){

        // 형식적 validation
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            // Spring Security가 userId와 jwtAccessToken에 대한 validation 모두 완료!

            userService.withdrawal(userId, reqDTO.getPassword());
            return new BaseResponse<>("회원탈퇴가 완료되었습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    // 임시 비밀번호 발송
    @PostMapping("/user/temporary/pwd")
    public BaseResponse<String> createTemporaryPwd(@Valid @RequestBody CreateTemporaryPwdReqDTO reqDTO,
                                                   BindingResult br){

        // 형식적 validation
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try{
            userService.createTemporaryPwd(reqDTO.getEmail());
            return new BaseResponse<>("해당 이메일로 임시 비밀번호를 발송했습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }
}
