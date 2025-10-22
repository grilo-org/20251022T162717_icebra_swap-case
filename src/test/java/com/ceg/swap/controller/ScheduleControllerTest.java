package com.ceg.swap.controller;

import com.ceg.swap.dto.ScheduleRequest;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import com.ceg.swap.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
public class ScheduleControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ScheduleService service;

    @MockBean
    private ScheduleJobRepository repository;

    @Test
    public void testSchedule() throws Exception {
        String body = "{\"githubUser\":\"octocat\",\"repository\":\"Hello-World\",\"webhookUrl\":\"https://webhook.site/xxxx\"}";
        when(service.schedule(any(ScheduleRequest.class))).thenAnswer(i -> {
            var req = i.getArgument(0, ScheduleRequest.class);
            var job = new ScheduleJob();
            job.setId(1L);
            job.setGithubUser(req.getGithubUser());
            job.setRepository(req.getRepository());
            job.setWebhookUrl(req.getWebhookUrl());
            job.setStatus("PENDING");
            return job;
        });

        mvc.perform(post("/api/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
