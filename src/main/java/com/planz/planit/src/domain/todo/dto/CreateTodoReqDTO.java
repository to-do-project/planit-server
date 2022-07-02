package com.planz.planit.src.domain.todo.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class CreateTodoReqDTO {
    @NotNull(message="NOT_EXIST_GOAL_ID")
    @Positive(message = "INVALID_GOAL_ID")
    private Long goalId;

    @NotBlank(message = "NOT_EXIST_TODO_TITLE")
    @Size(max=50,message = "INVALID_TODO_TITLE")
    private String title;
}
