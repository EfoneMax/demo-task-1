package com.example.demo;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HandlerImpl implements TaskData.Handler {

    public static final long FIFTEEN_SECONDS = 15L;
    private final TaskData.Client client;
    private final Executor delayedExecutor = CompletableFuture.delayedExecutor(FIFTEEN_SECONDS, TimeUnit.SECONDS);
    AtomicInteger retriesCount = new AtomicInteger(0);

    public HandlerImpl(TaskData.Client client) {
        this.client = client;
    }

    @Override
    public TaskData.ApplicationStatusResponse performOperation(String id) {
        Instant startTime = Instant.now();

        CompletableFuture<TaskData.Response> cfResponseOne =
                CompletableFuture.supplyAsync(() -> client.getApplicationStatus1(id), delayedExecutor);

        CompletableFuture<TaskData.Response> cfResponseTwo =
                CompletableFuture.supplyAsync(() -> client.getApplicationStatus1(id), delayedExecutor);

        return CompletableFuture.allOf(cfResponseOne, cfResponseTwo)
                .thenApply(exc -> {
                            TaskData.Response response1 = cfResponseOne.join();
                            TaskData.Response response2 = cfResponseTwo.join();

                            if (response1 instanceof TaskData.Response.Success responze) {
                                return new TaskData.ApplicationStatusResponse.Success(id, responze.applicationStatus());
                            } else if (response2 instanceof TaskData.Response.Success responze) {
                                return new TaskData.ApplicationStatusResponse.Success(id, responze.applicationStatus());
                            }

                            return new TaskData.ApplicationStatusResponse.Failure(getRequestTime(startTime),
                                    retriesCount.addAndGet(1));
                        }
                )
                .exceptionally(exc -> new TaskData.ApplicationStatusResponse.Failure(getRequestTime(startTime),
                                    retriesCount.addAndGet(1)))
                .join();
    }

    private static Duration getRequestTime(Instant startTime) {
        return Duration.between(startTime.plusSeconds(FIFTEEN_SECONDS), Instant.now());
    }
}
