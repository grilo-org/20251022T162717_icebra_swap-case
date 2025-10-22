package com.ceg.swap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    @NotBlank
    private String githubUser;
    @NotBlank
    private String repository;
    @NotBlank
    private String webhookUrl;
}
