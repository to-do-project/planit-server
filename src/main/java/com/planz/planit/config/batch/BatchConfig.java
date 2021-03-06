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
                    log.info("stepCallRunAwayProcedure : ?????? ????????????(run_away) ??????");

                    // MySQL??? ?????? ???????????? (run_away) ??????
                    List<DeviceToken> deviceTokenList = deviceTokenService.callRunAwayProcedure();
                    // setting_flag??? 1??????, ?????? ????????? ???????????? deviceToken ??? ?????????
                    List<String> deviceTokens = deviceTokenList.stream()
                            .map(DeviceToken::getDeviceToken)
                            .collect(Collectors.toList());

                    try{
                        // push ?????? ??????
                        firebaseCloudMessageService.sendMessageTo(deviceTokens, "[??????]", "??? ????????? ???????????? ?????? ????????? ???????????????. ?????? ????????? ??????????????????!");
                        log.info("[FCM ?????? ??????] setting_flag??? 1?????? ?????? ????????? ???????????????, ?????? FCM ?????? ??????");
                    }
                    catch (BaseException e){
                        log.error("[FCM ?????? ??????] " + e.getStatus());
                    }

                    return RepeatStatus.FINISHED;
                })
                .build();
        return stepCallRunAwayProcedure;
    }

}

