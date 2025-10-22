package com.ceg.swap.service;

import com.ceg.swap.dto.ContributorDTO;
import com.ceg.swap.dto.IssueDTO;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class JobExecutorTest {

    private ScheduleJobRepository repository;
    private GitHubClient gitHubClient;
    private WebhookSender webhookSender;
    private JobExecutor jobExecutor;

    @BeforeEach
    void setup() {
        repository = mock(ScheduleJobRepository.class);
        gitHubClient = mock(GitHubClient.class);
        webhookSender = mock(WebhookSender.class);
        jobExecutor = new JobExecutor(repository, gitHubClient, webhookSender);
    }

    @Test
    void shouldExecuteJobSuccessfully() throws Exception {
        ScheduleJob job = new ScheduleJob();
        job.setId(1L);
        job.setGithubUser("octocat");
        job.setRepository("hello-world");
        job.setWebhookUrl("https://webhook.site/test");
        job.setRequestedAt(OffsetDateTime.now());
        job.setExecuteAt(OffsetDateTime.now().plusDays(1));
        job.setStatus("PENDING");

        when(repository.findById(1L)).thenReturn(Optional.of(job));
        when(gitHubClient.fetchIssues(any(), any())).thenReturn(List.of(new IssueDTO("issue", "user", List.of("bug"))));
        when(gitHubClient.fetchContributors(any(), any())).thenReturn(List.of(new ContributorDTO("user", "user", 5)));

        jobExecutor.execute(1L);

        ArgumentCaptor<ScheduleJob> captor = ArgumentCaptor.forClass(ScheduleJob.class);
        verify(repository, atLeast(2)).save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(j -> "DONE".equals(j.getStatus())));
        verify(webhookSender, times(1)).send(eq("https://webhook.site/test"), any());
    }

    @Test
    void shouldHandleFailureGracefully() throws Exception {
        ScheduleJob job = new ScheduleJob();
        job.setId(2L);
        job.setGithubUser("octocat");
        job.setRepository("hello-world");
        job.setWebhookUrl("https://webhook.site/test");
        job.setStatus("PENDING");

        when(repository.findById(2L)).thenReturn(Optional.of(job));
        when(gitHubClient.fetchIssues(any(), any())).thenThrow(new RuntimeException("GitHub API error"));

        jobExecutor.execute(2L);

        verify(repository, atLeast(2)).save(any(ScheduleJob.class));
        assertEquals("FAILED", job.getStatus());
        assertTrue(job.getFailureReason().contains("GitHub API error"));
    }
}
