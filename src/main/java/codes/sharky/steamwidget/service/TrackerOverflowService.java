package codes.sharky.steamwidget.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

@Service
@Slf4j
public class TrackerOverflowService {

    private final BlockingQueue<Runnable> overflowQueue;
    private final ThreadPoolTaskExecutor trackerExecutor;

    public TrackerOverflowService(BlockingQueue<Runnable> overflowQueue, ThreadPoolTaskExecutor trackerExecutor,
                                  @Value("${tracker.overflow.drainBatchSize:100}") int drainBatchSize) {
        this.overflowQueue = overflowQueue;
        this.trackerExecutor = trackerExecutor;
        this.drainBatchSize = drainBatchSize;
    }

    private final int drainBatchSize;

    @Scheduled(fixedDelayString = "${tracker.overflow.drainIntervalMs:5000}")
    public void drainOverflowQueue() {
        if (overflowQueue.isEmpty()) {
            return;
        }

        int drained = 0;
        for (int i = 0; i < drainBatchSize; i++) {
            Runnable task = overflowQueue.poll();
            if (task == null) break;
            try {
                trackerExecutor.getThreadPoolExecutor().execute(task);
                drained++;
            } catch (Exception ex) {
                // If executor rejects again, put back and stop draining to avoid tight loop
                boolean requeued = overflowQueue.offer(task);
                log.warn("Failed to re-submit overflow task to executor; requeued={} queueSize={}", requeued, overflowQueue.size());
                break;
            }
        }

        if (drained > 0) {
            log.info("Drained {} tasks from overflow queue; remaining={}", drained, overflowQueue.size());
        }
    }
}

