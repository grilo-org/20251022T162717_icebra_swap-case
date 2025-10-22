package com.ceg.swap.controller;

import com.ceg.swap.dto.ScheduleRequest;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScheduleJobRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateScheduleJob() throws Exception {
        ScheduleRequest request = new ScheduleRequest("octocat", "hello-world", "https://webhook.site/test");
        mockMvc.perform(post("/api/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.githubUser").value("octocat"))
                .andExpect(jsonPath("$.repository").value("hello-world"));
    }

    @Test
    void shouldReturnJobById() throws Exception {
        ScheduleJob job = new ScheduleJob();
        job.setGithubUser("octocat");
        job.setRepository("hello-world");
        job.setWebhookUrl("https://webhook.site/test");
        job.setStatus("PENDING");
        ScheduleJob saved = repository.save(job);

        mockMvc.perform(get("/api/jobs/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.githubUser").value("octocat"));
    }
}
