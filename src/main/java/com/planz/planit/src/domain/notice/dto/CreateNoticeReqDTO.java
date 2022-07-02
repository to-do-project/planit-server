package com.planz.planit.src.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateNoticeReqDTO {

    @NotBlank(message = "NOT_EXIST_NOTICE_TITLE")
    private String title;

    @NotBlank(message = "NOT_EXIST_NOTICE_CONTENT")
    private String content;

}
