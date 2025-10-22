package com.ceg.swap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookPayload {
    private String user;
    private String repository;
    private List<IssueDTO> issues;
    private List<ContributorDTO> contributors;
    private String generatedAt;
}
