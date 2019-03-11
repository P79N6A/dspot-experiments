/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.streaming.api.operators.async.queue;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.apache.flink.streaming.api.operators.async.OperatorActions;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.TestLogger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


/**
 * Tests for the basic functionality of {@link StreamElementQueue}. The basic operations consist
 * of putting and polling elements from the queue.
 */
@RunWith(Parameterized.class)
public class StreamElementQueueTest extends TestLogger {
    private static final long timeout = 10000L;

    private static ExecutorService executor;

    enum StreamElementQueueType {

        OrderedStreamElementQueueType,
        UnorderedStreamElementQueueType;}

    private final StreamElementQueueTest.StreamElementQueueType streamElementQueueType;

    public StreamElementQueueTest(StreamElementQueueTest.StreamElementQueueType streamElementQueueType) {
        this.streamElementQueueType = Preconditions.checkNotNull(streamElementQueueType);
    }

    @Test
    public void testPut() throws InterruptedException {
        OperatorActions operatorActions = Mockito.mock(OperatorActions.class);
        StreamElementQueue queue = createStreamElementQueue(2, operatorActions);
        final Watermark watermark = new Watermark(0L);
        final StreamRecord<Integer> streamRecord = new StreamRecord(42, 1L);
        final Watermark nextWatermark = new Watermark(2L);
        final WatermarkQueueEntry watermarkQueueEntry = new WatermarkQueueEntry(watermark);
        final StreamRecordQueueEntry<Integer> streamRecordQueueEntry = new StreamRecordQueueEntry(streamRecord);
        queue.put(watermarkQueueEntry);
        queue.put(streamRecordQueueEntry);
        Assert.assertEquals(2, queue.size());
        Assert.assertFalse(queue.tryPut(new WatermarkQueueEntry(nextWatermark)));
        Collection<StreamElementQueueEntry<?>> actualValues = queue.values();
        List<StreamElementQueueEntry<?>> expectedValues = Arrays.asList(watermarkQueueEntry, streamRecordQueueEntry);
        Assert.assertEquals(expectedValues, actualValues);
        Mockito.verify(operatorActions, Mockito.never()).failOperator(ArgumentMatchers.any(Exception.class));
    }

    @Test
    public void testPoll() throws InterruptedException {
        OperatorActions operatorActions = Mockito.mock(OperatorActions.class);
        StreamElementQueue queue = createStreamElementQueue(2, operatorActions);
        WatermarkQueueEntry watermarkQueueEntry = new WatermarkQueueEntry(new Watermark(0L));
        StreamRecordQueueEntry<Integer> streamRecordQueueEntry = new StreamRecordQueueEntry(new StreamRecord(42, 1L));
        queue.put(watermarkQueueEntry);
        queue.put(streamRecordQueueEntry);
        Assert.assertEquals(watermarkQueueEntry, queue.peekBlockingly());
        Assert.assertEquals(2, queue.size());
        Assert.assertEquals(watermarkQueueEntry, queue.poll());
        Assert.assertEquals(1, queue.size());
        streamRecordQueueEntry.complete(Collections.<Integer>emptyList());
        Assert.assertEquals(streamRecordQueueEntry, queue.poll());
        Assert.assertEquals(0, queue.size());
        Assert.assertTrue(queue.isEmpty());
        Mockito.verify(operatorActions, Mockito.never()).failOperator(ArgumentMatchers.any(Exception.class));
    }

    /**
     * Tests that a put operation blocks if the queue is full.
     */
    @Test
    public void testBlockingPut() throws Exception {
        OperatorActions operatorActions = Mockito.mock(OperatorActions.class);
        final StreamElementQueue queue = createStreamElementQueue(1, operatorActions);
        StreamRecordQueueEntry<Integer> streamRecordQueueEntry = new StreamRecordQueueEntry(new StreamRecord(42, 0L));
        final StreamRecordQueueEntry<Integer> streamRecordQueueEntry2 = new StreamRecordQueueEntry(new StreamRecord(43, 1L));
        queue.put(streamRecordQueueEntry);
        Assert.assertEquals(1, queue.size());
        CompletableFuture<Void> putOperation = CompletableFuture.runAsync(() -> {
            try {
                queue.put(streamRecordQueueEntry2);
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        }, StreamElementQueueTest.executor);
        // give the future a chance to complete
        Thread.sleep(10L);
        // but it shouldn't ;-)
        Assert.assertFalse(putOperation.isDone());
        streamRecordQueueEntry.complete(Collections.<Integer>emptyList());
        // polling the completed head element frees the queue again
        Assert.assertEquals(streamRecordQueueEntry, queue.poll());
        // now the put operation should complete
        putOperation.get();
        Mockito.verify(operatorActions, Mockito.never()).failOperator(ArgumentMatchers.any(Exception.class));
    }

    /**
     * Test that a poll operation on an empty queue blocks.
     */
    @Test
    public void testBlockingPoll() throws Exception {
        OperatorActions operatorActions = Mockito.mock(OperatorActions.class);
        final StreamElementQueue queue = createStreamElementQueue(1, operatorActions);
        WatermarkQueueEntry watermarkQueueEntry = new WatermarkQueueEntry(new Watermark(1L));
        StreamRecordQueueEntry<Integer> streamRecordQueueEntry = new StreamRecordQueueEntry(new StreamRecord(1, 2L));
        Assert.assertTrue(queue.isEmpty());
        CompletableFuture<AsyncResult> peekOperation = CompletableFuture.supplyAsync(() -> {
            try {
                return queue.peekBlockingly();
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        }, StreamElementQueueTest.executor);
        Thread.sleep(10L);
        Assert.assertFalse(peekOperation.isDone());
        queue.put(watermarkQueueEntry);
        AsyncResult watermarkResult = peekOperation.get();
        Assert.assertEquals(watermarkQueueEntry, watermarkResult);
        Assert.assertEquals(1, queue.size());
        Assert.assertEquals(watermarkQueueEntry, queue.poll());
        Assert.assertTrue(queue.isEmpty());
        CompletableFuture<AsyncResult> pollOperation = CompletableFuture.supplyAsync(() -> {
            try {
                return queue.poll();
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        }, StreamElementQueueTest.executor);
        Thread.sleep(10L);
        Assert.assertFalse(pollOperation.isDone());
        queue.put(streamRecordQueueEntry);
        Thread.sleep(10L);
        Assert.assertFalse(pollOperation.isDone());
        streamRecordQueueEntry.complete(Collections.<Integer>emptyList());
        Assert.assertEquals(streamRecordQueueEntry, pollOperation.get());
        Assert.assertTrue(queue.isEmpty());
        Mockito.verify(operatorActions, Mockito.never()).failOperator(ArgumentMatchers.any(Exception.class));
    }
}
