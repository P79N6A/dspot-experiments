/**
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ehcache.clustered.replication;


import Consistency.STRONG;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.ClusteredTests;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteredStoreConfigurationBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.clustered.client.config.builders.TimeoutsBuilder;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.junit.ClassRule;
import org.junit.Test;
import org.terracotta.testing.rules.Cluster;


public class DuplicateTest extends ClusteredTests {
    private static final String RESOURCE_CONFIG = "<config xmlns:ohr='http://www.terracotta.org/config/offheap-resource'>" + ((("<ohr:offheap-resources>" + "<ohr:resource name=\"primary-server-resource\" unit=\"MB\">512</ohr:resource>") + "</ohr:offheap-resources>") + "</config>\n");

    private PersistentCacheManager cacheManager;

    @ClassRule
    public static Cluster CLUSTER = newCluster(2).in(new File("build/cluster")).withServiceFragment(DuplicateTest.RESOURCE_CONFIG).build();

    @Test
    public void duplicateAfterFailoverAreReturningTheCorrectResponse() throws Exception {
        CacheManagerBuilder<PersistentCacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder().with(ClusteringServiceConfigurationBuilder.cluster(DuplicateTest.CLUSTER.getConnectionURI()).timeouts(TimeoutsBuilder.timeouts().write(Duration.ofSeconds(30))).autoCreate().defaultServerResource("primary-server-resource")).withCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, String.class, ResourcePoolsBuilder.newResourcePoolsBuilder().with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 10, MemoryUnit.MB))).withResilienceStrategy(failingResilienceStrategy()).add(ClusteredStoreConfigurationBuilder.withConsistency(STRONG)));
        cacheManager = builder.build(true);
        Cache<Integer, String> cache = cacheManager.getCache("cache", Integer.class, String.class);
        int numEntries = 3000;
        AtomicInteger currentEntry = new AtomicInteger();
        // Perform put operations in another thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<?> puts = executorService.submit(() -> {
                while (true) {
                    int i = currentEntry.getAndIncrement();
                    if (i >= numEntries) {
                        break;
                    }
                    cache.put(i, ("value:" + i));
                } 
            });
            while ((currentEntry.get()) < 100);// wait to make sure some entries are added before shutdown

            // Failover to mirror when put & replication are in progress
            DuplicateTest.CLUSTER.getClusterControl().terminateActive();
            puts.get(30, TimeUnit.SECONDS);
            // Verify cache entries on mirror
            for (int i = 0; i < numEntries; i++) {
                assertThat(cache.get(i)).isEqualTo(("value:" + i));
            }
        } finally {
            executorService.shutdownNow();
        }
    }
}
