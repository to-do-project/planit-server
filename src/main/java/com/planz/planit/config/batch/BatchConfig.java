package com.planz.planit.config.batch;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.fcm.FirebaseCloudMessageService;
import com.planz.planit.src.domain.deviceToken.DeviceToken;
import com.planz.planit.src.service.DeviceTokenService;
import com.planz.planit.src.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final UserService userService;
    private final DeviceTokenService deviceTokenService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;


    @Autowired
    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, UserService userService, DeviceTokenService deviceTokenService, FirebaseCloudMessageService firebaseCloudMessageService) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.userService = userService;
        this.deviceTokenService = deviceTokenService;
        this.firebaseCloudMessageService = firebaseCloudMessageService;
    }

    @Bean
    public Job jobRunAway(){
        Job jobRunAway = jobBuilderFactory.get("jobRunAway")
                .start(stepCallRunAwayProcedure())
                .build();
        return jobRunAway;
    }

    @Bean
    @JobScope
    public Step stepCallRunAwayProcedure(){
        Step stepCallRunAwayProcedure = stepBuilderFactory.get("stepCallRunAwayProcedure")
                .tasklet((contribution, chunkContext) -> {
                    log.info("stepCallRunAwayProcedure : 가출 프로시저(run_away) 실행");

                    // MySQL의 가출 프로시저 (run_away) 실행
                    List<DeviceToken> deviceTokenList = deviceTokenService.callRunAwayProcedure();
                    // setting_flag가 1이고, 가출 상태인 사용자의 deviceToken 값 리스트
                    List<String> deviceTokens = deviceTokenList.stream()
                            .map(DeviceToken::getDeviceToken)
                            .collect(Collectors.toList());

                    try{
                        // push 알림 전송
                        firebaseCloudMessageService.sendMessageTo(deviceTokens, "[가출]", "별 주민이 우주선을 타고 행성을 떠났습니다. 빨리 행성을 확인해주세요!");
                        log.info("[FCM 전송 성공] setting_flag가 1이고 가출 상태인 사용자에게, 가출 FCM 전송 성공");
                    }
                    catch (BaseException e){
                        log.error("[FCM 전송 실패] " + e.getStatus());
                    }

                    return RepeatStatus.FINISHED;
                })
                .build();
        return stepCallRunAwayProcedure;
    }

}

