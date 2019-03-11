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
package org.apache.flink.runtime.io.network.netty;


import NettyMessage.BufferResponse;
import NettyMessage.ErrorResponse;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.apache.flink.runtime.execution.CancelTaskException;
import org.apache.flink.runtime.io.network.partition.ResultPartitionID;
import org.apache.flink.runtime.io.network.partition.ResultPartitionProvider;
import org.apache.flink.runtime.io.network.partition.ResultSubpartition.BufferAndBacklog;
import org.apache.flink.runtime.io.network.partition.ResultSubpartitionView;
import org.apache.flink.runtime.io.network.partition.consumer.InputChannelID;
import org.apache.flink.runtime.io.network.util.TestBufferFactory;
import org.apache.flink.shaded.netty4.io.netty.buffer.ByteBuf;
import org.apache.flink.shaded.netty4.io.netty.channel.embedded.EmbeddedChannel;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link PartitionRequestQueue}.
 */
public class PartitionRequestQueueTest {
    /**
     * In case of enqueuing an empty reader and a reader that actually has some buffers when channel is not writable,
     * on channelWritability change event should result in reading all of the messages.
     */
    @Test
    public void testNotifyReaderNonEmptyOnEmptyReaders() throws Exception {
        final int buffersToWrite = 5;
        PartitionRequestQueue queue = new PartitionRequestQueue();
        EmbeddedChannel channel = new EmbeddedChannel(queue);
        CreditBasedSequenceNumberingViewReader reader1 = new CreditBasedSequenceNumberingViewReader(new InputChannelID(0, 0), 10, queue);
        CreditBasedSequenceNumberingViewReader reader2 = new CreditBasedSequenceNumberingViewReader(new InputChannelID(1, 1), 10, queue);
        reader1.requestSubpartitionView(( partitionId, index, availabilityListener) -> new org.apache.flink.runtime.io.network.netty.EmptyAlwaysAvailableResultSubpartitionView(), new ResultPartitionID(), 0);
        reader1.notifyDataAvailable();
        Assert.assertTrue(reader1.isAvailable());
        Assert.assertFalse(reader1.isRegisteredAsAvailable());
        channel.unsafe().outboundBuffer().setUserDefinedWritability(1, false);
        Assert.assertFalse(channel.isWritable());
        reader1.notifyDataAvailable();
        channel.runPendingTasks();
        reader2.notifyDataAvailable();
        reader2.requestSubpartitionView(( partitionId, index, availabilityListener) -> new org.apache.flink.runtime.io.network.netty.DefaultBufferResultSubpartitionView(buffersToWrite), new ResultPartitionID(), 0);
        Assert.assertTrue(reader2.isAvailable());
        Assert.assertFalse(reader2.isRegisteredAsAvailable());
        reader2.notifyDataAvailable();
        // changing a channel writability should result in draining both reader1 and reader2
        channel.unsafe().outboundBuffer().setUserDefinedWritability(1, true);
        channel.runPendingTasks();
        Assert.assertEquals(buffersToWrite, channel.outboundMessages().size());
    }

    @Test
    public void testProducerFailedException() throws Exception {
        PartitionRequestQueue queue = new PartitionRequestQueue();
        ResultSubpartitionView view = new PartitionRequestQueueTest.ReleasedResultSubpartitionView();
        ResultPartitionProvider partitionProvider = ( partitionId, index, availabilityListener) -> view;
        EmbeddedChannel ch = new EmbeddedChannel(queue);
        CreditBasedSequenceNumberingViewReader seqView = new CreditBasedSequenceNumberingViewReader(new InputChannelID(), 2, queue);
        seqView.requestSubpartitionView(partitionProvider, new ResultPartitionID(), 0);
        // Add available buffer to trigger enqueue the erroneous view
        seqView.notifyDataAvailable();
        ch.runPendingTasks();
        // Read the enqueued msg
        Object msg = ch.readOutbound();
        Assert.assertEquals(msg.getClass(), ErrorResponse.class);
        NettyMessage.ErrorResponse err = ((NettyMessage.ErrorResponse) (msg));
        Assert.assertTrue(((err.cause) instanceof CancelTaskException));
    }

    /**
     * Tests {@link PartitionRequestQueue} buffer writing with default buffers.
     */
    @Test
    public void testDefaultBufferWriting() throws Exception {
        testBufferWriting(new PartitionRequestQueueTest.DefaultBufferResultSubpartitionView(1));
    }

    /**
     * Tests {@link PartitionRequestQueue} buffer writing with read-only buffers.
     */
    @Test
    public void testReadOnlyBufferWriting() throws Exception {
        testBufferWriting(new PartitionRequestQueueTest.ReadOnlyBufferResultSubpartitionView(1));
    }

    private static class DefaultBufferResultSubpartitionView extends PartitionRequestQueueTest.NoOpResultSubpartitionView {
        /**
         * Number of buffer in the backlog to report with every {@link #getNextBuffer()} call.
         */
        private final AtomicInteger buffersInBacklog;

        private DefaultBufferResultSubpartitionView(int buffersInBacklog) {
            this.buffersInBacklog = new AtomicInteger(buffersInBacklog);
        }

        @Nullable
        @Override
        public BufferAndBacklog getNextBuffer() {
            int buffers = buffersInBacklog.decrementAndGet();
            return new BufferAndBacklog(TestBufferFactory.createBuffer(10), (buffers > 0), buffers, false);
        }

        @Override
        public boolean isAvailable() {
            return (buffersInBacklog.get()) > 0;
        }
    }

    private static class ReadOnlyBufferResultSubpartitionView extends PartitionRequestQueueTest.DefaultBufferResultSubpartitionView {
        private ReadOnlyBufferResultSubpartitionView(int buffersInBacklog) {
            super(buffersInBacklog);
        }

        @Nullable
        @Override
        public BufferAndBacklog getNextBuffer() {
            BufferAndBacklog nextBuffer = super.getNextBuffer();
            return new BufferAndBacklog(nextBuffer.buffer().readOnlySlice(), nextBuffer.isMoreAvailable(), nextBuffer.buffersInBacklog(), nextBuffer.nextBufferIsEvent());
        }
    }

    private static class EmptyAlwaysAvailableResultSubpartitionView extends PartitionRequestQueueTest.NoOpResultSubpartitionView {
        @Override
        public boolean isReleased() {
            return false;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    private static class ReleasedResultSubpartitionView extends PartitionRequestQueueTest.EmptyAlwaysAvailableResultSubpartitionView {
        @Override
        public boolean isReleased() {
            return true;
        }

        @Override
        public Throwable getFailureCause() {
            return new RuntimeException("Expected test exception");
        }
    }

    /**
     * Tests {@link PartitionRequestQueue#enqueueAvailableReader(NetworkSequenceViewReader)},
     * verifying the reader would be enqueued in the pipeline if the next sending buffer is an event
     * even though it has no available credits.
     */
    @Test
    public void testEnqueueReaderByNotifyingEventBuffer() throws Exception {
        // setup
        final ResultSubpartitionView view = new PartitionRequestQueueTest.NextIsEventResultSubpartitionView();
        ResultPartitionProvider partitionProvider = ( partitionId, index, availabilityListener) -> view;
        final InputChannelID receiverId = new InputChannelID();
        final PartitionRequestQueue queue = new PartitionRequestQueue();
        final CreditBasedSequenceNumberingViewReader reader = new CreditBasedSequenceNumberingViewReader(receiverId, 0, queue);
        final EmbeddedChannel channel = new EmbeddedChannel(queue);
        reader.requestSubpartitionView(partitionProvider, new ResultPartitionID(), 0);
        // block the channel so that we see an intermediate state in the test
        ByteBuf channelBlockingBuffer = PartitionRequestQueueTest.blockChannel(channel);
        Assert.assertNull(channel.readOutbound());
        // Notify an available event buffer to trigger enqueue the reader
        reader.notifyDataAvailable();
        channel.runPendingTasks();
        // The reader is enqueued in the pipeline because the next buffer is an event, even though no credits are available
        Assert.assertThat(queue.getAvailableReaders(), Matchers.contains(reader));// contains only (this) one!

        Assert.assertEquals(0, reader.getNumCreditsAvailable());
        // Flush the buffer to make the channel writable again and see the final results
        channel.flush();
        Assert.assertSame(channelBlockingBuffer, channel.readOutbound());
        Assert.assertEquals(0, queue.getAvailableReaders().size());
        Assert.assertEquals(0, reader.getNumCreditsAvailable());
        Assert.assertNull(channel.readOutbound());
    }

    private static class NextIsEventResultSubpartitionView extends PartitionRequestQueueTest.NoOpResultSubpartitionView {
        @Override
        public boolean nextBufferIsEvent() {
            return true;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    /**
     * Tests {@link PartitionRequestQueue#enqueueAvailableReader(NetworkSequenceViewReader)},
     * verifying the reader would be enqueued in the pipeline iff it has both available credits and buffers.
     */
    @Test
    public void testEnqueueReaderByNotifyingBufferAndCredit() throws Exception {
        // setup
        final ResultSubpartitionView view = new PartitionRequestQueueTest.DefaultBufferResultSubpartitionView(10);
        ResultPartitionProvider partitionProvider = ( partitionId, index, availabilityListener) -> view;
        final InputChannelID receiverId = new InputChannelID();
        final PartitionRequestQueue queue = new PartitionRequestQueue();
        final CreditBasedSequenceNumberingViewReader reader = new CreditBasedSequenceNumberingViewReader(receiverId, 0, queue);
        final EmbeddedChannel channel = new EmbeddedChannel(queue);
        reader.requestSubpartitionView(partitionProvider, new ResultPartitionID(), 0);
        queue.notifyReaderCreated(reader);
        // block the channel so that we see an intermediate state in the test
        ByteBuf channelBlockingBuffer = PartitionRequestQueueTest.blockChannel(channel);
        Assert.assertNull(channel.readOutbound());
        // Notify available buffers to trigger enqueue the reader
        final int notifyNumBuffers = 5;
        for (int i = 0; i < notifyNumBuffers; i++) {
            reader.notifyDataAvailable();
        }
        channel.runPendingTasks();
        // the reader is not enqueued in the pipeline because no credits are available
        // -> it should still have the same number of pending buffers
        Assert.assertEquals(0, queue.getAvailableReaders().size());
        Assert.assertTrue(reader.hasBuffersAvailable());
        Assert.assertFalse(reader.isRegisteredAsAvailable());
        Assert.assertEquals(0, reader.getNumCreditsAvailable());
        // Notify available credits to trigger enqueue the reader again
        final int notifyNumCredits = 3;
        for (int i = 1; i <= notifyNumCredits; i++) {
            queue.addCredit(receiverId, 1);
            // the reader is enqueued in the pipeline because it has both available buffers and credits
            // since the channel is blocked though, we will not process anything and only enqueue the
            // reader once
            Assert.assertTrue(reader.isRegisteredAsAvailable());
            Assert.assertThat(queue.getAvailableReaders(), Matchers.contains(reader));// contains only (this) one!

            Assert.assertEquals(i, reader.getNumCreditsAvailable());
            Assert.assertTrue(reader.hasBuffersAvailable());
        }
        // Flush the buffer to make the channel writable again and see the final results
        channel.flush();
        Assert.assertSame(channelBlockingBuffer, channel.readOutbound());
        Assert.assertEquals(0, queue.getAvailableReaders().size());
        Assert.assertEquals(0, reader.getNumCreditsAvailable());
        Assert.assertTrue(reader.hasBuffersAvailable());
        Assert.assertFalse(reader.isRegisteredAsAvailable());
        for (int i = 1; i <= notifyNumCredits; i++) {
            Assert.assertThat(channel.readOutbound(), Matchers.instanceOf(BufferResponse.class));
        }
        Assert.assertNull(channel.readOutbound());
    }

    private static class NoOpResultSubpartitionView implements ResultSubpartitionView {
        @Nullable
        public BufferAndBacklog getNextBuffer() {
            return null;
        }

        @Override
        public void notifyDataAvailable() {
        }

        @Override
        public void releaseAllResources() {
        }

        @Override
        public void notifySubpartitionConsumed() {
        }

        @Override
        public boolean isReleased() {
            return true;
        }

        @Override
        public Throwable getFailureCause() {
            return null;
        }

        @Override
        public boolean nextBufferIsEvent() {
            return false;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }
    }
}
