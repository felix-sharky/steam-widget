package codes.sharky.steamwidget.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

/**
 * Handles overflow tasks rejected by the tracker executor by queueing and draining them later.
 * Periodically submits queued tasks back to the executor in bounded batches.
 */
@Service
@Slf4j
public class TrackerOverflowService {

    private final BlockingQueue<Runnable> overflowQueue;
    private final ThreadPoolTaskExecutor trackerExecutor;

    /**
     * Creates the overflow handler with the shared overflow queue and tracker executor.
     *
     * @param overflowQueue    queue holding tasks that exceeded executor capacity
     * @param trackerExecutor  executor used to re-submit overflow tasks
     * @param drainBatchSize   maximum tasks to drain and resubmit per run
     */
    public TrackerOverflowService(BlockingQueue<Runnable> overflowQueue, ThreadPoolTaskExecutor trackerExecutor,
                                  @Value("${tracker.overflow.drainBatchSize:100}") int drainBatchSize) {
        this.overflowQueue = overflowQueue;
        this.trackerExecutor = trackerExecutor;
        this.drainBatchSize = drainBatchSize;
    }

    private final int drainBatchSize;

    /**
     * Periodically drains the overflow queue and resubmits tasks to the tracker executor in batches.
     * Respects the configured batch size to avoid overwhelming the executor after backpressure.
     */
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
