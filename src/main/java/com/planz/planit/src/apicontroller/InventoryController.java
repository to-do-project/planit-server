package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.inventory.dto.GetInventoryResDTO;
import com.planz.planit.src.domain.inventory.dto.PlacePlanetItemsReqDTO;
import com.planz.planit.src.service.InventoryService;
import com.planz.planit.utils.ValidationRegex;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.List;

import static com.planz.planit.config.BaseResponseStatus.INVALID_INVENTORY_CATEGORY;

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
     * @RequestHeader User-Id, Jwt-Access-Token
     * @PathVariable category
     * @return totalInventoryItemCount, List(itemId, totalCount, placedCount, remainingCount)
     */
    @GetMapping("/planet-items/{category}")
    @ApiOperation(value = "보유한 행성 아이템 목록 조회 API")
    public BaseResponse<GetInventoryResDTO> getInventoryByCategory(HttpServletRequest request, @PathVariable String category) {

        // 형식적 validation
        if (!ValidationRegex.isRegexInventoryCategory(category)) {
            return new BaseResponse<>(INVALID_INVENTORY_CATEGORY);
        }

        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try {
            return new BaseResponse<>(inventoryService.getInventoryByCategory(Long.valueOf(userId), category));
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    /**
     * 보유한 행성 아이템 (기본 건축물, 식물, 돌, 길, 기타)을 배치한다. => 행성에 적용
     * @RequestHeader User-Id, Jwt-Access-Token
     * @RequestBody PlacePlanetItemsReqDTO
     *              => itemPositionList(itemId, positionList(posX, posY)) 리스트
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
