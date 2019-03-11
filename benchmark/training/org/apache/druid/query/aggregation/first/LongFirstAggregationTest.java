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
package org.apache.druid.query.aggregation.first;


import java.nio.ByteBuffer;
import org.apache.druid.collections.SerializablePair;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.apache.druid.java.util.common.Pair;
import org.apache.druid.query.aggregation.Aggregator;
import org.apache.druid.query.aggregation.AggregatorFactory;
import org.apache.druid.query.aggregation.BufferAggregator;
import org.apache.druid.query.aggregation.TestLongColumnSelector;
import org.apache.druid.query.aggregation.TestObjectColumnSelector;
import org.apache.druid.segment.ColumnSelectorFactory;
import org.junit.Assert;
import org.junit.Test;


public class LongFirstAggregationTest {
    private LongFirstAggregatorFactory longFirstAggFactory;

    private LongFirstAggregatorFactory combiningAggFactory;

    private ColumnSelectorFactory colSelectorFactory;

    private TestLongColumnSelector timeSelector;

    private TestLongColumnSelector valueSelector;

    private TestObjectColumnSelector objectSelector;

    private long[] longValues = new long[]{ 185, -216, -128751132, Long.MIN_VALUE };

    private long[] times = new long[]{ 1123126751, 1784247991, 1854329816, 1000000000 };

    private SerializablePair[] pairs = new SerializablePair[]{ new SerializablePair(1L, 113267L), new SerializablePair(1L, 5437384L), new SerializablePair(6L, 34583458L), new SerializablePair(88L, 34583452L) };

    @Test
    public void testLongFirstAggregator() {
        Aggregator agg = longFirstAggFactory.factorize(colSelectorFactory);
        aggregate(agg);
        aggregate(agg);
        aggregate(agg);
        aggregate(agg);
        Pair<Long, Long> result = ((Pair<Long, Long>) (agg.get()));
        Assert.assertEquals(times[3], result.lhs.longValue());
        Assert.assertEquals(longValues[3], result.rhs.longValue());
        Assert.assertEquals(longValues[3], agg.getLong());
        Assert.assertEquals(longValues[3], agg.getFloat(), 1.0E-4);
    }

    @Test
    public void testLongFirstBufferAggregator() {
        BufferAggregator agg = longFirstAggFactory.factorizeBuffered(colSelectorFactory);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[longFirstAggFactory.getMaxIntermediateSizeWithNulls()]);
        agg.init(buffer, 0);
        aggregate(agg, buffer, 0);
        aggregate(agg, buffer, 0);
        aggregate(agg, buffer, 0);
        aggregate(agg, buffer, 0);
        Pair<Long, Long> result = ((Pair<Long, Long>) (agg.get(buffer, 0)));
        Assert.assertEquals(times[3], result.lhs.longValue());
        Assert.assertEquals(longValues[3], result.rhs.longValue());
        Assert.assertEquals(longValues[3], agg.getLong(buffer, 0));
        Assert.assertEquals(longValues[3], agg.getFloat(buffer, 0), 1.0E-4);
    }

    @Test
    public void testCombine() {
        SerializablePair pair1 = new SerializablePair(1467225000L, 1263L);
        SerializablePair pair2 = new SerializablePair(1467240000L, 752713L);
        Assert.assertEquals(pair1, longFirstAggFactory.combine(pair1, pair2));
    }

    @Test
    public void testLongFirstCombiningAggregator() {
        Aggregator agg = combiningAggFactory.factorize(colSelectorFactory);
        aggregate(agg);
        aggregate(agg);
        aggregate(agg);
        aggregate(agg);
        Pair<Long, Long> result = ((Pair<Long, Long>) (agg.get()));
        Pair<Long, Long> expected = ((Pair<Long, Long>) (pairs[0]));
        Assert.assertEquals(expected.lhs, result.lhs);
        Assert.assertEquals(expected.rhs, result.rhs);
        Assert.assertEquals(expected.rhs.longValue(), agg.getLong());
        Assert.assertEquals(expected.rhs, agg.getFloat(), 1.0E-4);
    }

    @Test
    public void testLongFirstCombiningBufferAggregator() {
        BufferAggregator agg = combiningAggFactory.factorizeBuffered(colSelectorFactory);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[longFirstAggFactory.getMaxIntermediateSizeWithNulls()]);
        agg.init(buffer, 0);
        aggregate(agg, buffer, 0);
        aggregate(agg, buffer, 0);
        aggregate(agg, buffer, 0);
        aggregate(agg, buffer, 0);
        Pair<Long, Long> result = ((Pair<Long, Long>) (agg.get(buffer, 0)));
        Pair<Long, Long> expected = ((Pair<Long, Long>) (pairs[0]));
        Assert.assertEquals(expected.lhs, result.lhs);
        Assert.assertEquals(expected.rhs, result.rhs);
        Assert.assertEquals(expected.rhs.longValue(), agg.getLong(buffer, 0));
        Assert.assertEquals(expected.rhs, agg.getFloat(buffer, 0), 1.0E-4);
    }

    @Test
    public void testSerde() throws Exception {
        DefaultObjectMapper mapper = new DefaultObjectMapper();
        String longSpecJson = "{\"type\":\"longFirst\",\"name\":\"billy\",\"fieldName\":\"nilly\"}";
        Assert.assertEquals(longFirstAggFactory, mapper.readValue(longSpecJson, AggregatorFactory.class));
    }
}
