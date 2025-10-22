package com.ceg.swap.service;

import com.ceg.swap.dto.ScheduleRequest;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduleServiceTest {

    private ScheduleJobRepository repository;
    private TaskScheduler taskScheduler;
    private JobExecutorFactory factory;
    private ScheduleService service;

    @BeforeEach
    void setup() {
        repository = mock(ScheduleJobRepository.class);
        taskScheduler = mock(TaskScheduler.class);
        factory = mock(JobExecutorFactory.class);
        service = new ScheduleService(repository, taskScheduler, factory);
    }

    @Test
    void shouldScheduleJobAndSave() {
        ScheduleRequest req = new ScheduleRequest("octocat", "hello-world", "https://webhook.site/test");
        ScheduleJob job = new ScheduleJob();
        job.setId(1L);
        when(repository.save(any(ScheduleJob.class))).thenReturn(job);
        when(factory.createExecutor(anyLong())).thenReturn(() -> {
        });
        ScheduleJob saved = service.schedule(req);

        assertNotNull(saved);
        verify(repository, times(1)).save(any(ScheduleJob.class));
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Date.class));
    }
}
