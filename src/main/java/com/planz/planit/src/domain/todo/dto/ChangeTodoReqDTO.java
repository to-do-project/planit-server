package com.planz.planit.src.domain.todo.dto;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class ChangeTodoReqDTO {
    @NotBlank(message = "NOT_EXIST_TODO_TITLE")
    @Size(max=50,message = "INVALID_TODO_TITLE")
    private String title;
}
