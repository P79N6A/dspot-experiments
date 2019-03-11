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
package org.apache.beam.sdk.extensions.sql;


import Duration.ZERO;
import Schema.FieldType.INT32;
import java.util.Arrays;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.GlobalWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.Row;
import org.hamcrest.Matchers;
import org.joda.time.Duration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.apache.beam.sdk.extensions.sql.TestUtils.RowsBuilder.of;


/**
 * Tests for joins in queries.
 */
public class BeamSqlDslJoinTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    private static final Schema SOURCE_ROW_TYPE = Schema.builder().addNullableField("order_id", INT32).addNullableField("site_id", INT32).addNullableField("price", INT32).build();

    private static final Schema RESULT_ROW_TYPE = Schema.builder().addNullableField("order_id", INT32).addNullableField("site_id", INT32).addNullableField("price", INT32).addNullableField("order_id0", INT32).addNullableField("site_id0", INT32).addNullableField("price0", INT32).build();

    @Test
    public void testInnerJoin() throws Exception {
        String sql = "SELECT *  " + ((("FROM ORDER_DETAILS1 o1" + " JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PAssert.that(queryFromOrderTables(sql)).containsInAnyOrder(of(BeamSqlDslJoinTest.RESULT_ROW_TYPE).addRows(2, 3, 3, 1, 2, 3).getRows());
        pipeline.run();
    }

    @Test
    public void testLeftOuterJoin() throws Exception {
        String sql = "SELECT *  " + ((("FROM ORDER_DETAILS1 o1" + " LEFT OUTER JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PAssert.that(queryFromOrderTables(sql)).containsInAnyOrder(of(BeamSqlDslJoinTest.RESULT_ROW_TYPE).addRows(1, 2, 3, null, null, null, 2, 3, 3, 1, 2, 3, 3, 4, 5, null, null, null).getRows());
        pipeline.run();
    }

    @Test
    public void testRightOuterJoin() throws Exception {
        String sql = "SELECT *  " + ((("FROM ORDER_DETAILS1 o1" + " RIGHT OUTER JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PAssert.that(queryFromOrderTables(sql)).containsInAnyOrder(of(BeamSqlDslJoinTest.RESULT_ROW_TYPE).addRows(2, 3, 3, 1, 2, 3, null, null, null, 2, 3, 3, null, null, null, 3, 4, 5).getRows());
        pipeline.run();
    }

    @Test
    public void testFullOuterJoin() throws Exception {
        String sql = "SELECT *  " + ((("FROM ORDER_DETAILS1 o1" + " FULL OUTER JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PAssert.that(queryFromOrderTables(sql)).containsInAnyOrder(of(BeamSqlDslJoinTest.RESULT_ROW_TYPE).addRows(2, 3, 3, 1, 2, 3, 1, 2, 3, null, null, null, 3, 4, 5, null, null, null, null, null, null, 2, 3, 3, null, null, null, 3, 4, 5).getRows());
        pipeline.run();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testException_nonEqualJoin() throws Exception {
        String sql = "SELECT *  " + ((("FROM ORDER_DETAILS1 o1" + " JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id>o2.site_id");
        pipeline.enableAbandonedNodeEnforcement(false);
        queryFromOrderTables(sql);
        pipeline.run();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testException_crossJoin() throws Exception {
        String sql = "SELECT *  " + "FROM ORDER_DETAILS1 o1, ORDER_DETAILS2 o2";
        pipeline.enableAbandonedNodeEnforcement(false);
        queryFromOrderTables(sql);
        pipeline.run();
    }

    @Test
    public void testJoinsUnboundedWithinWindowsWithDefaultTrigger() throws Exception {
        String sql = "SELECT o1.order_id, o1.price, o1.site_id, o2.order_id, o2.price, o2.site_id  " + ((("FROM ORDER_DETAILS1 o1" + " JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PCollection<Row> orders = ordersUnbounded().apply("window", Window.into(FixedWindows.of(Duration.standardSeconds(50))));
        PCollectionTuple inputs = TestUtils.tuple("ORDER_DETAILS1", orders, "ORDER_DETAILS2", orders);
        PAssert.that(inputs.apply("sql", SqlTransform.query(sql))).containsInAnyOrder(of(BeamSqlDslJoinTest.RESULT_ROW_TYPE).addRows(1, 2, 2, 2, 2, 1, 1, 4, 3, 3, 3, 1).getRows());
        pipeline.run();
    }

    @Test
    public void testRejectsUnboundedWithinWindowsWithEndOfWindowTrigger() throws Exception {
        String sql = "SELECT o1.order_id, o1.price, o1.site_id, o2.order_id, o2.price, o2.site_id  " + ((("FROM ORDER_DETAILS1 o1" + " JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PCollection<Row> orders = ordersUnbounded().apply("window", Window.<Row>into(FixedWindows.of(Duration.standardSeconds(50))).triggering(AfterWatermark.pastEndOfWindow()).withAllowedLateness(ZERO).accumulatingFiredPanes());
        PCollectionTuple inputs = TestUtils.tuple("ORDER_DETAILS1", orders, "ORDER_DETAILS2", orders);
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage(Matchers.stringContainsInOrder(Arrays.asList("once per window", "default trigger")));
        inputs.apply("sql", SqlTransform.query(sql));
        pipeline.run();
    }

    @Test
    public void testRejectsGlobalWindowsWithDefaultTriggerInUnboundedInput() throws Exception {
        String sql = "SELECT *  " + ((("FROM ORDER_DETAILS1 o1" + " JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PCollection<Row> orders = ordersUnbounded();
        PCollectionTuple inputs = TestUtils.tuple("ORDER_DETAILS1", orders, "ORDER_DETAILS2", orders);
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage(Matchers.stringContainsInOrder(Arrays.asList("once per window", "default trigger")));
        inputs.apply("sql", SqlTransform.query(sql));
        pipeline.run();
    }

    @Test
    public void testRejectsGlobalWindowsWithEndOfWindowTrigger() throws Exception {
        String sql = "SELECT o1.order_id, o1.price, o1.site_id, o2.order_id, o2.price, o2.site_id  " + ((("FROM ORDER_DETAILS1 o1" + " JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PCollection<Row> orders = ordersUnbounded().apply("window", Window.<Row>into(new GlobalWindows()).triggering(AfterWatermark.pastEndOfWindow()).withAllowedLateness(ZERO).accumulatingFiredPanes());
        PCollectionTuple inputs = TestUtils.tuple("ORDER_DETAILS1", orders, "ORDER_DETAILS2", orders);
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage(Matchers.stringContainsInOrder(Arrays.asList("once per window", "default trigger")));
        inputs.apply("sql", SqlTransform.query(sql));
        pipeline.run();
    }

    @Test
    public void testRejectsNonGlobalWindowsWithRepeatingTrigger() throws Exception {
        String sql = "SELECT o1.order_id, o1.price, o1.site_id, o2.order_id, o2.price, o2.site_id  " + ((("FROM ORDER_DETAILS1 o1" + " JOIN ORDER_DETAILS2 o2") + " on ") + " o1.order_id=o2.site_id AND o2.price=o1.site_id");
        PCollection<Row> orders = ordersUnbounded().apply("window", Window.<Row>into(FixedWindows.of(Duration.standardSeconds(203))).triggering(Repeatedly.forever(AfterWatermark.pastEndOfWindow())).withAllowedLateness(Duration.standardMinutes(2)).accumulatingFiredPanes());
        PCollectionTuple inputs = TestUtils.tuple("ORDER_DETAILS1", orders, "ORDER_DETAILS2", orders);
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage(Matchers.stringContainsInOrder(Arrays.asList("once per window", "default trigger")));
        inputs.apply("sql", SqlTransform.query(sql));
        pipeline.run();
    }
}
