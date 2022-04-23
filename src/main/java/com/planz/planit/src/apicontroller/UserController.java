package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.mail.MailDTO;
import com.planz.planit.src.domain.user.dto.CheckAuthNumReqDTO;
import com.planz.planit.src.domain.user.dto.CreateAuthNumReqDTO;
import com.planz.planit.src.domain.user.dto.JoinReqDTO;
import com.planz.planit.src.domain.user.dto.LoginResDTO;
import com.planz.planit.src.service.MailService;
import com.planz.planit.src.service.UserService;
import com.planz.planit.utils.ValidationRegex;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.planz.planit.config.BaseResponseStatus.*;

@Log4j2
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 닉네임 형식 및 중복 확인
    @GetMapping("join/dupli/nickname")
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
    @GetMapping("join/dupli/email")
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

}
