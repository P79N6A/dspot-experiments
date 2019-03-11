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
package org.apache.beam.runners.dataflow.worker;


import BatchModeExecutionContext.DATASTORE_THROTTLE_TIME_NAMESPACE;
import ExecutionStateTracker.PROCESS_STATE_NAME;
import ExecutionStateTracker.START_STATE_NAME;
import NoopProfileScope.NOOP;
import com.google.api.services.dataflow.model.CounterMetadata;
import com.google.api.services.dataflow.model.CounterStructuredName;
import com.google.api.services.dataflow.model.CounterStructuredNameAndMetadata;
import com.google.api.services.dataflow.model.CounterUpdate;
import com.google.api.services.dataflow.model.DistributionUpdate;
import org.apache.beam.runners.core.metrics.ExecutionStateSampler;
import org.apache.beam.runners.core.metrics.ExecutionStateTracker;
import org.apache.beam.runners.core.metrics.ExecutionStateTracker.ExecutionState;
import org.apache.beam.runners.dataflow.worker.BatchModeExecutionContext.BatchModeExecutionState;
import org.apache.beam.runners.dataflow.worker.counters.DataflowCounterUpdateExtractor;
import org.apache.beam.runners.dataflow.worker.counters.NameContext;
import org.apache.beam.runners.dataflow.worker.profiler.ScopedProfiler.NoopProfileScope;
import org.apache.beam.runners.dataflow.worker.profiler.ScopedProfiler.ProfileScope;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.MetricName;
import org.apache.beam.sdk.metrics.MetricsContainer;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;


/**
 * Tests for {@link BatchModeExecutionContext}.
 */
@RunWith(JUnit4.class)
public class BatchModeExecutionContextTest {
    @Test
    public void extractMetricUpdatesCounter() {
        BatchModeExecutionContext executionContext = BatchModeExecutionContext.forTesting(PipelineOptionsFactory.create(), "testStage");
        DataflowOperationContext operationContext = executionContext.createOperationContext(NameContextsForTests.nameContextForTest());
        Counter counter = operationContext.metricsContainer().getCounter(MetricName.named("namespace", "some-counter"));
        counter.inc(1);
        counter.inc(41);
        counter.inc(1);
        counter.inc((-1));
        final CounterUpdate expected = new CounterUpdate().setStructuredNameAndMetadata(new CounterStructuredNameAndMetadata().setName(new CounterStructuredName().setOrigin("USER").setOriginNamespace("namespace").setName("some-counter").setOriginalStepName("originalName")).setMetadata(new CounterMetadata().setKind("SUM"))).setCumulative(true).setInteger(DataflowCounterUpdateExtractor.longToSplitInt(42));
        Assert.assertThat(executionContext.extractMetricUpdates(false), Matchers.containsInAnyOrder(expected));
        executionContext.commitMetricUpdates();
        Counter counterUncommitted = operationContext.metricsContainer().getCounter(MetricName.named("namespace", "uncommitted-counter"));
        counterUncommitted.inc(64);
        final CounterUpdate expectedUncommitted = new CounterUpdate().setStructuredNameAndMetadata(new CounterStructuredNameAndMetadata().setName(new CounterStructuredName().setOrigin("USER").setOriginNamespace("namespace").setName("uncommitted-counter").setOriginalStepName("originalName")).setMetadata(new CounterMetadata().setKind("SUM"))).setCumulative(true).setInteger(DataflowCounterUpdateExtractor.longToSplitInt(64));
        // Expect to get only the uncommitted metric, unless final update.
        Assert.assertThat(executionContext.extractMetricUpdates(false), Matchers.containsInAnyOrder(expectedUncommitted));
        Assert.assertThat(executionContext.extractMetricUpdates(true), Matchers.containsInAnyOrder(expected, expectedUncommitted));
        executionContext.commitMetricUpdates();
        // All Metrics are committed, expect none unless final update.
        Assert.assertThat(executionContext.extractMetricUpdates(false), Matchers.emptyIterable());
        Assert.assertThat(executionContext.extractMetricUpdates(true), Matchers.containsInAnyOrder(expected, expectedUncommitted));
    }

    @Test
    public void extractMetricUpdatesDistribution() {
        BatchModeExecutionContext executionContext = BatchModeExecutionContext.forTesting(PipelineOptionsFactory.create(), "testStage");
        DataflowOperationContext operationContext = executionContext.createOperationContext(NameContextsForTests.nameContextForTest());
        Distribution distribution = operationContext.metricsContainer().getDistribution(MetricName.named("namespace", "some-distribution"));
        distribution.update(2);
        distribution.update(8);
        final CounterUpdate expected = new CounterUpdate().setStructuredNameAndMetadata(new CounterStructuredNameAndMetadata().setName(new CounterStructuredName().setOrigin("USER").setOriginNamespace("namespace").setName("some-distribution").setOriginalStepName("originalName")).setMetadata(new CounterMetadata().setKind("DISTRIBUTION"))).setCumulative(true).setDistribution(new DistributionUpdate().setCount(DataflowCounterUpdateExtractor.longToSplitInt(2)).setMax(DataflowCounterUpdateExtractor.longToSplitInt(8)).setMin(DataflowCounterUpdateExtractor.longToSplitInt(2)).setSum(DataflowCounterUpdateExtractor.longToSplitInt(10)));
        Assert.assertThat(executionContext.extractMetricUpdates(false), Matchers.containsInAnyOrder(expected));
    }

    @Test
    public void extractMsecCounters() {
        BatchModeExecutionContext executionContext = BatchModeExecutionContext.forTesting(PipelineOptionsFactory.create(), "testStage");
        MetricsContainer metricsContainer = Mockito.mock(MetricsContainer.class);
        ProfileScope otherScope = Mockito.mock(ProfileScope.class);
        ProfileScope profileScope = Mockito.mock(ProfileScope.class);
        ExecutionState start1 = executionContext.executionStateRegistry.getState(NameContext.create("stage", "original-1", "system-1", "user-1"), START_STATE_NAME, metricsContainer, profileScope);
        ExecutionState process1 = executionContext.executionStateRegistry.getState(NameContext.create("stage", "original-1", "system-1", "user-1"), PROCESS_STATE_NAME, metricsContainer, profileScope);
        ExecutionState start2 = executionContext.executionStateRegistry.getState(NameContext.create("stage", "original-2", "system-2", "user-2"), START_STATE_NAME, metricsContainer, profileScope);
        ExecutionState other = executionContext.executionStateRegistry.getState(NameContext.forStage("stage"), "other", null, NOOP);
        other.takeSample(120);
        start1.takeSample(100);
        process1.takeSample(500);
        Assert.assertThat(executionContext.extractMsecCounters(false), Matchers.containsInAnyOrder(msecStage("other-msecs", "stage", 120), msec("start-msecs", "stage", "original-1", 100), msec("process-msecs", "stage", "original-1", 500)));
        process1.takeSample(200);
        start2.takeSample(200);
        Assert.assertThat(executionContext.extractMsecCounters(false), Matchers.containsInAnyOrder(msec("process-msecs", "stage", "original-1", (500 + 200)), msec("start-msecs", "stage", "original-2", 200)));
        process1.takeSample(300);
        Assert.assertThat(executionContext.extractMsecCounters(true), Matchers.hasItems(msecStage("other-msecs", "stage", 120), msec("start-msecs", "stage", "original-1", 100), msec("process-msecs", "stage", "original-1", ((500 + 200) + 300)), msec("start-msecs", "stage", "original-2", 200)));
    }

    @Test
    public void extractThrottleTimeCounters() {
        BatchModeExecutionContext executionContext = BatchModeExecutionContext.forTesting(PipelineOptionsFactory.create(), "testStage");
        DataflowOperationContext operationContext = executionContext.createOperationContext(NameContextsForTests.nameContextForTest());
        Counter counter = operationContext.metricsContainer().getCounter(MetricName.named(DATASTORE_THROTTLE_TIME_NAMESPACE, "cumulativeThrottlingSeconds"));
        counter.inc(12);
        counter.inc(17);
        counter.inc(1);
        Assert.assertEquals(30L, ((long) (executionContext.extractThrottleTime())));
    }

    @Test(timeout = 2000)
    public void stateSamplingInBatch() {
        // Test that when writing on one thread and reading from another, updates always eventually
        // reach the reading thread.
        BatchModeExecutionState state = /* requestingStepName */
        /* inputIndex */
        /* metricsContainer */
        new BatchModeExecutionState(NameContextsForTests.nameContextForTest(), "testState", null, null, null, NoopProfileScope.NOOP);
        ExecutionStateSampler sampler = ExecutionStateSampler.newForTest();
        try {
            sampler.start();
            ExecutionStateTracker tracker = new ExecutionStateTracker(sampler);
            Thread executionThread = new Thread();
            executionThread.setName("looping-thread-for-test");
            tracker.activate(executionThread);
            tracker.enterState(state);
            // Wait for the state to be incremented 3 times
            long value = 0;
            for (int i = 0; i < 3; i++) {
                CounterUpdate update = null;
                while (update == null) {
                    update = state.extractUpdate(false);
                } 
                long newValue = DataflowCounterUpdateExtractor.splitIntToLong(update.getInteger());
                Assert.assertThat(newValue, Matchers.greaterThan(value));
                value = newValue;
            }
        } finally {
            sampler.stop();
        }
    }
}
