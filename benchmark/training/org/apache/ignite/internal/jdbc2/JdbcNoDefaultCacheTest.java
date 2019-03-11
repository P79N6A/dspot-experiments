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
package org.apache.ignite.internal.jdbc2;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import org.apache.ignite.IgniteJdbcDriver;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;


/**
 *
 */
public class JdbcNoDefaultCacheTest extends GridCommonAbstractTest {
    /**
     * First cache name.
     */
    private static final String CACHE1_NAME = "cache1";

    /**
     * Second cache name.
     */
    private static final String CACHE2_NAME = "cache2";

    /**
     * Ignite configuration URL.
     */
    private static final String CFG_URL = "modules/clients/src/test/config/jdbc-config.xml";

    /**
     * Grid count.
     */
    private static final int GRID_CNT = 2;

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDefaults() throws Exception {
        String url = (IgniteJdbcDriver.CFG_URL_PREFIX) + (JdbcNoDefaultCacheTest.CFG_URL);
        try (Connection conn = DriverManager.getConnection(url)) {
            assertNotNull(conn);
            assertTrue(ignite().configuration().isClientMode());
        }
        try (Connection conn = DriverManager.getConnection((url + '/'))) {
            assertNotNull(conn);
            assertTrue(ignite().configuration().isClientMode());
        }
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testNoCacheNameQuery() throws Exception {
        try (Connection conn = DriverManager.getConnection(((IgniteJdbcDriver.CFG_URL_PREFIX) + (JdbcNoDefaultCacheTest.CFG_URL)));final Statement stmt = conn.createStatement()) {
            assertNotNull(stmt);
            assertFalse(stmt.isClosed());
            Throwable throwable = GridTestUtils.assertThrows(null, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    stmt.execute("select t._key, t._val from \"cache1\".Integer t");
                    return null;
                }
            }, SQLException.class, "Failed to query Ignite.");
            assertEquals(throwable.getCause().getMessage(), "Ouch! Argument is invalid: Cache name must not be null or empty.");
        }
    }
}
