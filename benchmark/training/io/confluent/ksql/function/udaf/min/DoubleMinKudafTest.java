/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.confluent.ksql.function.udaf.min;


import org.apache.kafka.streams.kstream.Merger;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;


public class DoubleMinKudafTest {
    @Test
    public void shouldFindCorrectMin() {
        final DoubleMinKudaf doubleMinKudaf = getDoubleMinKudaf();
        final double[] values = new double[]{ 3.0, 5.0, 8.0, 2.2, 3.5, 4.6, 5.0 };
        double currentMin = Double.MAX_VALUE;
        for (final double i : values) {
            currentMin = doubleMinKudaf.aggregate(i, currentMin);
        }
        Assert.assertThat(2.2, CoreMatchers.equalTo(currentMin));
    }

    @Test
    public void shouldHandleNull() {
        final DoubleMinKudaf doubleMinKudaf = getDoubleMinKudaf();
        final double[] values = new double[]{ 3.0, 5.0, 8.0, 2.2, 3.5, 4.6, 5.0 };
        double currentMin = Double.MAX_VALUE;
        // null before any aggregation
        currentMin = doubleMinKudaf.aggregate(null, currentMin);
        Assert.assertThat(Double.MAX_VALUE, CoreMatchers.equalTo(currentMin));
        // now send each value to aggregation and verify
        for (final double i : values) {
            currentMin = doubleMinKudaf.aggregate(i, currentMin);
        }
        Assert.assertThat(2.2, CoreMatchers.equalTo(currentMin));
        // null should not impact result
        currentMin = doubleMinKudaf.aggregate(null, currentMin);
        Assert.assertThat(2.2, CoreMatchers.equalTo(currentMin));
    }

    @Test
    public void shouldFindCorrectMinForMerge() {
        final DoubleMinKudaf doubleMinKudaf = getDoubleMinKudaf();
        final Merger<String, Double> merger = doubleMinKudaf.getMerger();
        final Double mergeResult1 = merger.apply("Key", 10.0, 12.0);
        Assert.assertThat(mergeResult1, CoreMatchers.equalTo(10.0));
        final Double mergeResult2 = merger.apply("Key", 10.0, (-12.0));
        Assert.assertThat(mergeResult2, CoreMatchers.equalTo((-12.0)));
        final Double mergeResult3 = merger.apply("Key", (-10.0), 0.0);
        Assert.assertThat(mergeResult3, CoreMatchers.equalTo((-10.0)));
    }
}
