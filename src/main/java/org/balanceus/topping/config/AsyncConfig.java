package org.balanceus.topping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "customExecutor")
    public Executor customExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size
        executor.setCorePoolSize(5);
        
        // Maximum pool size
        executor.setMaxPoolSize(10);
        
        // Queue capacity
        executor.setQueueCapacity(50);
        
        // Thread name prefix
        executor.setThreadNamePrefix("custom-executor-");
        
        // Rejection policy - discard oldest task when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        
        // Initialize the executor
        executor.initialize();
        
        return executor;
    }
}