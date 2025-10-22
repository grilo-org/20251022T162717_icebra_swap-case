package com.ceg.swap.service;

import com.ceg.swap.dto.ContributorDTO;
import com.ceg.swap.dto.IssueDTO;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobExecutor {

    private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);

    private final ScheduleJobRepository repository;
    private final GitHubClient gitHubClient;
    private final WebhookSender webhookSender;

    public JobExecutor(ScheduleJobRepository repository,
                       GitHubClient gitHubClient,
                       WebhookSender webhookSender) {
        this.repository = repository;
        this.gitHubClient = gitHubClient;
        this.webhookSender = webhookSender;
    }

    public void execute(Long jobId) {
        Optional<ScheduleJob> optionalJob = repository.findById(jobId);
        if (optionalJob.isEmpty()) {
            log.warn("Job {} não encontrado.", jobId);
            return;
        }

        ScheduleJob job = optionalJob.get();
        try {
            log.info("Executando job {} para repositório {}/{}", jobId, job.getGithubUser(), job.getRepository());
            job.setStatus("RUNNING");
            repository.save(job);

            List<IssueDTO> issues = gitHubClient.fetchIssues(job.getGithubUser(), job.getRepository());
            List<ContributorDTO> contributors = gitHubClient.fetchContributors(job.getGithubUser(), job.getRepository());

            var payload = new JobResult(issues, contributors);

            webhookSender.send(job.getWebhookUrl(), payload);

            job.setStatus("DONE");
            repository.save(job);

            log.info("Job {} concluido com sucesso.", jobId);
        } catch (Exception e) {
            log.error("Falha ao executar job {}: {}", jobId, e.getMessage());
            job.setStatus("FAILED");
            job.setFailureReason(e.getMessage());
            repository.save(job);
        }
    }

    private static class JobResult {
        public List<IssueDTO> issues;
        public List<ContributorDTO> contributors;

        public JobResult(List<IssueDTO> issues, List<ContributorDTO> contributors) {
            this.issues = issues;
            this.contributors = contributors;
        }
    }
}
