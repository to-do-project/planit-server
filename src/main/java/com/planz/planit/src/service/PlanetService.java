package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.src.domain.planet.Planet;
import com.planz.planit.src.domain.planet.PlanetRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;

@Log4j2
@Service
public class PlanetService {
    private final PlanetRepository planetRepository;

    @Autowired
    public PlanetService(PlanetRepository planetRepository) {
        this.planetRepository = planetRepository;
    }

    // Planet 저장
    public void savePlanet(Planet planetEntity) throws BaseException {
        try {
            planetRepository.save(planetEntity);
        }
        catch (Exception e){
            log.error("savePlanet() : planetRepository.save(planetEntity) 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // Planet 삭제
    public void deletePlanet(Long longUserId) throws BaseException {
        try {
            planetRepository.deleteByUserIdInQuery(longUserId);
        }
        catch (Exception e){
            log.error("deletePlanet() : planetRepository.deleteByUserIdInQuery(longUserId) 실행 중 데이터베이스 에러 발생");
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
