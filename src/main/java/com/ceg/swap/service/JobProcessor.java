package com.ceg.swap.service;

import com.ceg.swap.dto.ContributorDTO;
import com.ceg.swap.dto.IssueDTO;
import com.ceg.swap.dto.WebhookPayload;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class JobProcessor {
    private final ScheduleJobRepository repository;
    private final GitHubClient gitHubClient;
    private final RestTemplate rest = new RestTemplate();

    public JobProcessor(ScheduleJobRepository repository, GitHubClient gitHubClient) {
        this.repository = repository;
        this.gitHubClient = gitHubClient;
    }


    @Scheduled(fixedRate = 60000)
    public void processPending() {
        var now = OffsetDateTime.now();
        var list = repository.findByStatusAndExecuteAtBefore("PENDING", now.plusSeconds(1));
        for (ScheduleJob job : list) {
            try {
                job.setStatus("PROCESSING");
                repository.save(job);
                List<IssueDTO> issues = gitHubClient.fetchIssues(job.getGithubUser(), job.getRepository());
                List<ContributorDTO> contributors = gitHubClient.fetchContributors(job.getGithubUser(), job.getRepository());

                WebhookPayload payload = WebhookPayload.builder()
                        .user(job.getGithubUser())
                        .repository(job.getRepository())
                        .issues(issues)
                        .contributors(contributors)
                        .generatedAt(OffsetDateTime.now().toString())
                        .build();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<WebhookPayload> entity = new HttpEntity<>(payload, headers);
                rest.exchange(job.getWebhookUrl(), HttpMethod.POST, entity, String.class);

                job.setStatus("DONE");
                repository.save(job);
            } catch (Exception ex) {
                job.setStatus("FAILED");
                job.setFailureReason(ex.getMessage());
                repository.save(job);
            }
        }
    }
}
