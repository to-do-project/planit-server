package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.domain.closet.dto.GetClosetResDTO;
import com.planz.planit.src.service.ClosetService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/closet")
public class ClosetController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final ClosetService closetService;

    @Autowired
    public ClosetController(ClosetService closetService) {
        this.closetService = closetService;
    }

    /**
     * 보유 중인 캐릭터 아이템 목록을 반환한다.
     * => List(itemId) 반환
     * @param request
     * @return GetClosetResDTO
     */
    @GetMapping("/character-items")
    @ApiOperation(value = "보유한 캐릭터 아이템 목록 조회 API")
    public BaseResponse<GetClosetResDTO> getCloset(HttpServletRequest request){

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            return new BaseResponse<>(closetService.getCloset(Long.valueOf(userId)));
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }

    /**
     * 보유한 캐릭터 아이템을 적용한다.
     * @param request, itemId
     * @return 결과 메세지
     */
    @PatchMapping("/character-items/{itemId}")
    @ApiOperation(value = "보유한 캐릭터 아이템 적용 API")
    public BaseResponse<String> applyCharacterItem(HttpServletRequest request, @PathVariable Long itemId){

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            closetService.applyCharacterItem(Long.valueOf(userId), itemId);
            return new BaseResponse<>("성공적으로 캐릭터 아이템을 적용했습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }
}
