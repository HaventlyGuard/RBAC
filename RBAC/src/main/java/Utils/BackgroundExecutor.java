package Utils;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class BackgroundExecutor {

    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;

    public BackgroundExecutor() {
        this.executorService = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newScheduledThreadPool(2);
    }

    public <T> CompletableFuture<T> submitTask(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executorService);
    }

    public CompletableFuture<Void> submitRunnable(Runnable task) {
        return CompletableFuture.runAsync(task, executorService);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay,
                                                  long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public void shutdown() {
        executorService.shutdown();
        scheduledExecutor.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isTerminated() {
        return executorService.isTerminated() && scheduledExecutor.isTerminated();
    }
}