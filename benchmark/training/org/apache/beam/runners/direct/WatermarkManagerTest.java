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
package org.apache.beam.runners.direct;


import BoundedWindow.TIMESTAMP_MAX_VALUE;
import BoundedWindow.TIMESTAMP_MIN_VALUE;
import TimeDomain.EVENT_TIME;
import TimeDomain.PROCESSING_TIME;
import TimeDomain.SYNCHRONIZED_PROCESSING_TIME;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.beam.runners.core.StateNamespaces;
import org.apache.beam.runners.core.TimerInternals.TimerData;
import org.apache.beam.runners.direct.WatermarkManager.AppliedPTransformInputWatermark;
import org.apache.beam.runners.direct.WatermarkManager.FiredTimers;
import org.apache.beam.runners.direct.WatermarkManager.TimerUpdate;
import org.apache.beam.runners.direct.WatermarkManager.TimerUpdate.TimerUpdateBuilder;
import org.apache.beam.runners.direct.WatermarkManager.TransformWatermarks;
import org.apache.beam.runners.direct.WatermarkManager.Watermark;
import org.apache.beam.runners.local.StructuralKey;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarLongCoder;
import org.apache.beam.sdk.runners.AppliedPTransform;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.util.WindowedValue;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TimestampedValue;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableList;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableMap;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;


/**
 * Tests for {@link WatermarkManager}.
 */
@RunWith(JUnit4.class)
public class WatermarkManagerTest implements Serializable {
    @Rule
    public transient ExpectedException thrown = ExpectedException.none();

    private transient MockClock clock;

    private transient PCollection<Integer> createdInts;

    private transient PCollection<Integer> filtered;

    private transient PCollection<Integer> filteredTimesTwo;

    private transient PCollection<KV<String, Integer>> keyed;

    private transient PCollection<Integer> intsToFlatten;

    private transient PCollection<Integer> flattened;

    private transient WatermarkManager<AppliedPTransform<?, ?, ?>, ? super PCollection<?>> manager;

    private transient BundleFactory bundleFactory;

    private DirectGraph graph;

    @Rule
    public transient TestPipeline p = TestPipeline.create().enableAbandonedNodeEnforcement(false);

    /**
     * Demonstrates that getWatermark, when called on an {@link AppliedPTransform} that has not
     * processed any elements, returns the {@link BoundedWindow#TIMESTAMP_MIN_VALUE}.
     */
    @Test
    public void getWatermarkForUntouchedTransform() {
        TransformWatermarks watermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(watermarks.getInputWatermark(), Matchers.equalTo(TIMESTAMP_MIN_VALUE));
        Assert.assertThat(watermarks.getOutputWatermark(), Matchers.equalTo(TIMESTAMP_MIN_VALUE));
    }

    /**
     * Demonstrates that getWatermark for a transform that consumes no input uses the Watermark Hold
     * value provided to it as the output watermark.
     */
    @Test
    public void getWatermarkForUpdatedSourceTransform() {
        CommittedBundle<Integer> output = multiWindowedBundle(createdInts, 1);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.singleton(output), new Instant(8000L));
        manager.refreshAll();
        TransformWatermarks updatedSourceWatermark = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(updatedSourceWatermark.getOutputWatermark(), Matchers.equalTo(new Instant(8000L)));
    }

    /**
     * Demonstrates that getWatermark for a transform that takes multiple inputs is held to the
     * minimum watermark across all of its inputs.
     */
    @Test
    public void getWatermarkForMultiInputTransform() {
        CommittedBundle<Integer> secondPcollectionBundle = multiWindowedBundle(intsToFlatten, (-1));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(intsToFlatten), null, Collections.<CommittedBundle<?>>singleton(secondPcollectionBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        // We didn't do anything for the first source, so we shouldn't have progressed the watermark
        TransformWatermarks firstSourceWatermark = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(firstSourceWatermark.getOutputWatermark(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        // the Second Source output all of the elements so it should be done (with a watermark at the
        // end of time).
        TransformWatermarks secondSourceWatermark = manager.getWatermarks(graph.getProducer(intsToFlatten));
        Assert.assertThat(secondSourceWatermark.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        // We haven't consumed anything yet, so our watermark should be at the beginning of time
        TransformWatermarks transformWatermark = manager.getWatermarks(graph.getProducer(flattened));
        Assert.assertThat(transformWatermark.getInputWatermark(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        Assert.assertThat(transformWatermark.getOutputWatermark(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        CommittedBundle<Integer> flattenedBundleSecondCreate = multiWindowedBundle(flattened, (-1));
        // We have finished processing the bundle from the second PCollection, but we haven't consumed
        // anything from the first PCollection yet; so our watermark shouldn't advance
        manager.updateWatermarks(secondPcollectionBundle, TimerUpdate.empty(), graph.getProducer(flattened), secondPcollectionBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(flattenedBundleSecondCreate), TIMESTAMP_MAX_VALUE);
        TransformWatermarks transformAfterProcessing = manager.getWatermarks(graph.getProducer(flattened));
        manager.updateWatermarks(secondPcollectionBundle, TimerUpdate.empty(), graph.getProducer(flattened), secondPcollectionBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(flattenedBundleSecondCreate), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        Assert.assertThat(transformAfterProcessing.getInputWatermark(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        Assert.assertThat(transformAfterProcessing.getOutputWatermark(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        Instant firstCollectionTimestamp = new Instant(10000);
        CommittedBundle<Integer> firstPcollectionBundle = timestampedBundle(createdInts, TimestampedValue.of(5, firstCollectionTimestamp));
        // the source is done, but elements are still buffered. The source output watermark should be
        // past the end of the global window
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(firstPcollectionBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks firstSourceWatermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(firstSourceWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        // We still haven't consumed any of the first source's input, so the watermark should still not
        // progress
        TransformWatermarks flattenAfterSourcesProduced = manager.getWatermarks(graph.getProducer(flattened));
        Assert.assertThat(flattenAfterSourcesProduced.getInputWatermark(), Matchers.not(Matchers.greaterThan(firstCollectionTimestamp)));
        Assert.assertThat(flattenAfterSourcesProduced.getOutputWatermark(), Matchers.not(Matchers.greaterThan(firstCollectionTimestamp)));
        // We have buffered inputs, but since the PCollection has all of the elements (has a WM past the
        // end of the global window), we should have a watermark equal to the min among buffered
        // elements
        TransformWatermarks withBufferedElements = manager.getWatermarks(graph.getProducer(flattened));
        Assert.assertThat(withBufferedElements.getInputWatermark(), Matchers.equalTo(firstCollectionTimestamp));
        Assert.assertThat(withBufferedElements.getOutputWatermark(), Matchers.equalTo(firstCollectionTimestamp));
        CommittedBundle<?> completedFlattenBundle = bundleFactory.createBundle(flattened).commit(TIMESTAMP_MAX_VALUE);
        manager.updateWatermarks(firstPcollectionBundle, TimerUpdate.empty(), graph.getProducer(flattened), firstPcollectionBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(completedFlattenBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks afterConsumingAllInput = manager.getWatermarks(graph.getProducer(flattened));
        Assert.assertThat(afterConsumingAllInput.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(afterConsumingAllInput.getOutputWatermark(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MAX_VALUE)));
    }

    /**
     * Demonstrates that getWatermark for a transform that takes multiple inputs is held to the
     * minimum watermark across all of its inputs.
     */
    @Test
    public void getWatermarkMultiIdenticalInput() {
        PCollection<Integer> created = p.apply(Create.of(1, 2, 3));
        PCollection<Integer> multiConsumer = PCollectionList.of(created).and(created).apply(Flatten.pCollections());
        DirectGraphVisitor graphVisitor = new DirectGraphVisitor();
        p.traverseTopologically(graphVisitor);
        DirectGraph graph = graphVisitor.getGraph();
        AppliedPTransform<?, ?, ?> theFlatten = graph.getProducer(multiConsumer);
        WatermarkManager<AppliedPTransform<?, ?, ?>, ? super PCollection<?>> tstMgr = WatermarkManager.create(clock, graph, AppliedPTransform::getFullName);
        CommittedBundle<Void> root = bundleFactory.<Void>createRootBundle().add(WindowedValue.valueInGlobalWindow(null)).commit(clock.now());
        CommittedBundle<Integer> createBundle = bundleFactory.createBundle(created).add(WindowedValue.timestampedValueInGlobalWindow(1, new Instant(33536))).commit(clock.now());
        Map<AppliedPTransform<?, ?, ?>, Collection<CommittedBundle<?>>> initialInputs = ImmutableMap.<AppliedPTransform<?, ?, ?>, Collection<CommittedBundle<?>>>builder().put(graph.getProducer(created), Collections.singleton(root)).build();
        tstMgr.initialize(((Map) (initialInputs)));
        tstMgr.updateWatermarks(root, TimerUpdate.empty(), graph.getProducer(created), null, Collections.singleton(createBundle), TIMESTAMP_MAX_VALUE);
        tstMgr.refreshAll();
        TransformWatermarks flattenWms = tstMgr.getWatermarks(theFlatten);
        Assert.assertThat(flattenWms.getInputWatermark(), Matchers.equalTo(new Instant(33536)));
        tstMgr.updateWatermarks(createBundle, TimerUpdate.empty(), theFlatten, null, Collections.emptyList(), TIMESTAMP_MAX_VALUE);
        tstMgr.refreshAll();
        Assert.assertThat(flattenWms.getInputWatermark(), Matchers.equalTo(new Instant(33536)));
        tstMgr.updateWatermarks(createBundle, TimerUpdate.empty(), theFlatten, null, Collections.emptyList(), TIMESTAMP_MAX_VALUE);
        tstMgr.refreshAll();
        Assert.assertThat(flattenWms.getInputWatermark(), Matchers.equalTo(TIMESTAMP_MAX_VALUE));
    }

    /**
     * Demonstrates that pending elements are independent among {@link AppliedPTransform
     * AppliedPTransforms} that consume the same input {@link PCollection}.
     */
    @Test
    public void getWatermarkForMultiConsumedCollection() {
        CommittedBundle<Integer> createdBundle = timestampedBundle(createdInts, TimestampedValue.of(1, new Instant(1000000L)), TimestampedValue.of(2, new Instant(1234L)), TimestampedValue.of(3, new Instant((-1000L))));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createdBundle), new Instant(Long.MAX_VALUE));
        manager.refreshAll();
        TransformWatermarks createdAfterProducing = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(createdAfterProducing.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        CommittedBundle<KV<String, Integer>> keyBundle = timestampedBundle(keyed, TimestampedValue.of(KV.of("MyKey", 1), new Instant(1000000L)), TimestampedValue.of(KV.of("MyKey", 2), new Instant(1234L)), TimestampedValue.of(KV.of("MyKey", 3), new Instant((-1000L))));
        manager.updateWatermarks(createdBundle, TimerUpdate.empty(), graph.getProducer(keyed), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(keyBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks keyedWatermarks = manager.getWatermarks(graph.getProducer(keyed));
        Assert.assertThat(keyedWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(keyedWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        TransformWatermarks filteredWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(filteredWatermarks.getInputWatermark(), Matchers.not(Matchers.greaterThan(new Instant((-1000L)))));
        Assert.assertThat(filteredWatermarks.getOutputWatermark(), Matchers.not(Matchers.greaterThan(new Instant((-1000L)))));
        CommittedBundle<Integer> filteredBundle = timestampedBundle(filtered, TimestampedValue.of(2, new Instant(1234L)));
        manager.updateWatermarks(createdBundle, TimerUpdate.empty(), graph.getProducer(filtered), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(filteredBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks filteredProcessedWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(filteredProcessedWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(filteredProcessedWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
    }

    /**
     * Demonstrates that the watermark of an {@link AppliedPTransform} is held to the provided
     * watermark hold.
     */
    @Test
    public void updateWatermarkWithWatermarkHolds() {
        CommittedBundle<Integer> createdBundle = timestampedBundle(createdInts, TimestampedValue.of(1, new Instant(1000000L)), TimestampedValue.of(2, new Instant(1234L)), TimestampedValue.of(3, new Instant((-1000L))));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createdBundle), new Instant(Long.MAX_VALUE));
        CommittedBundle<KV<String, Integer>> keyBundle = timestampedBundle(keyed, TimestampedValue.of(KV.of("MyKey", 1), new Instant(1000000L)), TimestampedValue.of(KV.of("MyKey", 2), new Instant(1234L)), TimestampedValue.of(KV.of("MyKey", 3), new Instant((-1000L))));
        manager.updateWatermarks(createdBundle, TimerUpdate.empty(), graph.getProducer(keyed), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(keyBundle), new Instant(500L));
        manager.refreshAll();
        TransformWatermarks keyedWatermarks = manager.getWatermarks(graph.getProducer(keyed));
        Assert.assertThat(keyedWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(keyedWatermarks.getOutputWatermark(), Matchers.not(Matchers.greaterThan(new Instant(500L))));
    }

    /**
     * Demonstrates that the watermark of an {@link AppliedPTransform} is held to the provided
     * watermark hold.
     */
    @Test
    public void updateWatermarkWithKeyedWatermarkHolds() {
        CommittedBundle<Integer> firstKeyBundle = bundleFactory.createKeyedBundle(StructuralKey.of("Odd", StringUtf8Coder.of()), createdInts).add(WindowedValue.timestampedValueInGlobalWindow(1, new Instant(1000000L))).add(WindowedValue.timestampedValueInGlobalWindow(3, new Instant((-1000L)))).commit(clock.now());
        CommittedBundle<Integer> secondKeyBundle = bundleFactory.createKeyedBundle(StructuralKey.of("Even", StringUtf8Coder.of()), createdInts).add(WindowedValue.timestampedValueInGlobalWindow(2, new Instant(1234L))).commit(clock.now());
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, ImmutableList.of(firstKeyBundle, secondKeyBundle), TIMESTAMP_MAX_VALUE);
        manager.updateWatermarks(firstKeyBundle, TimerUpdate.empty(), graph.getProducer(filtered), firstKeyBundle.withElements(Collections.emptyList()), Collections.emptyList(), new Instant((-1000L)));
        manager.updateWatermarks(secondKeyBundle, TimerUpdate.empty(), graph.getProducer(filtered), secondKeyBundle.withElements(Collections.emptyList()), Collections.emptyList(), new Instant(1234L));
        manager.refreshAll();
        TransformWatermarks filteredWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(filteredWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(filteredWatermarks.getOutputWatermark(), Matchers.not(Matchers.greaterThan(new Instant((-1000L)))));
        CommittedBundle<Integer> fauxFirstKeyTimerBundle = bundleFactory.createKeyedBundle(StructuralKey.of("Odd", StringUtf8Coder.of()), createdInts).commit(clock.now());
        manager.updateWatermarks(fauxFirstKeyTimerBundle, TimerUpdate.empty(), graph.getProducer(filtered), fauxFirstKeyTimerBundle.withElements(Collections.emptyList()), Collections.emptyList(), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        Assert.assertThat(filteredWatermarks.getOutputWatermark(), Matchers.equalTo(new Instant(1234L)));
        CommittedBundle<Integer> fauxSecondKeyTimerBundle = bundleFactory.createKeyedBundle(StructuralKey.of("Even", StringUtf8Coder.of()), createdInts).commit(clock.now());
        manager.updateWatermarks(fauxSecondKeyTimerBundle, TimerUpdate.empty(), graph.getProducer(filtered), fauxSecondKeyTimerBundle.withElements(Collections.emptyList()), Collections.emptyList(), new Instant(5678L));
        manager.refreshAll();
        Assert.assertThat(filteredWatermarks.getOutputWatermark(), Matchers.equalTo(new Instant(5678L)));
        manager.updateWatermarks(fauxSecondKeyTimerBundle, TimerUpdate.empty(), graph.getProducer(filtered), fauxSecondKeyTimerBundle.withElements(Collections.emptyList()), Collections.emptyList(), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        Assert.assertThat(filteredWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
    }

    /**
     * Demonstrates that updated output watermarks are monotonic in the presence of late data, when
     * called on an {@link AppliedPTransform} that consumes no input.
     */
    @Test
    public void updateOutputWatermarkShouldBeMonotonic() {
        CommittedBundle<?> firstInput = bundleFactory.createBundle(createdInts).commit(TIMESTAMP_MAX_VALUE);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(firstInput), new Instant(0L));
        manager.refreshAll();
        TransformWatermarks firstWatermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(firstWatermarks.getOutputWatermark(), Matchers.equalTo(new Instant(0L)));
        CommittedBundle<?> secondInput = bundleFactory.createBundle(createdInts).commit(TIMESTAMP_MAX_VALUE);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(secondInput), new Instant((-250L)));
        manager.refreshAll();
        TransformWatermarks secondWatermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(secondWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(new Instant(0L))));
    }

    /**
     * Demonstrates that updated output watermarks are monotonic in the presence of watermark holds
     * that become earlier than a previous watermark hold.
     */
    @Test
    public void updateWatermarkWithHoldsShouldBeMonotonic() {
        CommittedBundle<Integer> createdBundle = timestampedBundle(createdInts, TimestampedValue.of(1, new Instant(1000000L)), TimestampedValue.of(2, new Instant(1234L)), TimestampedValue.of(3, new Instant((-1000L))));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createdBundle), new Instant(Long.MAX_VALUE));
        CommittedBundle<KV<String, Integer>> keyBundle = timestampedBundle(keyed, TimestampedValue.of(KV.of("MyKey", 1), new Instant(1000000L)), TimestampedValue.of(KV.of("MyKey", 2), new Instant(1234L)), TimestampedValue.of(KV.of("MyKey", 3), new Instant((-1000L))));
        manager.updateWatermarks(createdBundle, TimerUpdate.empty(), graph.getProducer(keyed), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(keyBundle), new Instant(500L));
        manager.refreshAll();
        TransformWatermarks keyedWatermarks = manager.getWatermarks(graph.getProducer(keyed));
        Assert.assertThat(keyedWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(keyedWatermarks.getOutputWatermark(), Matchers.not(Matchers.greaterThan(new Instant(500L))));
        Instant oldOutputWatermark = keyedWatermarks.getOutputWatermark();
        TransformWatermarks updatedWatermarks = manager.getWatermarks(graph.getProducer(keyed));
        Assert.assertThat(updatedWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        // We added a hold prior to the old watermark; we shouldn't progress (due to the earlier hold)
        // but the watermark is monotonic and should not backslide to the new, earlier hold
        Assert.assertThat(updatedWatermarks.getOutputWatermark(), Matchers.equalTo(oldOutputWatermark));
    }

    @Test
    public void updateWatermarkWithUnprocessedElements() {
        WindowedValue<Integer> first = WindowedValue.valueInGlobalWindow(1);
        WindowedValue<Integer> second = WindowedValue.timestampedValueInGlobalWindow(2, new Instant((-1000L)));
        WindowedValue<Integer> third = WindowedValue.timestampedValueInGlobalWindow(3, new Instant(1234L));
        CommittedBundle<Integer> createdBundle = bundleFactory.createBundle(createdInts).add(first).add(second).add(third).commit(clock.now());
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createdBundle), TIMESTAMP_MAX_VALUE);
        CommittedBundle<KV<String, Integer>> keyBundle = timestampedBundle(keyed, TimestampedValue.of(KV.of("MyKey", 1), TIMESTAMP_MIN_VALUE));
        manager.updateWatermarks(createdBundle, TimerUpdate.empty(), graph.getProducer(keyed), createdBundle.withElements(ImmutableList.of(second, third)), Collections.<CommittedBundle<?>>singleton(keyBundle), TIMESTAMP_MAX_VALUE);
        TransformWatermarks keyedWatermarks = manager.getWatermarks(graph.getProducer(keyed));
        // the unprocessed second and third are readded to pending
        Assert.assertThat(keyedWatermarks.getInputWatermark(), Matchers.not(Matchers.greaterThan(new Instant((-1000L)))));
    }

    @Test
    public void updateWatermarkWithCompletedElementsNotPending() {
        WindowedValue<Integer> first = WindowedValue.timestampedValueInGlobalWindow(1, new Instant(22));
        CommittedBundle<Integer> createdBundle = bundleFactory.createBundle(createdInts).add(first).commit(clock.now());
        WindowedValue<Integer> second = WindowedValue.timestampedValueInGlobalWindow(2, new Instant(22));
        CommittedBundle<Integer> neverCreatedBundle = bundleFactory.createBundle(createdInts).add(second).commit(clock.now());
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createdBundle), TIMESTAMP_MAX_VALUE);
        manager.updateWatermarks(neverCreatedBundle, TimerUpdate.empty(), graph.getProducer(filtered), neverCreatedBundle.withElements(Collections.emptyList()), Collections.emptyList(), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks filteredWms = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(filteredWms.getInputWatermark(), Matchers.equalTo(new Instant(22L)));
    }

    /**
     * Demonstrates that updateWatermarks in the presence of late data is monotonic.
     */
    @Test
    public void updateWatermarkWithLateData() {
        Instant sourceWatermark = new Instant(1000000L);
        CommittedBundle<Integer> createdBundle = timestampedBundle(createdInts, TimestampedValue.of(1, sourceWatermark), TimestampedValue.of(2, new Instant(1234L)));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createdBundle), sourceWatermark);
        CommittedBundle<KV<String, Integer>> keyBundle = timestampedBundle(keyed, TimestampedValue.of(KV.of("MyKey", 1), sourceWatermark), TimestampedValue.of(KV.of("MyKey", 2), new Instant(1234L)));
        // Finish processing the on-time data. The watermarks should progress to be equal to the source
        manager.updateWatermarks(createdBundle, TimerUpdate.empty(), graph.getProducer(keyed), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(keyBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks onTimeWatermarks = manager.getWatermarks(graph.getProducer(keyed));
        Assert.assertThat(onTimeWatermarks.getInputWatermark(), Matchers.equalTo(sourceWatermark));
        Assert.assertThat(onTimeWatermarks.getOutputWatermark(), Matchers.equalTo(sourceWatermark));
        CommittedBundle<Integer> lateDataBundle = timestampedBundle(createdInts, TimestampedValue.of(3, new Instant((-1000L))));
        // the late data arrives in a downstream PCollection after its watermark has advanced past it;
        // we don't advance the watermark past the current watermark until we've consumed the late data
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(lateDataBundle), new Instant(2000000L));
        manager.refreshAll();
        TransformWatermarks bufferedLateWm = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(bufferedLateWm.getOutputWatermark(), Matchers.equalTo(new Instant(2000000L)));
        // The input watermark should be held to its previous value (not advanced due to late data; not
        // moved backwards in the presence of watermarks due to monotonicity).
        TransformWatermarks lateDataBufferedWatermark = manager.getWatermarks(graph.getProducer(keyed));
        Assert.assertThat(lateDataBufferedWatermark.getInputWatermark(), Matchers.not(Matchers.lessThan(sourceWatermark)));
        Assert.assertThat(lateDataBufferedWatermark.getOutputWatermark(), Matchers.not(Matchers.lessThan(sourceWatermark)));
        CommittedBundle<KV<String, Integer>> lateKeyedBundle = timestampedBundle(keyed, TimestampedValue.of(KV.of("MyKey", 3), new Instant((-1000L))));
        manager.updateWatermarks(lateDataBundle, TimerUpdate.empty(), graph.getProducer(keyed), lateDataBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(lateKeyedBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
    }

    /**
     * Demonstrates that after watermarks of an upstream transform are updated, but no output has been
     * produced, the watermarks of a downstream process are advanced.
     */
    @Test
    public void getWatermarksAfterOnlyEmptyOutput() {
        CommittedBundle<Integer> emptyCreateOutput = multiWindowedBundle(createdInts);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(emptyCreateOutput), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks updatedSourceWatermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(updatedSourceWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        TransformWatermarks finishedFilterWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(finishedFilterWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(finishedFilterWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
    }

    /**
     * Demonstrates that after watermarks of an upstream transform are updated, but no output has been
     * produced, and the downstream transform has a watermark hold, the watermark is held to the hold.
     */
    @Test
    public void getWatermarksAfterHoldAndEmptyOutput() {
        CommittedBundle<Integer> firstCreateOutput = multiWindowedBundle(createdInts, 1, 2);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(firstCreateOutput), new Instant(12000L));
        CommittedBundle<Integer> firstFilterOutput = multiWindowedBundle(filtered);
        manager.updateWatermarks(firstCreateOutput, TimerUpdate.empty(), graph.getProducer(filtered), firstCreateOutput.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(firstFilterOutput), new Instant(10000L));
        manager.refreshAll();
        TransformWatermarks firstFilterWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(firstFilterWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(new Instant(12000L))));
        Assert.assertThat(firstFilterWatermarks.getOutputWatermark(), Matchers.not(Matchers.greaterThan(new Instant(10000L))));
        CommittedBundle<Integer> emptyCreateOutput = multiWindowedBundle(createdInts);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(emptyCreateOutput), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks updatedSourceWatermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(updatedSourceWatermarks.getOutputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        TransformWatermarks finishedFilterWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(finishedFilterWatermarks.getInputWatermark(), Matchers.not(Matchers.lessThan(TIMESTAMP_MAX_VALUE)));
        Assert.assertThat(finishedFilterWatermarks.getOutputWatermark(), Matchers.not(Matchers.greaterThan(new Instant(10000L))));
    }

    @Test
    public void getSynchronizedProcessingTimeInputWatermarksHeldToPendingBundles() {
        TransformWatermarks watermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(watermarks.getSynchronizedProcessingInputTime(), Matchers.equalTo(clock.now()));
        Assert.assertThat(watermarks.getSynchronizedProcessingOutputTime(), Matchers.equalTo(TIMESTAMP_MIN_VALUE));
        TransformWatermarks filteredWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        // Non-root processing watermarks don't progress until data has been processed
        Assert.assertThat(filteredWatermarks.getSynchronizedProcessingInputTime(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        Assert.assertThat(filteredWatermarks.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        CommittedBundle<Integer> createOutput = bundleFactory.createBundle(createdInts).commit(new Instant(1250L));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createOutput), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks createAfterUpdate = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(createAfterUpdate.getSynchronizedProcessingInputTime(), Matchers.equalTo(clock.now()));
        Assert.assertThat(createAfterUpdate.getSynchronizedProcessingOutputTime(), Matchers.equalTo(clock.now()));
        TransformWatermarks filterAfterProduced = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(filterAfterProduced.getSynchronizedProcessingInputTime(), Matchers.not(Matchers.greaterThan(clock.now())));
        Assert.assertThat(filterAfterProduced.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(clock.now())));
        clock.set(new Instant(1500L));
        Assert.assertThat(createAfterUpdate.getSynchronizedProcessingInputTime(), Matchers.equalTo(clock.now()));
        Assert.assertThat(createAfterUpdate.getSynchronizedProcessingOutputTime(), Matchers.equalTo(clock.now()));
        Assert.assertThat(filterAfterProduced.getSynchronizedProcessingInputTime(), Matchers.not(Matchers.greaterThan(new Instant(1250L))));
        Assert.assertThat(filterAfterProduced.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(new Instant(1250L))));
        CommittedBundle<?> filterOutputBundle = bundleFactory.createBundle(intsToFlatten).commit(new Instant(1250L));
        manager.updateWatermarks(createOutput, TimerUpdate.empty(), graph.getProducer(filtered), createOutput.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(filterOutputBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks filterAfterConsumed = manager.getWatermarks(graph.getProducer(filtered));
        Assert.assertThat(filterAfterConsumed.getSynchronizedProcessingInputTime(), Matchers.not(Matchers.greaterThan(createAfterUpdate.getSynchronizedProcessingOutputTime())));
        Assert.assertThat(filterAfterConsumed.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(filterAfterConsumed.getSynchronizedProcessingInputTime())));
    }

    /**
     * Demonstrates that the Synchronized Processing Time output watermark cannot progress past
     * pending timers in the same set. This propagates to all downstream SynchronizedProcessingTimes.
     *
     * <p>Also demonstrate that the result is monotonic.
     */
    @Test
    public void getSynchronizedProcessingTimeOutputHeldToPendingTimers() {
        CommittedBundle<Integer> createdBundle = multiWindowedBundle(createdInts, 1, 2, 4, 8);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createdBundle), new Instant(1248L));
        manager.refreshAll();
        TransformWatermarks filteredWms = manager.getWatermarks(graph.getProducer(filtered));
        TransformWatermarks filteredDoubledWms = manager.getWatermarks(graph.getProducer(filteredTimesTwo));
        Instant initialFilteredWm = filteredWms.getSynchronizedProcessingOutputTime();
        Instant initialFilteredDoubledWm = filteredDoubledWms.getSynchronizedProcessingOutputTime();
        StructuralKey<String> key = StructuralKey.of("key", StringUtf8Coder.of());
        CommittedBundle<Integer> filteredBundle = multiWindowedBundle(filtered, 2, 8);
        TimerData pastTimer = TimerData.of(StateNamespaces.global(), new Instant(250L), PROCESSING_TIME);
        TimerData futureTimer = TimerData.of(StateNamespaces.global(), new Instant(4096L), PROCESSING_TIME);
        TimerUpdate timers = TimerUpdate.builder(key).setTimer(pastTimer).setTimer(futureTimer).build();
        manager.updateWatermarks(createdBundle, timers, graph.getProducer(filtered), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(filteredBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        Instant startTime = clock.now();
        clock.set(startTime.plus(250L));
        // We're held based on the past timer
        Assert.assertThat(filteredWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(startTime)));
        Assert.assertThat(filteredDoubledWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(startTime)));
        // And we're monotonic
        Assert.assertThat(filteredWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.lessThan(initialFilteredWm)));
        Assert.assertThat(filteredDoubledWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.lessThan(initialFilteredDoubledWm)));
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> firedTimers = manager.extractFiredTimers();
        Assert.assertThat(Iterables.getOnlyElement(firedTimers).getTimers(), Matchers.contains(pastTimer));
        // Our timer has fired, but has not been completed, so it holds our synchronized processing WM
        Assert.assertThat(filteredWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(startTime)));
        Assert.assertThat(filteredDoubledWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(startTime)));
        CommittedBundle<Integer> filteredTimerBundle = bundleFactory.createKeyedBundle(key, filtered).commit(TIMESTAMP_MAX_VALUE);
        CommittedBundle<Integer> filteredTimerResult = bundleFactory.createKeyedBundle(key, filteredTimesTwo).commit(filteredWms.getSynchronizedProcessingOutputTime());
        // Complete the processing time timer
        manager.updateWatermarks(filteredTimerBundle, TimerUpdate.builder(key).withCompletedTimers(Collections.singleton(pastTimer)).build(), graph.getProducer(filtered), filteredTimerBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(filteredTimerResult), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        clock.set(startTime.plus(500L));
        Assert.assertThat(filteredWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(clock.now())));
        // filtered should be held to the time at which the filteredTimerResult fired
        Assert.assertThat(filteredDoubledWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.lessThan(filteredTimerResult.getSynchronizedProcessingOutputWatermark())));
        manager.updateWatermarks(filteredTimerResult, TimerUpdate.empty(), graph.getProducer(filteredTimesTwo), filteredTimerResult.withElements(Collections.emptyList()), Collections.emptyList(), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        Assert.assertThat(filteredDoubledWms.getSynchronizedProcessingOutputTime(), Matchers.equalTo(clock.now()));
        clock.set(new Instant(Long.MAX_VALUE));
        Assert.assertThat(filteredWms.getSynchronizedProcessingOutputTime(), Matchers.equalTo(new Instant(4096)));
        Assert.assertThat(filteredDoubledWms.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(new Instant(4096))));
    }

    /**
     * Demonstrates that if any earlier processing holds appear in the synchronized processing time
     * output hold the result is monotonic.
     */
    @Test
    public void getSynchronizedProcessingTimeOutputTimeIsMonotonic() {
        Instant startTime = clock.now();
        TransformWatermarks watermarks = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(watermarks.getSynchronizedProcessingInputTime(), Matchers.equalTo(startTime));
        TransformWatermarks filteredWatermarks = manager.getWatermarks(graph.getProducer(filtered));
        // Non-root processing watermarks don't progress until data has been processed
        Assert.assertThat(filteredWatermarks.getSynchronizedProcessingInputTime(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        Assert.assertThat(filteredWatermarks.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(TIMESTAMP_MIN_VALUE)));
        CommittedBundle<Integer> createOutput = bundleFactory.createBundle(createdInts).commit(new Instant(1250L));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createOutput), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks createAfterUpdate = manager.getWatermarks(graph.getProducer(createdInts));
        Assert.assertThat(createAfterUpdate.getSynchronizedProcessingInputTime(), Matchers.not(Matchers.greaterThan(clock.now())));
        Assert.assertThat(createAfterUpdate.getSynchronizedProcessingOutputTime(), Matchers.not(Matchers.greaterThan(clock.now())));
        CommittedBundle<Integer> createSecondOutput = bundleFactory.createBundle(createdInts).commit(new Instant(750L));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(createSecondOutput), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        Assert.assertThat(createAfterUpdate.getSynchronizedProcessingOutputTime(), Matchers.equalTo(clock.now()));
    }

    @Test
    public void synchronizedProcessingInputTimeIsHeldToUpstreamProcessingTimeTimers() {
        CommittedBundle<Integer> created = multiWindowedBundle(createdInts, 1, 2, 3);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(created), new Instant(40900L));
        manager.refreshAll();
        CommittedBundle<Integer> filteredBundle = multiWindowedBundle(filtered, 2, 4);
        Instant upstreamHold = new Instant(2048L);
        TimerData upstreamProcessingTimer = TimerData.of(StateNamespaces.global(), upstreamHold, PROCESSING_TIME);
        manager.updateWatermarks(created, TimerUpdate.builder(StructuralKey.of("key", StringUtf8Coder.of())).setTimer(upstreamProcessingTimer).build(), graph.getProducer(filtered), created.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(filteredBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks downstreamWms = manager.getWatermarks(graph.getProducer(filteredTimesTwo));
        Assert.assertThat(downstreamWms.getSynchronizedProcessingInputTime(), Matchers.equalTo(clock.now()));
        clock.set(TIMESTAMP_MAX_VALUE);
        Assert.assertThat(downstreamWms.getSynchronizedProcessingInputTime(), Matchers.equalTo(upstreamHold));
        manager.extractFiredTimers();
        // Pending processing time timers that have been fired but aren't completed hold the
        // synchronized processing time
        Assert.assertThat(downstreamWms.getSynchronizedProcessingInputTime(), Matchers.equalTo(upstreamHold));
        CommittedBundle<Integer> otherCreated = multiWindowedBundle(createdInts, 4, 8, 12);
        manager.updateWatermarks(otherCreated, TimerUpdate.builder(StructuralKey.of("key", StringUtf8Coder.of())).withCompletedTimers(Collections.singleton(upstreamProcessingTimer)).build(), graph.getProducer(filtered), otherCreated.withElements(Collections.emptyList()), Collections.emptyList(), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        Assert.assertThat(downstreamWms.getSynchronizedProcessingInputTime(), Matchers.not(Matchers.lessThan(clock.now())));
    }

    @Test
    public void synchronizedProcessingInputTimeIsHeldToPendingBundleTimes() {
        CommittedBundle<Integer> created = multiWindowedBundle(createdInts, 1, 2, 3);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.<CommittedBundle<?>>singleton(created), new Instant(29919235L));
        Instant upstreamHold = new Instant(2048L);
        CommittedBundle<Integer> filteredBundle = bundleFactory.createKeyedBundle(StructuralKey.of("key", StringUtf8Coder.of()), filtered).commit(upstreamHold);
        manager.updateWatermarks(created, TimerUpdate.empty(), graph.getProducer(filtered), created.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(filteredBundle), TIMESTAMP_MAX_VALUE);
        manager.refreshAll();
        TransformWatermarks downstreamWms = manager.getWatermarks(graph.getProducer(filteredTimesTwo));
        Assert.assertThat(downstreamWms.getSynchronizedProcessingInputTime(), Matchers.equalTo(clock.now()));
        clock.set(TIMESTAMP_MAX_VALUE);
        Assert.assertThat(downstreamWms.getSynchronizedProcessingInputTime(), Matchers.equalTo(upstreamHold));
    }

    @Test
    public void extractFiredTimersReturnsFiredEventTimeTimers() {
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> initialTimers = manager.extractFiredTimers();
        // Watermarks haven't advanced
        Assert.assertThat(initialTimers, Matchers.emptyIterable());
        // Advance WM of keyed past the first timer, but ahead of the second and third
        CommittedBundle<Integer> createdBundle = multiWindowedBundle(filtered);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.singleton(createdBundle), new Instant(1500L));
        manager.refreshAll();
        TimerData earliestTimer = TimerData.of(StateNamespaces.global(), new Instant(1000), EVENT_TIME);
        TimerData middleTimer = TimerData.of(StateNamespaces.global(), new Instant(5000L), EVENT_TIME);
        TimerData lastTimer = TimerData.of(StateNamespaces.global(), new Instant(10000L), EVENT_TIME);
        StructuralKey<byte[]> key = StructuralKey.of(new byte[]{ 1, 4, 9 }, ByteArrayCoder.of());
        TimerUpdate update = TimerUpdate.builder(key).setTimer(earliestTimer).setTimer(middleTimer).setTimer(lastTimer).build();
        manager.updateWatermarks(createdBundle, update, graph.getProducer(filtered), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(multiWindowedBundle(intsToFlatten)), new Instant(1000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> firstFiredTimers = manager.extractFiredTimers();
        Assert.assertThat(firstFiredTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> firstFired = Iterables.getOnlyElement(firstFiredTimers);
        Assert.assertThat(firstFired.getTimers(), Matchers.contains(earliestTimer));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.emptyList(), new Instant(50000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> secondFiredTimers = manager.extractFiredTimers();
        Assert.assertThat(secondFiredTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> secondFired = Iterables.getOnlyElement(secondFiredTimers);
        // Contains, in order, middleTimer and then lastTimer
        Assert.assertThat(secondFired.getTimers(), Matchers.contains(middleTimer, lastTimer));
    }

    @Test
    public void extractFiredTimersReturnsFiredProcessingTimeTimers() {
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> initialTimers = manager.extractFiredTimers();
        // Watermarks haven't advanced
        Assert.assertThat(initialTimers, Matchers.emptyIterable());
        // Advance WM of keyed past the first timer, but ahead of the second and third
        CommittedBundle<Integer> createdBundle = multiWindowedBundle(filtered);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.singleton(createdBundle), new Instant(1500L));
        TimerData earliestTimer = TimerData.of(StateNamespaces.global(), new Instant(999L), PROCESSING_TIME);
        TimerData middleTimer = TimerData.of(StateNamespaces.global(), new Instant(5000L), PROCESSING_TIME);
        TimerData lastTimer = TimerData.of(StateNamespaces.global(), new Instant(10000L), PROCESSING_TIME);
        StructuralKey<?> key = StructuralKey.of((-12L), VarLongCoder.of());
        TimerUpdate update = TimerUpdate.builder(key).setTimer(lastTimer).setTimer(earliestTimer).setTimer(middleTimer).build();
        manager.updateWatermarks(createdBundle, update, graph.getProducer(filtered), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(multiWindowedBundle(intsToFlatten)), new Instant(1000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> firstFiredTimers = manager.extractFiredTimers();
        Assert.assertThat(firstFiredTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> firstFired = Iterables.getOnlyElement(firstFiredTimers);
        Assert.assertThat(firstFired.getTimers(), Matchers.contains(earliestTimer));
        clock.set(new Instant(50000L));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.emptyList(), new Instant(50000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> secondFiredTimers = manager.extractFiredTimers();
        Assert.assertThat(secondFiredTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> secondFired = Iterables.getOnlyElement(secondFiredTimers);
        // Contains, in order, middleTimer and then lastTimer
        Assert.assertThat(secondFired.getTimers(), Matchers.contains(middleTimer, lastTimer));
    }

    @Test
    public void extractFiredTimersReturnsFiredSynchronizedProcessingTimeTimers() {
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> initialTimers = manager.extractFiredTimers();
        // Watermarks haven't advanced
        Assert.assertThat(initialTimers, Matchers.emptyIterable());
        // Advance WM of keyed past the first timer, but ahead of the second and third
        CommittedBundle<Integer> createdBundle = multiWindowedBundle(filtered);
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.singleton(createdBundle), new Instant(1500L));
        TimerData earliestTimer = TimerData.of(StateNamespaces.global(), new Instant(999L), SYNCHRONIZED_PROCESSING_TIME);
        TimerData middleTimer = TimerData.of(StateNamespaces.global(), new Instant(5000L), SYNCHRONIZED_PROCESSING_TIME);
        TimerData lastTimer = TimerData.of(StateNamespaces.global(), new Instant(10000L), SYNCHRONIZED_PROCESSING_TIME);
        StructuralKey<byte[]> key = StructuralKey.of(new byte[]{ 2, -2, 22 }, ByteArrayCoder.of());
        TimerUpdate update = TimerUpdate.builder(key).setTimer(lastTimer).setTimer(earliestTimer).setTimer(middleTimer).build();
        manager.updateWatermarks(createdBundle, update, graph.getProducer(filtered), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(multiWindowedBundle(intsToFlatten)), new Instant(1000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> firstFiredTimers = manager.extractFiredTimers();
        Assert.assertThat(firstFiredTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> firstFired = Iterables.getOnlyElement(firstFiredTimers);
        Assert.assertThat(firstFired.getTimers(), Matchers.contains(earliestTimer));
        clock.set(new Instant(50000L));
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.emptyList(), new Instant(50000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> secondFiredTimers = manager.extractFiredTimers();
        Assert.assertThat(secondFiredTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> secondFired = Iterables.getOnlyElement(secondFiredTimers);
        // Contains, in order, middleTimer and then lastTimer
        Assert.assertThat(secondFired.getTimers(), Matchers.contains(middleTimer, lastTimer));
    }

    @Test
    public void processingTimeTimersCanBeReset() {
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> initialTimers = manager.extractFiredTimers();
        Assert.assertThat(initialTimers, Matchers.emptyIterable());
        String timerId = "myTimer";
        StructuralKey<?> key = StructuralKey.of((-12L), VarLongCoder.of());
        TimerData initialTimer = TimerData.of(timerId, StateNamespaces.global(), new Instant(5000L), PROCESSING_TIME);
        TimerData overridingTimer = TimerData.of(timerId, StateNamespaces.global(), new Instant(10000L), PROCESSING_TIME);
        TimerUpdate initialUpdate = TimerUpdate.builder(key).setTimer(initialTimer).build();
        TimerUpdate overridingUpdate = TimerUpdate.builder(key).setTimer(overridingTimer).build();
        manager.updateWatermarks(null, initialUpdate, graph.getProducer(createdInts), null, Collections.emptyList(), new Instant(5000L));
        manager.refreshAll();
        // This update should override the previous timer.
        manager.updateWatermarks(null, overridingUpdate, graph.getProducer(createdInts), null, Collections.emptyList(), new Instant(10000L));
        // Set clock past the timers.
        clock.set(new Instant(50000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> firedTimers = manager.extractFiredTimers();
        Assert.assertThat(firedTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> timers = Iterables.getOnlyElement(firedTimers);
        Assert.assertThat(timers.getTimers(), Matchers.contains(overridingTimer));
    }

    @Test
    public void eventTimeTimersCanBeReset() {
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> initialTimers = manager.extractFiredTimers();
        Assert.assertThat(initialTimers, Matchers.emptyIterable());
        String timerId = "myTimer";
        StructuralKey<?> key = StructuralKey.of((-12L), VarLongCoder.of());
        TimerData initialTimer = TimerData.of(timerId, StateNamespaces.global(), new Instant(1000L), EVENT_TIME);
        TimerData overridingTimer = TimerData.of(timerId, StateNamespaces.global(), new Instant(2000L), EVENT_TIME);
        TimerUpdate initialUpdate = TimerUpdate.builder(key).setTimer(initialTimer).build();
        TimerUpdate overridingUpdate = TimerUpdate.builder(key).setTimer(overridingTimer).build();
        CommittedBundle<Integer> createdBundle = multiWindowedBundle(filtered);
        manager.updateWatermarks(createdBundle, initialUpdate, graph.getProducer(filtered), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(multiWindowedBundle(intsToFlatten)), new Instant(1000L));
        manager.refreshAll();
        // This update should override the previous timer.
        manager.updateWatermarks(createdBundle, overridingUpdate, graph.getProducer(filtered), createdBundle.withElements(Collections.emptyList()), Collections.<CommittedBundle<?>>singleton(multiWindowedBundle(intsToFlatten)), new Instant(1000L));
        manager.refreshAll();
        // Set WM past the timers.
        manager.updateWatermarks(null, TimerUpdate.empty(), graph.getProducer(createdInts), null, Collections.singleton(createdBundle), new Instant(3000L));
        manager.refreshAll();
        Collection<FiredTimers<AppliedPTransform<?, ?, ?>>> firstFiredTimers = manager.extractFiredTimers();
        Assert.assertThat(firstFiredTimers, Matchers.not(Matchers.emptyIterable()));
        FiredTimers<AppliedPTransform<?, ?, ?>> firstFired = Iterables.getOnlyElement(firstFiredTimers);
        Assert.assertThat(firstFired.getTimers(), Matchers.contains(overridingTimer));
    }

    @Test
    public void inputWatermarkDuplicates() {
        Watermark mockWatermark = Mockito.mock(Watermark.class);
        AppliedPTransformInputWatermark underTest = new AppliedPTransformInputWatermark("underTest", ImmutableList.of(mockWatermark));
        // Refresh
        Mockito.when(mockWatermark.get()).thenReturn(new Instant(0));
        underTest.refresh();
        Assert.assertEquals(new Instant(0), underTest.get());
        // Apply a timer update
        StructuralKey<String> key = StructuralKey.of("key", StringUtf8Coder.of());
        TimerData timer1 = TimerData.of("a", StateNamespaces.global(), new Instant(100), EVENT_TIME);
        TimerData timer2 = TimerData.of("a", StateNamespaces.global(), new Instant(200), EVENT_TIME);
        underTest.updateTimers(TimerUpdate.builder(key).setTimer(timer1).setTimer(timer2).build());
        // Only the last timer update should be observable
        Assert.assertEquals(timer2.getTimestamp(), underTest.getEarliestTimerTimestamp());
        // Advance the input watermark
        Mockito.when(mockWatermark.get()).thenReturn(new Instant(1000));
        underTest.refresh();
        Assert.assertEquals(new Instant(1000), underTest.get());// input watermark is not held by timers

        // Examine the fired event time timers
        Map<StructuralKey<?>, List<TimerData>> fired = underTest.extractFiredEventTimeTimers();
        List<TimerData> timers = fired.get(key);
        Assert.assertNotNull(timers);
        Assert.assertThat(timers, Matchers.contains(timer2));
        // Update based on timer firings
        underTest.updateTimers(TimerUpdate.builder(key).withCompletedTimers(timers).build());
        // Now we should be able to advance
        Assert.assertEquals(TIMESTAMP_MAX_VALUE, underTest.getEarliestTimerTimestamp());
        // Nothing left to fire
        fired = underTest.extractFiredEventTimeTimers();
        Assert.assertThat(fired.entrySet(), Matchers.empty());
    }

    @Test
    public void timerUpdateBuilderBuildAddsAllAddedTimers() {
        TimerData set = TimerData.of(StateNamespaces.global(), new Instant(10L), EVENT_TIME);
        TimerData deleted = TimerData.of(StateNamespaces.global(), new Instant(24L), PROCESSING_TIME);
        TimerData completedOne = TimerData.of(StateNamespaces.global(), new Instant(1024L), SYNCHRONIZED_PROCESSING_TIME);
        TimerData completedTwo = TimerData.of(StateNamespaces.global(), new Instant(2048L), EVENT_TIME);
        TimerUpdate update = TimerUpdate.builder(StructuralKey.of("foo", StringUtf8Coder.of())).withCompletedTimers(ImmutableList.of(completedOne, completedTwo)).setTimer(set).deletedTimer(deleted).build();
        Assert.assertThat(update.getCompletedTimers(), Matchers.containsInAnyOrder(completedOne, completedTwo));
        Assert.assertThat(update.getSetTimers(), Matchers.contains(set));
        Assert.assertThat(update.getDeletedTimers(), Matchers.contains(deleted));
    }

    @Test
    public void timerUpdateBuilderWithSetAtEndOfTime() {
        Instant timerStamp = BoundedWindow.TIMESTAMP_MAX_VALUE;
        TimerData tooFar = TimerData.of(StateNamespaces.global(), timerStamp, EVENT_TIME);
        TimerUpdateBuilder builder = TimerUpdate.builder(StructuralKey.empty());
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(timerStamp.toString());
        builder.setTimer(tooFar);
    }

    @Test
    public void timerUpdateBuilderWithSetPastEndOfTime() {
        Instant timerStamp = TIMESTAMP_MAX_VALUE.plus(Duration.standardMinutes(2));
        TimerData tooFar = TimerData.of(StateNamespaces.global(), timerStamp, EVENT_TIME);
        TimerUpdateBuilder builder = TimerUpdate.builder(StructuralKey.empty());
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(timerStamp.toString());
        builder.setTimer(tooFar);
    }

    @Test
    public void timerUpdateBuilderWithSetThenDeleteHasOnlyDeleted() {
        TimerUpdateBuilder builder = TimerUpdate.builder(null);
        TimerData timer = TimerData.of(StateNamespaces.global(), Instant.now(), EVENT_TIME);
        TimerUpdate built = builder.setTimer(timer).deletedTimer(timer).build();
        Assert.assertThat(built.getSetTimers(), Matchers.emptyIterable());
        Assert.assertThat(built.getDeletedTimers(), Matchers.contains(timer));
    }

    @Test
    public void timerUpdateBuilderWithDeleteThenSetHasOnlySet() {
        TimerUpdateBuilder builder = TimerUpdate.builder(null);
        TimerData timer = TimerData.of(StateNamespaces.global(), Instant.now(), EVENT_TIME);
        TimerUpdate built = builder.deletedTimer(timer).setTimer(timer).build();
        Assert.assertThat(built.getSetTimers(), Matchers.contains(timer));
        Assert.assertThat(built.getDeletedTimers(), Matchers.emptyIterable());
    }

    @Test
    public void timerUpdateBuilderWithSetAfterBuildNotAddedToBuilt() {
        TimerUpdateBuilder builder = TimerUpdate.builder(null);
        TimerData timer = TimerData.of(StateNamespaces.global(), Instant.now(), EVENT_TIME);
        TimerUpdate built = builder.build();
        builder.setTimer(timer);
        Assert.assertThat(built.getSetTimers(), Matchers.emptyIterable());
        builder.build();
        Assert.assertThat(built.getSetTimers(), Matchers.emptyIterable());
    }

    @Test
    public void timerUpdateBuilderWithDeleteAfterBuildNotAddedToBuilt() {
        TimerUpdateBuilder builder = TimerUpdate.builder(null);
        TimerData timer = TimerData.of(StateNamespaces.global(), Instant.now(), EVENT_TIME);
        TimerUpdate built = builder.build();
        builder.deletedTimer(timer);
        Assert.assertThat(built.getDeletedTimers(), Matchers.emptyIterable());
        builder.build();
        Assert.assertThat(built.getDeletedTimers(), Matchers.emptyIterable());
    }

    @Test
    public void timerUpdateBuilderWithCompletedAfterBuildNotAddedToBuilt() {
        TimerUpdateBuilder builder = TimerUpdate.builder(null);
        TimerData timer = TimerData.of(StateNamespaces.global(), Instant.now(), EVENT_TIME);
        TimerUpdate built = builder.build();
        builder.withCompletedTimers(ImmutableList.of(timer));
        Assert.assertThat(built.getCompletedTimers(), Matchers.emptyIterable());
        builder.build();
        Assert.assertThat(built.getCompletedTimers(), Matchers.emptyIterable());
    }

    @Test
    public void timerUpdateWithCompletedTimersNotAddedToExisting() {
        TimerUpdateBuilder builder = TimerUpdate.builder(null);
        TimerData timer = TimerData.of(StateNamespaces.global(), Instant.now(), EVENT_TIME);
        TimerUpdate built = builder.build();
        Assert.assertThat(built.getCompletedTimers(), Matchers.emptyIterable());
        Assert.assertThat(built.withCompletedTimers(ImmutableList.of(timer)).getCompletedTimers(), Matchers.contains(timer));
        Assert.assertThat(built.getCompletedTimers(), Matchers.emptyIterable());
    }
}
