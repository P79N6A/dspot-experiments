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


import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for the {@link NettyBufferPool} wrapper.
 */
public class NettyBufferPoolTest {
    @Test
    public void testNoHeapAllocations() throws Exception {
        NettyBufferPool nettyBufferPool = new NettyBufferPool(1);
        // Buffers should prefer to be direct
        Assert.assertTrue(nettyBufferPool.buffer().isDirect());
        Assert.assertTrue(nettyBufferPool.buffer(128).isDirect());
        Assert.assertTrue(nettyBufferPool.buffer(128, 256).isDirect());
        // IO buffers should prefer to be direct
        Assert.assertTrue(nettyBufferPool.ioBuffer().isDirect());
        Assert.assertTrue(nettyBufferPool.ioBuffer(128).isDirect());
        Assert.assertTrue(nettyBufferPool.ioBuffer(128, 256).isDirect());
        // Disallow heap buffers
        try {
            nettyBufferPool.heapBuffer();
            Assert.fail("Unexpected heap buffer operation");
        } catch (UnsupportedOperationException ignored) {
        }
        try {
            nettyBufferPool.heapBuffer(128);
            Assert.fail("Unexpected heap buffer operation");
        } catch (UnsupportedOperationException ignored) {
        }
        try {
            nettyBufferPool.heapBuffer(128, 256);
            Assert.fail("Unexpected heap buffer operation");
        } catch (UnsupportedOperationException ignored) {
        }
        // Disallow composite heap buffers
        try {
            nettyBufferPool.compositeHeapBuffer();
            Assert.fail("Unexpected heap buffer operation");
        } catch (UnsupportedOperationException ignored) {
        }
        try {
            nettyBufferPool.compositeHeapBuffer(2);
            Assert.fail("Unexpected heap buffer operation");
        } catch (UnsupportedOperationException ignored) {
        }
        // Is direct buffer pooled!
        Assert.assertTrue(nettyBufferPool.isDirectBufferPooled());
    }

    @Test
    public void testAllocationsStatistics() throws Exception {
        NettyBufferPool nettyBufferPool = new NettyBufferPool(1);
        int chunkSize = nettyBufferPool.getChunkSize();
        {
            // Single large buffer allocates one chunk
            nettyBufferPool.directBuffer((chunkSize - 64));
            long allocated = nettyBufferPool.getNumberOfAllocatedBytes().get();
            Assert.assertEquals(chunkSize, allocated);
        }
        {
            // Allocate a little more (one more chunk required)
            nettyBufferPool.directBuffer(128);
            long allocated = nettyBufferPool.getNumberOfAllocatedBytes().get();
            Assert.assertEquals((2 * chunkSize), allocated);
        }
    }
}
