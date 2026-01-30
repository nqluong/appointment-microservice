package org.project.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.project.process.DoctorAvailabilityCacheProcess;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorSlotRedisCache;
import org.project.service.RedisCacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    private final List<DoctorAvailabilityCacheProcess> cacheProcesses = new ArrayList<>();

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }


    @Bean(name = "cacheProcessExecutor")
    public Executor cacheProcessExecutor(RedisCacheService redisCacheService,
                                          DoctorSlotRedisCache doctorSlotRedisCache,
                                          DoctorAvailableSlotRepository slotRepository) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("cache-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        // Start cache processing workers
        int numWorkers = 3;
        for (int i = 0; i < numWorkers; i++) {
            DoctorAvailabilityCacheProcess process = new DoctorAvailabilityCacheProcess(
                redisCacheService, doctorSlotRedisCache, slotRepository);
            cacheProcesses.add(process);
            executor.execute(process);
            log.info("Started cache worker thread {}/{}", i + 1, numWorkers);
        }

        return executor;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down cache workers...");
        for (DoctorAvailabilityCacheProcess process : cacheProcesses) {
            process.stop();
        }
        cacheProcesses.clear();
        log.info("All cache workers stopped");
    }
}

