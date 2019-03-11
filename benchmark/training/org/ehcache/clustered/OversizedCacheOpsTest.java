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
package org.ehcache.clustered;


import java.io.File;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.terracotta.testing.rules.Cluster;


public class OversizedCacheOpsTest extends ClusteredTests {
    private static final String RESOURCE_CONFIG = "<config xmlns:ohr='http://www.terracotta.org/config/offheap-resource'>" + ((("<ohr:offheap-resources>" + "<ohr:resource name=\"primary-server-resource\" unit=\"MB\">2</ohr:resource>") + "</ohr:offheap-resources>") + "</config>\n");

    @ClassRule
    public static Cluster CLUSTER = newCluster().in(new File("build/cluster")).withServiceFragment(OversizedCacheOpsTest.RESOURCE_CONFIG).build();

    @Test
    public void overSizedCacheOps() throws Exception {
        CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder().with(ClusteringServiceConfigurationBuilder.cluster(OversizedCacheOpsTest.CLUSTER.getConnectionURI().resolve("/crud-cm")).autoCreate().defaultServerResource("primary-server-resource"));
        try (PersistentCacheManager cacheManager = clusteredCacheManagerBuilder.build(true)) {
            CacheConfiguration<Long, String> config = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.newResourcePoolsBuilder().with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 1, MemoryUnit.MB))).build();
            Cache<Long, String> cache = cacheManager.createCache("clustered-cache", config);
            cache.put(1L, "The one");
            cache.put(2L, "The two");
            cache.put(1L, "Another one");
            cache.put(3L, "The three");
            Assert.assertThat(cache.get(1L), Matchers.equalTo("Another one"));
            Assert.assertThat(cache.get(2L), Matchers.equalTo("The two"));
            Assert.assertThat(cache.get(3L), Matchers.equalTo("The three"));
            cache.put(1L, buildLargeString(2));
            Assert.assertThat(cache.get(1L), Matchers.is(Matchers.nullValue()));
            // ensure others are not evicted
            Assert.assertThat(cache.get(2L), Matchers.equalTo("The two"));
            Assert.assertThat(cache.get(3L), Matchers.equalTo("The three"));
        }
    }
}
