package com.planz.planit.config.batch;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final BatchConfig batchConfig;

    @Autowired
    public BatchScheduler(JobLauncher jobLauncher, BatchConfig batchConfig) {
        this.jobLauncher = jobLauncher;
        this.batchConfig = batchConfig;
    }

    //초 분 시 일 월 요일
    @Scheduled(cron = "0 0 0 * * *")
    public void runJob(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);

        try{
            jobLauncher.run(batchConfig.jobRunAway(), jobParameters);
        }
        catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | JobRestartException e){
            log.error("스프링 가출 배치 실행중 에러 발생");
            e.printStackTrace();
        }
    }
}
