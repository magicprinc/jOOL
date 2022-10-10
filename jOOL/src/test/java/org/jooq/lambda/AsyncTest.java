package org.jooq.lambda;

import org.junit.Test;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;

public class AsyncTest {
    
    @Test
    public void testNoCustomExecutor() {
        CompletionStage<Void> completionStage = Async.runAsync(() -> {});
        assertNull(completionStage.toCompletableFuture().join());
        
        completionStage = Async.supplyAsync(() -> null);
        assertNull(completionStage.toCompletableFuture().join());
    }
}