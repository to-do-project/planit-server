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
import io.swagger.annotations.ApiOperation;
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


    /**
     * 닉네임 형식 및 중복 여부를 확인한다.
     * @param nickname
     * @Return 결과 메세지
     */
    @GetMapping("/join/dupli/nickname")
    @ApiOperation(value = "닉네임 형식 및 중복 여부 확인")
    public BaseResponse<String> checkNickname(@RequestParam("nickname") String nickname) {

        // 닉네임 형식 검사
        if (ValidationRegex.isRegexNickname(nickname) == false) {
            return new BaseResponse<>(INVALID_NICKNAME_FORM);
        }

        try {
            // 닉네임 중복 검사
            if (userService.isEmptyNickname(nickname)) {
                return new BaseResponse<>("사용가능한 닉네임입니다.");
            } else {
                return new BaseResponse<>(ALREADY_EXIST_NICKNAME);
            }
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 이메일 형식 및 중복 여부를 확인한다.
     * @param email
     * @Return 결과 메세지
     */
    @GetMapping("/join/dupli/email")
    @ApiOperation(value = "이메일 형식 및 중복 여부 확인")
    public BaseResponse<String> checkEmail(@RequestParam("email") String email) {

        // 이메일 형식 검사
        if (ValidationRegex.isRegexEmail(email) == false) {
            return new BaseResponse<>(INVALID_EMAIL_FORM);
        }

        try {
            // 이메일 중복 검사
            if (userService.isEmptyEmail(email)) {
                return new BaseResponse<>("사용가능한 이메일입니다.");
            } else {
                return new BaseResponse<>(ALREADY_EXIST_EMAIL);
            }
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 6자리 인증 번호를 생성해서 이메일로 발송한다.
     * @Body email
     * @Return 결과 메세지
     */
    @PostMapping("/join/auth/new-num")
    @ApiOperation(value = "이메일 인증 번호 발송")
    public BaseResponse<String> createAuthNum(@Valid @RequestBody CreateAuthNumReqDTO reqDTO,
                                              BindingResult br) {
        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try {
            // 인증번호 생성 후 이메일로 발송
            userService.createAuthNum(reqDTO.getEmail());
            return new BaseResponse<>("해당 이메일로 인증번호를 발송했습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }

    }

    /**
     * Redis에 저장된 인증번호와 Request Body에 담긴 인증번호를 비교한다.
     * @Body email, authNum
     * @Return 결과 메세지
     */
    @PostMapping("/join/auth/check-num")
    @ApiOperation(value = "이메일 인증 번호 검증")
    public BaseResponse<String> checkAuthNum(@Valid @RequestBody CheckAuthNumReqDTO reqDTO,
                                             BindingResult br) {
        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try {
            // 이메일 인증번호 검증
            userService.checkAuthNum(reqDTO.getEmail(), reqDTO.getAuthNum());
            return new BaseResponse<>("인증이 완료되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }

    }

    /**
     * 회원가입 - User 테이블에 새로운 사용자 정보 추가
     * @Body email, password, nickname, planetColor, deviceToken
     * @Return LoginResDTO (사용자 정보)
     */
    @PostMapping("/join")
    @ApiOperation(value = "회원가입")
    public BaseResponse<LoginResDTO> join(HttpServletResponse response,
                                          @Valid @RequestBody JoinReqDTO reqDTO,
                                          BindingResult br) {

        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try {
            LoginResDTO result = userService.join(reqDTO, response);
            return new BaseResponse<>(result);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * access token을 재발급한다.
     * @RequestHeader User-Id, Jwt-Refresh-Token
     * @RequestBody deviceToken
     * @ResponseHeader Jwt-Access-Token, Jwt-Refresh-Token
     * @ResponseBody 결과 메세지
     */
    @PostMapping("/access-token")
    @ApiOperation(value = "access token을 재발급")
    public BaseResponse<String> reissueAccessToken(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   @Valid @RequestBody DeviceTokenReqDTO reqDTO,
                                                   BindingResult br) {

        // 형식적 validation => DTO 수정 필요
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        String userId = request.getHeader(USER_ID_HEADER_NAME);
        String refreshToken = jwtTokenService.getRefreshToken(request);

        try {

            // 형식적 Validation => JwtAuthorizationFilter 를 통과하지 않아서 필요한 부분!!
            ValidationUtils.checkUserIdInHeader(userId);

            if (refreshToken == null) {
                return new BaseResponse<>(NOT_EXIST_REFRESH_TOKEN_IN_HEADER);
            }

            userService.reissueAccessToken(userId, refreshToken, reqDTO.getDeviceToken(), response);
            return new BaseResponse<>("JWT Access Token을 새로 발급했습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 로그아웃 - 해당 기기의 디바이스 토큰 삭제, 연관된 jwt refresh token 삭제
     * @RequestHeader User-Id
     * @RequestBody deviceToken
     * @ResponseBody 결과 메세지
     */
    @PostMapping("/log-out")
    @ApiOperation(value = "로그아웃")
    public BaseResponse<String> logout(HttpServletRequest request,
                                       @Valid @RequestBody DeviceTokenReqDTO reqDTO,
                                       BindingResult br) {
        // 형식적 validation => DTO 수정 필요
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try {
            // 형식적 Validation => JwtAuthorizationFilter 를 통과하지 않아서 필요한 부분!!
            ValidationUtils.checkUserIdInHeader(userId);

            userService.logout(userId, reqDTO);
            return new BaseResponse<>("로그아웃이 완료되었습니다. 클라이언트 단의 access token과 refresh token을 삭제해주세요.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 회원탈퇴 - 해당 기기의 디바이스 토큰 삭제, 연관된 jwt refresh token 삭제
     * @RequestHeader User-Id, Jwt-Access-Token
     * @RequestBody password, deviceToken
     * @ResponseBody 결과 메세지
     */
    @DeleteMapping("/api/user")
    @ApiOperation(value = "회원탈퇴")
    public BaseResponse<String> withdrawal(HttpServletRequest request,
                                           @Valid @RequestBody WithdrawalReqDTO reqDTO,
                                           BindingResult br) {

        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        // Spring Security가 userId와 jwtAccessToken에 대한 validation 모두 완료!
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try {
            userService.withdrawal(userId, reqDTO.getPassword());
            return new BaseResponse<>("회원탈퇴가 완료되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 임시 비밀번호를 발송한다.
     * @Body email
     * @return 결과 메세지
     */
    @PostMapping("/user/temporary/pwd")
    @ApiOperation(value = "임시 비밀번호 발송")
    public BaseResponse<String> createTemporaryPwd(@Valid @RequestBody CreateTemporaryPwdReqDTO reqDTO,
                                                   BindingResult br) {

        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try {
            userService.createTemporaryPwd(reqDTO.getEmail());
            return new BaseResponse<>("해당 이메일로 임시 비밀번호를 발송했습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }

    }

    /**
     * 비밀번호를 변경한다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @RequestBody oldPassword, newPassword
     * @return 결과 메세지
     */
    @PatchMapping("/api/user/pwd")
    @ApiOperation(value = "비밀번호 변경")
    public BaseResponse<String> modifyPassword(HttpServletRequest request,
                                               @Valid @RequestBody ModifyPasswordReqDTO reqDTO,
                                               BindingResult br) {
        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        // 헤더에서 userId 가져오기
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try {
            userService.modifyPassword(userId, reqDTO.getOldPassword(), reqDTO.getNewPassword());
            return new BaseResponse<>("비밀번호 변경이 성공적으로 완료되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 닉네임을 변경한다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @RequestBody nickname
     * @return 결과 메세지
     */
    @PatchMapping("/api/user/nickname")
    @ApiOperation(value = "닉네임 변경")
    public BaseResponse<String> modifyNickname(HttpServletRequest request,
                                               @Valid @RequestBody ModifyNicknameReqDTO reqDTO,
                                               BindingResult br) {
        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        // 헤더에서 userId 가져오기
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try {
            userService.modifyNickname(userId, reqDTO.getNickname());
            return new BaseResponse<>("닉네임 변경이 성공적으로 완료되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    //유저 검색 - 은지 추가
    //닉네임 혹은 이메일을 완전하게 입력해야한다.
    @GetMapping("/api/users")
    @ApiOperation("닉네임, 이메일로 유저 검색")
    public BaseResponse<SearchUserResDTO> searchUsers(@RequestParam("keyword") String keyword) {
        try {
            SearchUserResDTO searchUserResDTO = userService.searchUsers(keyword);
            return new BaseResponse<>(searchUserResDTO);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }
}
