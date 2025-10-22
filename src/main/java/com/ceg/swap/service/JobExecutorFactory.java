package com.ceg.swap.service;

@FunctionalInterface
public interface JobExecutorFactory {
    Runnable createExecutor(Long jobId);
}
