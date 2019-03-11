package com.orientechnologies.common.directmemory;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;


public class OByteBufferPoolTest {
    @Test
    public void testByteBufferAllocationZeroPool() {
        final ODirectMemoryAllocator allocator = new ODirectMemoryAllocator();
        final OByteBufferPool byteBufferPool = new OByteBufferPool(42, allocator, 0);
        final OPointer pointerOne = byteBufferPool.acquireDirect(false);
        Assert.assertEquals(42, pointerOne.getNativeByteBuffer().capacity());
        Assert.assertEquals(42, allocator.getMemoryConsumption());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        final OPointer pointerTwo = byteBufferPool.acquireDirect(true);
        Assert.assertEquals(42, pointerTwo.getNativeByteBuffer().capacity());
        Assert.assertEquals(84, allocator.getMemoryConsumption());
        assertBufferIsClear(pointerTwo.getNativeByteBuffer());
        byteBufferPool.release(pointerOne);
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(42, allocator.getMemoryConsumption());
        byteBufferPool.release(pointerTwo);
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(0, allocator.getMemoryConsumption());
        byteBufferPool.clear();
        byteBufferPool.checkMemoryLeaks();
    }

    @Test
    public void testByteBufferAllocationTwoPagesPool() {
        final ODirectMemoryAllocator allocator = new ODirectMemoryAllocator();
        final OByteBufferPool byteBufferPool = new OByteBufferPool(42, allocator, 2);
        OPointer pointerOne = byteBufferPool.acquireDirect(false);
        Assert.assertEquals(42, pointerOne.getNativeByteBuffer().capacity());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(42, allocator.getMemoryConsumption());
        OPointer pointerTwo = byteBufferPool.acquireDirect(true);
        Assert.assertEquals(42, pointerTwo.getNativeByteBuffer().capacity());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(84, allocator.getMemoryConsumption());
        assertBufferIsClear(pointerTwo.getNativeByteBuffer());
        OPointer pointerThree = byteBufferPool.acquireDirect(false);
        Assert.assertEquals(42, pointerThree.getNativeByteBuffer().capacity());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        byteBufferPool.release(pointerOne);
        Assert.assertEquals(1, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        byteBufferPool.release(pointerTwo);
        Assert.assertEquals(2, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        byteBufferPool.release(pointerThree);
        Assert.assertEquals(2, byteBufferPool.getPoolSize());
        Assert.assertEquals(84, allocator.getMemoryConsumption());
        pointerOne = byteBufferPool.acquireDirect(true);
        Assert.assertEquals(42, pointerOne.getNativeByteBuffer().capacity());
        Assert.assertEquals(1, byteBufferPool.getPoolSize());
        Assert.assertEquals(84, allocator.getMemoryConsumption());
        assertBufferIsClear(pointerOne.getNativeByteBuffer());
        pointerTwo = byteBufferPool.acquireDirect(true);
        Assert.assertEquals(42, pointerTwo.getNativeByteBuffer().capacity());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(84, allocator.getMemoryConsumption());
        assertBufferIsClear(pointerTwo.getNativeByteBuffer());
        pointerThree = byteBufferPool.acquireDirect(false);
        Assert.assertEquals(42, pointerThree.getNativeByteBuffer().capacity());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        byteBufferPool.release(pointerThree);
        Assert.assertEquals(1, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        pointerThree = byteBufferPool.acquireDirect(true);
        Assert.assertEquals(42, pointerThree.getNativeByteBuffer().capacity());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        assertBufferIsClear(pointerThree.getNativeByteBuffer());
        byteBufferPool.release(pointerThree);
        Assert.assertEquals(1, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        byteBufferPool.release(pointerOne);
        Assert.assertEquals(2, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        Assert.assertEquals(2, byteBufferPool.getPoolSize());
        Assert.assertEquals(126, allocator.getMemoryConsumption());
        byteBufferPool.release(pointerTwo);
        Assert.assertEquals(2, byteBufferPool.getPoolSize());
        Assert.assertEquals(84, allocator.getMemoryConsumption());
        byteBufferPool.clear();
        Assert.assertEquals(0, allocator.getMemoryConsumption());
        Assert.assertEquals(0, byteBufferPool.getPoolSize());
        byteBufferPool.checkMemoryLeaks();
    }

    private static final class Allocator implements Callable<Void> {
        private final OByteBufferPool pool;

        private final ThreadLocalRandom random = ThreadLocalRandom.current();

        private final AtomicBoolean stop;

        private final List<OPointer> allocatedPointers = new ArrayList<>();

        private Allocator(OByteBufferPool pool, AtomicBoolean stop) {
            this.pool = pool;
            this.stop = stop;
        }

        @Override
        public Void call() {
            try {
                while (!(stop.get())) {
                    if ((allocatedPointers.size()) < 500) {
                        OPointer pointer = pool.acquireDirect(false);
                        allocatedPointers.add(pointer);
                    } else
                        if ((allocatedPointers.size()) < 1000) {
                            if ((random.nextDouble()) <= 0.5) {
                                OPointer pointer = pool.acquireDirect(false);
                                allocatedPointers.add(pointer);
                            } else {
                                final int bufferToRemove = random.nextInt(allocatedPointers.size());
                                final OPointer pointer = allocatedPointers.remove(bufferToRemove);
                                pool.release(pointer);
                            }
                        } else {
                            if ((random.nextDouble()) <= 0.4) {
                                OPointer pointer = pool.acquireDirect(false);
                                allocatedPointers.add(pointer);
                            } else {
                                final int bufferToRemove = random.nextInt(allocatedPointers.size());
                                final OPointer pointer = allocatedPointers.remove(bufferToRemove);
                                pool.release(pointer);
                            }
                        }

                } 
                System.out.println(("Allocated buffers " + (allocatedPointers.size())));
                for (OPointer pointer : allocatedPointers) {
                    pool.release(pointer);
                }
            } catch (Exception | Error e) {
                e.printStackTrace();
                throw e;
            }
            return null;
        }
    }
}
