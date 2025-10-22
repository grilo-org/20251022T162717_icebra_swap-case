package com.ceg.swap.controller;

import com.ceg.swap.dto.ScheduleRequest;
import com.ceg.swap.model.ScheduleJob;
import com.ceg.swap.repository.ScheduleJobRepository;
import com.ceg.swap.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ScheduleController {
    private final ScheduleService service;
    private final ScheduleJobRepository repository;

    public ScheduleController(ScheduleService service, ScheduleJobRepository repository) {
        this.service = service;
        this.repository = repository;
    }


    @PostMapping("/schedule")
    public ResponseEntity<ScheduleJob> schedule(@Validated @RequestBody ScheduleRequest request) {
        ScheduleJob job = service.schedule(request);
        return ResponseEntity.status(201).body(job);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ScheduleJob> getJob(@PathVariable Long id) {
        Optional<ScheduleJob> opt = repository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
