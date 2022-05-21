package com.planz.planit.src.apicontroller;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponse;
import com.planz.planit.src.domain.planet.dto.GetPlanetMainInfoResDTO;
import com.planz.planit.src.service.PlanetService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RequestMapping("/api/planet")
@RestController
public class PlanetController {

    @Value("${jwt.user-id-header-name}")
    private String USER_ID_HEADER_NAME;

    private final PlanetService planetService;

    @Autowired
    public PlanetController(PlanetService planetService) {
        this.planetService = planetService;
    }

    @GetMapping("/main/{targetUserId}")
    @ApiOperation(value = "행성 메인 화면 조회 API")
    public BaseResponse<GetPlanetMainInfoResDTO> getPlanetMainInfo(HttpServletRequest request, @PathVariable Long targetUserId){

        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME));

        try{
            // 친구의 행성 정보를 조회하는 경우 => 나와 친구 관계인지 validation
            if (userId != targetUserId) {

            }

            return new BaseResponse<>(planetService.getPlanetMainInfo(targetUserId));
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}
