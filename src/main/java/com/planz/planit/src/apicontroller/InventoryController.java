package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.domain.inventory.dto.GetInventoryResDTO;
import com.planz.planit.src.service.InventoryService;
import com.planz.planit.utils.ValidationRegex;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    @GetMapping("/planet-items/{category}")
    @ApiOperation(value = "카테고리 별로 인벤토리에서 보유 중인 행성 아이템 목록 조회하기")
    public BaseResponse<List<GetInventoryResDTO>> getInventoryByCategory(HttpServletRequest request, @PathVariable String category){

        // 형식적 validation
        if ( (category == null) || (!ValidationRegex.isRegexInventoryCategory(category)) ){
            return new BaseResponse<>(INVALID_INVENTORY_CATEGORY);
        }

        // Spring Security가 userId와 jwtAccessToken에 대한 validation 모두 완료!
        String userId = request.getHeader(USER_ID_HEADER_NAME);

        try{
            List<GetInventoryResDTO> result = inventoryService.getInventoryByCategory(userId, category);
            return new BaseResponse<>(result);
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}
