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
package org.apache.beam.runners.core.metrics;


import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.metrics.MetricsOptions;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.UsesAttemptedMetrics;
import org.apache.beam.sdk.testing.UsesCounterMetrics;
import org.apache.beam.sdk.testing.UsesMetricsPusher;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.hamcrest.Matchers;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A test that verifies that metrics push system works.
 */
@Category({ UsesMetricsPusher.class })
@RunWith(JUnit4.class)
public class MetricsPusherTest {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsPusherTest.class);

    private static final long NUM_ELEMENTS = 1000L;

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    @Category({ ValidatesRunner.class, UsesAttemptedMetrics.class, UsesCounterMetrics.class })
    @Test
    public void test() throws Exception {
        // Use maxReadTime to force unbounded mode.
        pipeline.apply(GenerateSequence.from(0).to(MetricsPusherTest.NUM_ELEMENTS).withMaxReadTime(Duration.standardDays(1))).apply(ParDo.of(new MetricsPusherTest.CountingDoFn()));
        pipeline.run();
        // give metrics pusher time to push
        Thread.sleep((((pipeline.getOptions().as(MetricsOptions.class).getMetricsPushPeriod()) + 1L) * 1000));
        Assert.assertThat(TestMetricsSink.getCounterValue(), Matchers.is(MetricsPusherTest.NUM_ELEMENTS));
    }

    private static class CountingDoFn extends DoFn<Long, Long> {
        private final Counter counter = Metrics.counter(MetricsPusherTest.class, "counter");

        @ProcessElement
        public void processElement(ProcessContext context) {
            try {
                counter.inc();
                context.output(context.element());
            } catch (Exception e) {
                MetricsPusherTest.LOG.error(e.getMessage(), e);
            }
        }
    }
}
