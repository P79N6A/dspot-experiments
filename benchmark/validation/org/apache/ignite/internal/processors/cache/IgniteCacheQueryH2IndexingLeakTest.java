/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.internal.processors.cache;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.processors.query.GridQueryProcessor;
import org.apache.ignite.internal.util.lang.GridAbsPredicate;
import org.apache.ignite.internal.util.typedef.CAX;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;


/**
 * Tests leaks at the IgniteH2Indexing
 */
public class IgniteCacheQueryH2IndexingLeakTest extends GridCommonAbstractTest {
    /**
     *
     */
    private static final long TEST_TIMEOUT = (2 * 60) * 1000;

    /**
     * Threads to parallel execute queries
     */
    private static final int THREAD_COUNT = 10;

    /**
     * Timeout
     */
    private static final long STMT_CACHE_CLEANUP_TIMEOUT = 500;

    /**
     * Orig cleanup period.
     */
    private static String origCacheCleanupPeriod;

    /**
     * Orig usage timeout.
     */
    private static String origCacheThreadUsageTimeout;

    /**
     *
     */
    private static final int ITERATIONS = 5;

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testLeaksInIgniteH2IndexingOnTerminatedThread() throws Exception {
        final IgniteCache<Integer, Integer> c = grid(0).cache(DEFAULT_CACHE_NAME);
        for (int i = 0; i < (IgniteCacheQueryH2IndexingLeakTest.ITERATIONS); ++i) {
            info(("Iteration #" + i));
            final AtomicBoolean stop = new AtomicBoolean();
            // Open iterator on the created cursor: add entries to the cache.
            IgniteInternalFuture<?> fut = multithreadedAsync(new CAX() {
                @SuppressWarnings("unchecked")
                @Override
                public void applyx() throws IgniteCheckedException {
                    while (!(stop.get())) {
                        c.query(new SqlQuery(Integer.class, "_val >= 0")).getAll();
                        c.query(new SqlQuery(Integer.class, "_val >= 1")).getAll();
                    } 
                }
            }, IgniteCacheQueryH2IndexingLeakTest.THREAD_COUNT);
            final GridQueryProcessor qryProc = grid(0).context().query();
            try {
                // Wait for stmt cache entry is created for each thread.
                assertTrue(GridTestUtils.waitForCondition(new GridAbsPredicate() {
                    @Override
                    public boolean apply() {
                        // '>' case is for lazy query flag turned on - in this case, there's more threads
                        // than those run by test explicitly, and we can't rely on exact number.
                        // Still the main check for this test is that all threads, no matter how many of them
                        // is out there, are terminated and their statement caches are cleaned up.
                        return (IgniteCacheQueryH2IndexingLeakTest.getStatementCacheSize(qryProc)) >= (IgniteCacheQueryH2IndexingLeakTest.THREAD_COUNT);
                    }
                }, IgniteCacheQueryH2IndexingLeakTest.STMT_CACHE_CLEANUP_TIMEOUT));
            } finally {
                stop.set(true);
            }
            fut.get();
            // Wait for stmtCache is cleaned up because all user threads are terminated.
            assertTrue(GridTestUtils.waitForCondition(new GridAbsPredicate() {
                @Override
                public boolean apply() {
                    return (IgniteCacheQueryH2IndexingLeakTest.getStatementCacheSize(qryProc)) == 0;
                }
            }, ((IgniteCacheQueryH2IndexingLeakTest.STMT_CACHE_CLEANUP_TIMEOUT) * 10)));
        }
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testLeaksInIgniteH2IndexingOnUnusedThread() throws Exception {
        final IgniteCache<Integer, Integer> c = grid(0).cache(DEFAULT_CACHE_NAME);
        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < (IgniteCacheQueryH2IndexingLeakTest.ITERATIONS); ++i) {
            info(("Iteration #" + i));
            // Open iterator on the created cursor: add entries to the cache
            IgniteInternalFuture<?> fut = multithreadedAsync(new CAX() {
                @SuppressWarnings("unchecked")
                @Override
                public void applyx() throws IgniteCheckedException {
                    c.query(new SqlQuery(Integer.class, "_val >= 0")).getAll();
                    U.await(latch);
                }
            }, IgniteCacheQueryH2IndexingLeakTest.THREAD_COUNT);
            Thread.sleep(IgniteCacheQueryH2IndexingLeakTest.STMT_CACHE_CLEANUP_TIMEOUT);
            // Wait for stmtCache is cleaned up because all user threads don't perform queries a lot of time.
            assertTrue(GridTestUtils.waitForCondition(new GridAbsPredicate() {
                @Override
                public boolean apply() {
                    return (IgniteCacheQueryH2IndexingLeakTest.getStatementCacheSize(grid(0).context().query())) == 0;
                }
            }, ((IgniteCacheQueryH2IndexingLeakTest.STMT_CACHE_CLEANUP_TIMEOUT) * 2)));
            latch.countDown();
            fut.get();
        }
    }
}
