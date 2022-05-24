package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.item.dto.BuyItemReqDTO;
import com.planz.planit.src.domain.item.dto.BuyItemResDTO;
import com.planz.planit.src.domain.item.dto.GetItemInfoResDTO;
import com.planz.planit.src.domain.item.dto.GetItemStoreInfoResDTO;
import com.planz.planit.src.service.ItemService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/store")
public class ItemController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }


    /**
     * 아이템 스토어 전체 화면에서 필요한 모든 정보를 넘겨준다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @return 닉네임, 보유 포인트, 캐릭터 아이템 아이디 리스트, 행성 아이템 아이디 리스트
     */
    @GetMapping("")
    @ApiOperation(value = "아이템 스토어 전체 목록 조회 API")
    public BaseResponse<GetItemStoreInfoResDTO> getItemStoreInfo(HttpServletRequest request){
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            return new BaseResponse<>(itemService.getItemStoreInfo(Long.valueOf(userId)));
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 아이템 스토어에서 특정 아이템의 세부 정보를 넘겨준다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @PathVariable itemId
     * @return 아이템 아이디, 아이템 이름, 아이템 설명, 아이템 가격, 아이템 종류, 구매할 수 있는 최소 & 최대 수량
     */
    @GetMapping("/items/{itemId}")
    @ApiOperation(value = "아이템 스토어 세부 정보 조회 API")
    public BaseResponse<GetItemInfoResDTO> getItemInfo(HttpServletRequest request, @PathVariable Long itemId){

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            return new BaseResponse<>(itemService.getItemInfo(Long.valueOf(userId), itemId));
        }
        catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 아이템 스토어에서 특정 아이템을 구매한다.
     * @RequestHeader User-Id, Jwt-Access-Token
     * @RequestBody itemId, count, totalPrice
     * @return 아이템 아이디, 업데이트된 구매할 수 있는 최소 & 최대 수량, 업데이트된 보유 포인트 반환
     */
    @PostMapping("/item")
    @ApiOperation(value = "아이템 스토어 구매 API")
    public BaseResponse<BuyItemResDTO> buyItem(HttpServletRequest request,
                                               @Valid @RequestBody BuyItemReqDTO reqDTO,
                                               BindingResult br){
        if(br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            return new BaseResponse<>(itemService.buyItem(Long.valueOf(userId), reqDTO.getItemId(), reqDTO.getCount(), reqDTO.getTotalPrice()));
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}
