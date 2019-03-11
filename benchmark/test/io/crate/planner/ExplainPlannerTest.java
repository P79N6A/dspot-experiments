/**
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */
package io.crate.planner;


import Row.EMPTY;
import com.google.common.collect.ImmutableList;
import io.crate.data.BatchIterator;
import io.crate.data.Row;
import io.crate.planner.node.management.ExplainPlan;
import io.crate.planner.operators.ExplainLogicalPlan;
import io.crate.planner.operators.LogicalPlan;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import io.crate.testing.TestingHelpers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.hamcrest.Matchers;
import org.junit.Test;


public class ExplainPlannerTest extends CrateDummyClusterServiceUnitTest {
    private static final List<String> EXPLAIN_TEST_STATEMENTS = ImmutableList.of("select 1 as connected", "select id from sys.cluster", "select id from users order by id", "select * from users", "select count(*) from users", "select name, count(distinct id) from users group by name", "select avg(id) from users", "select * from users where name = (select 'name')");

    private SQLExecutor e;

    @Test
    public void testExplain() {
        for (String statement : ExplainPlannerTest.EXPLAIN_TEST_STATEMENTS) {
            ExplainPlan plan = e.plan(("EXPLAIN " + statement));
            assertNotNull(plan);
            assertNotNull(plan.subPlan());
            assertFalse(plan.doAnalyze());
        }
    }

    @Test
    public void testExplainAnalyze() {
        for (String statement : ExplainPlannerTest.EXPLAIN_TEST_STATEMENTS) {
            ExplainPlan plan = e.plan(("EXPLAIN ANALYZE " + statement));
            assertNotNull(plan);
            assertNotNull(plan.subPlan());
            assertTrue(plan.doAnalyze());
        }
    }

    @Test
    public void testPrinter() {
        for (String statement : ExplainPlannerTest.EXPLAIN_TEST_STATEMENTS) {
            LogicalPlan plan = e.logicalPlan(statement);
            Map<String, Object> map = null;
            try {
                map = ExplainLogicalPlan.explainMap(plan, e.getPlannerContext(clusterService.state()), new io.crate.execution.dsl.projection.builder.ProjectionBuilder(TestingHelpers.getFunctions()));
            } catch (Exception e) {
                fail(("statement not printable: " + statement));
            }
            assertNotNull(map);
            assertThat(map.size(), Matchers.greaterThan(0));
        }
    }

    @Test
    public void testPrinterToXContent() {
        for (String statement : ExplainPlannerTest.EXPLAIN_TEST_STATEMENTS) {
            LogicalPlan plan = e.logicalPlan(statement);
            Map<String, Object> map = null;
            try {
                map = ExplainLogicalPlan.explainMap(plan, e.getPlannerContext(clusterService.state()), new io.crate.execution.dsl.projection.builder.ProjectionBuilder(TestingHelpers.getFunctions()));
            } catch (Exception e) {
                fail(("statement not printable: " + statement));
            }
            String json = null;
            try {
                XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
                xContentBuilder.value(map);
                json = BytesReference.bytes(xContentBuilder).utf8ToString();
            } catch (Exception e) {
                fail(("printed plan cannot be converted to xContent: " + statement));
            }
            assertNotNull(json);
        }
    }

    @Test
    public void testExplainAnalyzeMultiPhasePlanNotSupported() {
        ExplainPlan plan = e.plan("EXPLAIN ANALYZE SELECT * FROM users WHERE name = (SELECT 'crate') or id = (SELECT 1)");
        PlannerContext plannerContext = e.getPlannerContext(clusterService.state());
        CountDownLatch counter = new CountDownLatch(1);
        AtomicReference<BatchIterator<Row>> iterator = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        plan.execute(null, plannerContext, ( i, f) -> {
            iterator.set(i);
            failure.set(f);
            counter.countDown();
        }, EMPTY, SubQueryResults.EMPTY);
        assertNull(iterator.get());
        assertNotNull(failure.get());
        assertThat(failure.get().getMessage(), Matchers.containsString("EXPLAIN ANALYZE does not support profiling multi-phase plans, such as queries with scalar subselects."));
    }
}
