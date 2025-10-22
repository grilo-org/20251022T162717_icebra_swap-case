package com.ceg.swap.repository;

import com.ceg.swap.model.ScheduleJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ScheduleJobRepository extends JpaRepository<ScheduleJob, Long> {
    List<ScheduleJob> findByStatusAndExecuteAtBefore(String status, OffsetDateTime time);
}
