package codes.sharky.steamwidget.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    // Overflow queue capacity for short bursts. Tune as needed.
    private static final int OVERFLOW_QUEUE_CAPACITY = 5000;

    @Bean(name = "trackerOverflowQueue")
    public BlockingQueue<Runnable> trackerOverflowQueue() {
        return new ArrayBlockingQueue<>(OVERFLOW_QUEUE_CAPACITY);
    }

    @Bean(name = "trackerExecutor")
    public ThreadPoolTaskExecutor trackerExecutor(BlockingQueue<Runnable> trackerOverflowQueue) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("tracker-");

        // When the executor is full, push the rejected task to the overflow queue.
        RejectedExecutionHandler handler = (r, exec) -> {
            boolean added = trackerOverflowQueue.offer(r);
            if (!added) {
                log.error("Tracker executor and overflow queue are full - dropping task");
            } else {
                log.warn("Tracker executor full - queued task in overflow queue (size={})", trackerOverflowQueue.size());
            }
        };

        executor.setRejectedExecutionHandler(handler);
        executor.initialize();
        return executor;
    }
}
