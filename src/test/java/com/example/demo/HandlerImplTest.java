package com.example.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandlerImplTest {

    public class ClientSuccessImpl implements TaskData.Client {
        @Override
        public TaskData.Response getApplicationStatus1(String id) {
            return new TaskData.Response.Success("SUCCESS", id);
        }

        @Override
        public TaskData.Response getApplicationStatus2(String id) {
            return new TaskData.Response.Failure(new RuntimeException());
        }
    }

    public class ClientFailureImpl implements TaskData.Client {
        @Override
        public TaskData.Response getApplicationStatus1(String id) {
            return new TaskData.Response.Failure(new RuntimeException());
        }

        @Override
        public TaskData.Response getApplicationStatus2(String id) {
            return new TaskData.Response.Failure(new RuntimeException());
        }
    }

    TaskData.Handler handlerSuccess = new HandlerImpl(new ClientSuccessImpl());
    TaskData.Handler handlerFailure = new HandlerImpl(new ClientFailureImpl());

    @Test
    void performSuccessfulOperation() {
        TaskData.ApplicationStatusResponse response = handlerSuccess.performOperation("some-id");
        assertNotNull(response);
        assertTrue(response instanceof TaskData.ApplicationStatusResponse.Success);
    }

    @Test
    void performFailedOperation() {
        TaskData.ApplicationStatusResponse response = handlerFailure.performOperation("some-id");
        assertNotNull(response);
        assertTrue(response instanceof TaskData.ApplicationStatusResponse.Failure);
        assertNotNull(((TaskData.ApplicationStatusResponse.Failure) response).lastRequestTime());
    }
}