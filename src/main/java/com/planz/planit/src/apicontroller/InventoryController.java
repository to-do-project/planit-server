package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.inventory.dto.GetInventoryResDTO;
import com.planz.planit.src.domain.inventory.dto.PlacePlanetItemsReqDTO;
import com.planz.planit.src.service.InventoryService;
import com.planz.planit.utils.ValidationRegex;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.INVALID_INVENTORY_CATEGORY;

@Log4j2
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * 카테고리 별로 인벤토리에서 보유 중인 행성 아이템 목록을 반환한다.
     * => List(inventoryId, itemId, count) 반환
     * @param request, category
     * @return List<GetInventoryResDTO>
     */
    @GetMapping("/planet-items/{category}")
    @ApiOperation(value = "보유한 행성 아이템 목록 조회 API")
    public BaseResponse<List<GetInventoryResDTO>> getInventoryByCategory(HttpServletRequest request, @PathVariable String category) {

        // 형식적 validation
        if (!ValidationRegex.isRegexInventoryCategory(category)) {
            return new BaseResponse<>(INVALID_INVENTORY_CATEGORY);
        }

        // Spring Security가 userId와 jwtAccessToken에 대한 validation 모두 완료!
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try {
            List<GetInventoryResDTO> result = inventoryService.getInventoryByCategory(Long.valueOf(userId), category);
            return new BaseResponse<>(result);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

/*    @PostMapping("/planet-item")
    @ApiOperation(value = "행성 아이템 구매하기")
    public BaseResponse<String> buyPlanetItem(HttpServletRequest request,
                                              @Valid @RequestBody BuyPlanetItemReqDTO reqDTO,
                                              BindingResult br) {
        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        // Spring Security가 userId와 jwtAccessToken에 대한 validation 모두 완료!
        String userId = request.getHeader(USER_ID_HEADER_NAME);


        try {
            inventoryService.buyPlanetItem(userId, reqDTO.getItemId(), reqDTO.getCount());
            return new BaseResponse<>("성공적으로 행성 아이템을 구매했습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }

    }*/

    /**
     * 보유한 행성 아이템 (기본 건축물, 식물, 돌, 길, 기타)을 배치한다. => 행성에 적용
     * @param request, PlacePlanetItemsReqDTO
     * @return 결과 메세지
     */
    @PatchMapping("/planet-items/placement")
    @ApiOperation(value = "보유한 행성 아이템 배치 API")
    public BaseResponse<String> placePlanetItems(HttpServletRequest request,
                                                 @Valid @RequestBody PlacePlanetItemsReqDTO reqDTO,
                                                 BindingResult br) {
        // 형식적 validation
        if (br.hasErrors()) {
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            return new BaseResponse<>(BaseResponseStatus.of(errorName));
        }

        // Spring Security가 userId와 jwtAccessToken에 대한 validation 모두 완료!
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            inventoryService.placePlanetItems(Long.valueOf(userId), reqDTO.getItemPositionList());
            return new BaseResponse<>("행성 편집을 성공적으로 완료했습니다.");
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}
