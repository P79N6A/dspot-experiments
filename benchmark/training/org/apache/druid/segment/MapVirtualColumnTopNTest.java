/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.segment;


import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.druid.java.util.common.DateTimes;
import org.apache.druid.java.util.common.Intervals;
import org.apache.druid.java.util.common.granularity.Granularities;
import org.apache.druid.query.QueryPlus;
import org.apache.druid.query.QueryRunner;
import org.apache.druid.query.QueryRunnerTestHelper;
import org.apache.druid.query.Result;
import org.apache.druid.query.aggregation.CountAggregatorFactory;
import org.apache.druid.query.dimension.DefaultDimensionSpec;
import org.apache.druid.query.topn.DimensionAndMetricValueExtractor;
import org.apache.druid.query.topn.NumericTopNMetricSpec;
import org.apache.druid.query.topn.TopNQuery;
import org.apache.druid.query.topn.TopNResultValue;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class MapVirtualColumnTopNTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private QueryRunner<Result<TopNResultValue>> runner;

    @Test
    public void testWithMapColumn() {
        final TopNQuery query = // params is the map type
        new TopNQuery(new org.apache.druid.query.TableDataSource(QueryRunnerTestHelper.dataSource), VirtualColumns.create(ImmutableList.of(new MapVirtualColumn("keys", "values", "params"))), new DefaultDimensionSpec("params", "params"), new NumericTopNMetricSpec("count"), 1, new org.apache.druid.query.spec.MultipleIntervalSegmentSpec(ImmutableList.of(Intervals.of("2011/2012"))), null, Granularities.ALL, ImmutableList.of(new CountAggregatorFactory("count")), null, null);
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("Map column doesn't support getRow()");
        runner.run(QueryPlus.wrap(query), new HashMap()).toList();
    }

    @Test
    public void testWithSubColumn() {
        final TopNQuery query = // params.key3 is string
        new TopNQuery(new org.apache.druid.query.TableDataSource(QueryRunnerTestHelper.dataSource), VirtualColumns.create(ImmutableList.of(new MapVirtualColumn("keys", "values", "params"))), new DefaultDimensionSpec("params.key3", "params.key3"), new NumericTopNMetricSpec("count"), 2, new org.apache.druid.query.spec.MultipleIntervalSegmentSpec(ImmutableList.of(Intervals.of("2011/2012"))), null, Granularities.ALL, ImmutableList.of(new CountAggregatorFactory("count")), null, null);
        final List<Result<TopNResultValue>> result = runner.run(QueryPlus.wrap(query), new HashMap()).toList();
        final List<Result<TopNResultValue>> expected = Collections.singletonList(new Result(DateTimes.of("2011-01-12T00:00:00.000Z"), new TopNResultValue(ImmutableList.of(new DimensionAndMetricValueExtractor(MapVirtualColumnTestBase.mapOf("count", 2L, "params.key3", null)), new DimensionAndMetricValueExtractor(MapVirtualColumnTestBase.mapOf("count", 1L, "params.key3", "value3"))))));
        Assert.assertEquals(expected, result);
    }
}
