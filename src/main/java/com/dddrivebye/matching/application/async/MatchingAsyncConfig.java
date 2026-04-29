package com.dddrivebye.matching.application.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

/**
 * Async + scheduling wiring for the matching module.
 *
 * Plays the BullMQ role: incoming jobs (e.g. RideRequestedEvent) are processed
 * off the calling thread by {@code matchingTaskExecutor}; delayed jobs (e.g.
 * the 30-second proposal expiry) are dispatched by {@code matchingTaskScheduler}.
 *
 * Both pools live in-process. If/when this monolith is split, swap the trigger
 * source for Redis Streams (or any external queue) without touching domain logic.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class MatchingAsyncConfig {

    @Bean(name = "matchingTaskExecutor")
    public Executor matchingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("matching-worker-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "matchingTaskScheduler")
    public TaskScheduler matchingTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("matching-scheduler-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }
}
