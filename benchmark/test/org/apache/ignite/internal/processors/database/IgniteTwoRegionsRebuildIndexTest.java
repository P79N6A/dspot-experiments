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
package org.apache.ignite.internal.processors.database;


import org.apache.ignite.Ignite;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;


/**
 * Tests the case when preformed index rebuild for created by client in-memory cache.
 */
public class IgniteTwoRegionsRebuildIndexTest extends GridCommonAbstractTest {
    /**
     *
     */
    private static final String PERSISTED_CACHE = "persisted";

    /**
     *
     */
    private static final String INMEMORY_CACHE = "inmemory";

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testRebuildIndexes() throws Exception {
        startGrid("server");
        Ignite client = startGrid("client");
        client.cluster().active(true);
        populateData(client, IgniteTwoRegionsRebuildIndexTest.PERSISTED_CACHE);
        populateData(client, IgniteTwoRegionsRebuildIndexTest.INMEMORY_CACHE);
        stopGrid("server");
        startGrid("server");
        stopGrid("client");
        startGrid("client");
    }
}
