package com.planz.planit.src.domain.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GetNoticesResDTO {

    private int totalNoticeCnt;
    private List<NoticeDTO> noticeList;


    @Builder
    @Getter
    public static class NoticeDTO{
        private Long noticeId;
        private String title;
        private String content;

        // 자바 클래스 -> json 으로 serialize할 때 문제가 생기므로 추가
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
        private LocalDateTime createAt;
    }
}

