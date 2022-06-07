package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.notice.dto.CreateNoticeReqDTO;
import com.planz.planit.src.domain.notice.dto.GetNoticesResDTO;
import com.planz.planit.src.service.NoticeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class NoticeController {

    private final NoticeService noticeService;

    @Autowired
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 새로운 공지사항을 생성한다.
     * Users 테이블의 role 필드값이 "ROLE_ADMIN"인 사용자만 해당 API를 이용할 수 있다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @RequestBody title, content
     * @return 결과 메세지
     */
    @PostMapping("/admin/api/notices/new-notice")
    @ApiOperation(value = "공지사항 생성 API (ROLE_ADMIN 권한을 가진 사용자만 이용 가능)")
    public BaseResponse<String> createNotice(@Valid @RequestBody CreateNoticeReqDTO reqDTO,
                                             BindingResult br){

        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        try {
            noticeService.createNotice(reqDTO.getTitle(), reqDTO.getContent());
            return new BaseResponse<>("성공적으로 새로운 공지사항을 생성했습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 모든 공지사항을 조회한다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @return totalNoticeCnt, List(noticeId, title, content, createAt)
     */
    @GetMapping("/api/notices")
    @ApiOperation(value = "공지사항 조회 API")
    public BaseResponse<GetNoticesResDTO> getNotices(){

        try{
            return new BaseResponse<>(noticeService.getNotices());
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}
