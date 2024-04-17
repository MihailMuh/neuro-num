package ru.lvmlabs.neuronum.baseconfigs.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class AwaitableExecutorService implements AutoCloseable {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final ConcurrentLinkedQueue<Future<?>> submittedTasks = new ConcurrentLinkedQueue<>();

    public void execute(Runnable runnable) {
        submittedTasks.add(executorService.submit(() -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                log.error("Can't process task!");
                exception.printStackTrace();
            }
        }));
    }

    public void await() {
        while (submittedTasks.stream().anyMatch(task -> !task.isDone())) {
            log.debug("Awaiting for {} task to complete...", submittedTasks.stream().filter(task -> !task.isCancelled() && !task.isDone()).count());

            ThreadUtils.sleep(5_000);
        }

        log.debug("All tasks completed!");
        submittedTasks.clear();
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }
}
