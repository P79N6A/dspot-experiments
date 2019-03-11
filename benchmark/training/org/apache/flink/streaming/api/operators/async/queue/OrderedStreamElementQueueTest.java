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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.apache.flink.streaming.api.operators.async.OperatorActions;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.TestLogger;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


/**
 * {@link OrderedStreamElementQueue} specific tests.
 */
public class OrderedStreamElementQueueTest extends TestLogger {
    private static final long timeout = 10000L;

    private static ExecutorService executor;

    /**
     * Tests that only the head element is pulled from the ordered queue if it has been
     * completed.
     */
    @Test
    public void testCompletionOrder() throws Exception {
        OperatorActions operatorActions = Mockito.mock(OperatorActions.class);
        final OrderedStreamElementQueue queue = new OrderedStreamElementQueue(4, OrderedStreamElementQueueTest.executor, operatorActions);
        StreamRecordQueueEntry<Integer> entry1 = new StreamRecordQueueEntry(new org.apache.flink.streaming.runtime.streamrecord.StreamRecord(1, 0L));
        StreamRecordQueueEntry<Integer> entry2 = new StreamRecordQueueEntry(new org.apache.flink.streaming.runtime.streamrecord.StreamRecord(2, 1L));
        WatermarkQueueEntry entry3 = new WatermarkQueueEntry(new Watermark(2L));
        StreamRecordQueueEntry<Integer> entry4 = new StreamRecordQueueEntry(new org.apache.flink.streaming.runtime.streamrecord.StreamRecord(3, 3L));
        List<StreamElementQueueEntry<?>> expected = Arrays.asList(entry1, entry2, entry3, entry4);
        for (StreamElementQueueEntry<?> entry : expected) {
            queue.put(entry);
        }
        CompletableFuture<List<AsyncResult>> pollOperation = CompletableFuture.supplyAsync(() -> {
            List<AsyncResult> result = new ArrayList<>(4);
            while (!(queue.isEmpty())) {
                try {
                    result.add(queue.poll());
                } catch (InterruptedException e) {
                    throw new CompletionException(e);
                }
            } 
            return result;
        }, OrderedStreamElementQueueTest.executor);
        Thread.sleep(10L);
        Assert.assertFalse(pollOperation.isDone());
        entry2.complete(Collections.<Integer>emptyList());
        entry4.complete(Collections.<Integer>emptyList());
        Thread.sleep(10L);
        Assert.assertEquals(4, queue.size());
        entry1.complete(Collections.<Integer>emptyList());
        Assert.assertEquals(expected, pollOperation.get());
        Mockito.verify(operatorActions, Mockito.never()).failOperator(ArgumentMatchers.any(Exception.class));
    }
}
