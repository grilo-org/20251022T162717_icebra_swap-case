package com.ceg.swap.service;

import com.ceg.swap.dto.ScheduleRequest;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;

@Service
public class ScheduleService {

    private final ScheduleJobRepository repository;
    private final TaskScheduler taskScheduler;
    private final JobExecutorFactory jobExecutorFactory;

    public ScheduleService(ScheduleJobRepository repository,
                           TaskScheduler taskScheduler,
                           JobExecutorFactory jobExecutorFactory) {
        this.repository = repository;
        this.taskScheduler = taskScheduler;
        this.jobExecutorFactory = jobExecutorFactory;
    }

    public ScheduleJob schedule(ScheduleRequest request) {
        ScheduleJob job = new ScheduleJob();
        job.setGithubUser(request.getGithubUser());
        job.setRepository(request.getRepository());
        job.setWebhookUrl(request.getWebhookUrl());
        job.setStatus("PENDING");
        job.setRequestedAt(OffsetDateTime.now());
        job.setExecuteAt(OffsetDateTime.now().plusDays(1));

        ScheduleJob saved = repository.save(job);

        Runnable executor = jobExecutorFactory.createExecutor(saved.getId());
        taskScheduler.schedule(executor, Date.from(saved.getExecuteAt().toInstant()));

        return saved;
    }
}
