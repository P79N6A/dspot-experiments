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


import State.RUNNING;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.beam.runners.direct.DirectRunner.DirectPipelineResult;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.PipelineRunner;
import org.apache.beam.sdk.coders.AtomicCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.coders.VarLongCoder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.io.CountingSource;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.util.CoderUtils;
import org.apache.beam.sdk.util.IllegalMutationException;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static java.util.Arrays.asList;


/**
 * Tests for basic {@link DirectRunner} functionality.
 */
@RunWith(JUnit4.class)
public class DirectRunnerTest implements Serializable {
    @Rule
    public transient ExpectedException thrown = ExpectedException.none();

    @Test
    public void defaultRunnerLoaded() {
        Assert.assertThat(DirectRunner.class, Matchers.<Class<? extends PipelineRunner>>equalTo(PipelineOptionsFactory.create().getRunner()));
    }

    @Test
    public void wordCountShouldSucceed() throws Throwable {
        Pipeline p = getPipeline();
        PCollection<KV<String, Long>> counts = p.apply(Create.of("foo", "bar", "foo", "baz", "bar", "foo")).apply(MapElements.via(new org.apache.beam.sdk.transforms.SimpleFunction<String, String>() {
            @Override
            public String apply(String input) {
                return input;
            }
        })).apply(Count.perElement());
        PCollection<String> countStrs = counts.apply(MapElements.via(new org.apache.beam.sdk.transforms.SimpleFunction<KV<String, Long>, String>() {
            @Override
            public String apply(KV<String, Long> input) {
                return String.format("%s: %s", input.getKey(), input.getValue());
            }
        }));
        PAssert.that(countStrs).containsInAnyOrder("baz: 1", "bar: 2", "foo: 3");
        DirectPipelineResult result = ((DirectPipelineResult) (p.run()));
        result.waitUntilFinish();
    }

    private static AtomicInteger changed;

    @Test
    public void reusePipelineSucceeds() throws Throwable {
        Pipeline p = getPipeline();
        DirectRunnerTest.changed = new AtomicInteger(0);
        PCollection<KV<String, Long>> counts = p.apply(Create.of("foo", "bar", "foo", "baz", "bar", "foo")).apply(MapElements.via(new org.apache.beam.sdk.transforms.SimpleFunction<String, String>() {
            @Override
            public String apply(String input) {
                return input;
            }
        })).apply(Count.perElement());
        PCollection<String> countStrs = counts.apply(MapElements.via(new org.apache.beam.sdk.transforms.SimpleFunction<KV<String, Long>, String>() {
            @Override
            public String apply(KV<String, Long> input) {
                return String.format("%s: %s", input.getKey(), input.getValue());
            }
        }));
        counts.apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<KV<String, Long>, Void>() {
            @ProcessElement
            public void updateChanged(ProcessContext c) {
                DirectRunnerTest.changed.getAndIncrement();
            }
        }));
        PAssert.that(countStrs).containsInAnyOrder("baz: 1", "bar: 2", "foo: 3");
        DirectPipelineResult result = ((DirectPipelineResult) (p.run()));
        result.waitUntilFinish();
        DirectPipelineResult otherResult = ((DirectPipelineResult) (p.run()));
        otherResult.waitUntilFinish();
        Assert.assertThat("Each element should have been processed twice", DirectRunnerTest.changed.get(), Matchers.equalTo(6));
    }

    @Test
    public void byteArrayCountShouldSucceed() {
        Pipeline p = getPipeline();
        SerializableFunction<Integer, byte[]> getBytes = ( input) -> {
            try {
                return CoderUtils.encodeToByteArray(VarIntCoder.of(), input);
            } catch ( e) {
                fail(("Unexpected Coder Exception " + e));
                throw new AssertionError("Unreachable");
            }
        };
        TypeDescriptor<byte[]> td = new TypeDescriptor<byte[]>() {};
        PCollection<byte[]> foos = p.apply(Create.of(1, 1, 1, 2, 2, 3)).apply(MapElements.into(td).via(getBytes));
        PCollection<byte[]> msync = p.apply(Create.of(1, (-2), (-8), (-16))).apply(MapElements.into(td).via(getBytes));
        PCollection<byte[]> bytes = org.apache.beam.sdk.values.PCollectionList.of(foos).and(msync).apply(org.apache.beam.sdk.transforms.Flatten.pCollections());
        PCollection<KV<byte[], Long>> counts = bytes.apply(Count.perElement());
        PCollection<KV<Integer, Long>> countsBackToString = counts.apply(MapElements.via(new org.apache.beam.sdk.transforms.SimpleFunction<KV<byte[], Long>, KV<Integer, Long>>() {
            @Override
            public KV<Integer, Long> apply(KV<byte[], Long> input) {
                try {
                    return KV.of(CoderUtils.decodeFromByteArray(VarIntCoder.of(), input.getKey()), input.getValue());
                } catch (CoderException e) {
                    Assert.fail(("Unexpected Coder Exception " + e));
                    throw new AssertionError("Unreachable");
                }
            }
        }));
        Map<Integer, Long> expected = ImmutableMap.<Integer, Long>builder().put(1, 4L).put(2, 2L).put(3, 1L).put((-2), 1L).put((-8), 1L).put((-16), 1L).build();
        PAssert.thatMap(countsBackToString).isEqualTo(expected);
    }

    @Test
    public void splitsInputs() {
        Pipeline p = getPipeline();
        PCollection<Long> longs = p.apply(Read.from(DirectRunnerTest.MustSplitSource.of(CountingSource.upTo(3))));
        PAssert.that(longs).containsInAnyOrder(0L, 1L, 2L);
        p.run();
    }

    @Test
    public void cancelShouldStopPipeline() throws Exception {
        PipelineOptions opts = TestPipeline.testingPipelineOptions();
        opts.as(DirectOptions.class).setBlockOnRun(false);
        opts.setRunner(DirectRunner.class);
        final Pipeline p = Pipeline.create(opts);
        p.apply(GenerateSequence.from(0).withRate(1L, Duration.standardSeconds(1)));
        final BlockingQueue<PipelineResult> resultExchange = new ArrayBlockingQueue<>(1);
        Runnable cancelRunnable = () -> {
            try {
                resultExchange.take().cancel();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };
        Callable<PipelineResult> runPipelineRunnable = () -> {
            PipelineResult res = p.run();
            try {
                resultExchange.put(res);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            return res;
        };
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<?> cancelResult = executor.submit(cancelRunnable);
        Future<PipelineResult> result = executor.submit(runPipelineRunnable);
        cancelResult.get();
        // If cancel doesn't work, this will hang forever
        result.get().waitUntilFinish();
    }

    @Test
    public void testWaitUntilFinishTimeout() throws Exception {
        DirectOptions options = PipelineOptionsFactory.as(DirectOptions.class);
        options.setBlockOnRun(false);
        options.setRunner(DirectRunner.class);
        Pipeline p = Pipeline.create(options);
        p.apply(Create.of(1L)).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<Long, Long>() {
            @ProcessElement
            public void hang(ProcessContext context) throws InterruptedException {
                // Hangs "forever"
                Thread.sleep(Long.MAX_VALUE);
            }
        }));
        PipelineResult result = p.run();
        // The pipeline should never complete;
        Assert.assertThat(result.getState(), Matchers.is(RUNNING));
        // Must time out, otherwise this test will never complete
        result.waitUntilFinish(Duration.millis(1L));
        Assert.assertThat(result.getState(), Matchers.is(RUNNING));
    }

    private static final AtomicLong TEARDOWN_CALL = new AtomicLong((-1));

    @Test
    public void tearsDownFnsBeforeFinishing() {
        DirectRunnerTest.TEARDOWN_CALL.set((-1));
        final Pipeline pipeline = getPipeline();
        pipeline.apply(Create.of("a")).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<String, String>() {
            @ProcessElement
            public void onElement(final ProcessContext ctx) {
                // no-op
            }

            @Teardown
            public void teardown() {
                // just to not have a fast execution hiding an issue until we have a shutdown
                // callback
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    throw new AssertionError(e);
                }
                DirectRunnerTest.TEARDOWN_CALL.set(System.nanoTime());
            }
        }));
        final PipelineResult pipelineResult = pipeline.run();
        pipelineResult.waitUntilFinish();
        final long doneTs = System.nanoTime();
        final long tearDownTs = DirectRunnerTest.TEARDOWN_CALL.get();
        Assert.assertThat(tearDownTs, Matchers.greaterThan(0L));
        Assert.assertThat(doneTs, Matchers.greaterThan(tearDownTs));
    }

    @Test
    public void transformDisplayDataExceptionShouldFail() {
        org.apache.beam.sdk.transforms.DoFn<Integer, Integer> brokenDoFn = new org.apache.beam.sdk.transforms.DoFn<Integer, Integer>() {
            @ProcessElement
            public void processElement(ProcessContext c) throws Exception {
            }

            @Override
            public void populateDisplayData(DisplayData.Builder builder) {
                throw new RuntimeException("oh noes!");
            }
        };
        Pipeline p = getPipeline();
        p.apply(Create.of(1, 2, 3)).apply(ParDo.of(brokenDoFn));
        thrown.expectMessage(brokenDoFn.getClass().getName());
        thrown.expectCause(ThrowableMessageMatcher.hasMessage(Matchers.is("oh noes!")));
        p.run();
    }

    /**
     * Tests that a {@link DoFn} that mutates an output with a good equals() fails in the {@link DirectRunner}.
     */
    @Test
    public void testMutatingOutputThenOutputDoFnError() throws Exception {
        Pipeline pipeline = getPipeline();
        pipeline.apply(Create.of(42)).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<Integer, List<Integer>>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                List<Integer> outputList = asList(1, 2, 3, 4);
                c.output(outputList);
                outputList.set(0, 37);
                c.output(outputList);
            }
        }));
        thrown.expect(IllegalMutationException.class);
        thrown.expectMessage("output");
        thrown.expectMessage("must not be mutated");
        pipeline.run();
    }

    /**
     * Tests that a {@link DoFn} that mutates an output with a good equals() fails in the {@link DirectRunner}.
     */
    @Test
    public void testMutatingOutputWithEnforcementDisabledSucceeds() throws Exception {
        PipelineOptions options = PipelineOptionsFactory.create();
        options.setRunner(DirectRunner.class);
        options.as(DirectOptions.class).setEnforceImmutability(false);
        Pipeline pipeline = Pipeline.create(options);
        pipeline.apply(Create.of(42)).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<Integer, List<Integer>>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                List<Integer> outputList = asList(1, 2, 3, 4);
                c.output(outputList);
                outputList.set(0, 37);
                c.output(outputList);
            }
        }));
        pipeline.run();
    }

    /**
     * Tests that a {@link DoFn} that mutates an output with a good equals() fails in the {@link DirectRunner}.
     */
    @Test
    public void testMutatingOutputThenTerminateDoFnError() throws Exception {
        Pipeline pipeline = getPipeline();
        pipeline.apply(Create.of(42)).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<Integer, List<Integer>>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                List<Integer> outputList = asList(1, 2, 3, 4);
                c.output(outputList);
                outputList.set(0, 37);
            }
        }));
        thrown.expect(IllegalMutationException.class);
        thrown.expectMessage("output");
        thrown.expectMessage("must not be mutated");
        pipeline.run();
    }

    /**
     * Tests that a {@link DoFn} that mutates an output with a bad equals() still fails in the {@link DirectRunner}.
     */
    @Test
    public void testMutatingOutputCoderDoFnError() throws Exception {
        Pipeline pipeline = getPipeline();
        pipeline.apply(Create.of(42)).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<Integer, byte[]>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                byte[] outputArray = new byte[]{ 1, 2, 3 };
                c.output(outputArray);
                outputArray[0] = 10;
                c.output(outputArray);
            }
        }));
        thrown.expect(IllegalMutationException.class);
        thrown.expectMessage("output");
        thrown.expectMessage("must not be mutated");
        pipeline.run();
    }

    /**
     * Tests that a {@link DoFn} that mutates its input with a good equals() fails in the {@link DirectRunner}.
     */
    @Test
    public void testMutatingInputDoFnError() throws Exception {
        Pipeline pipeline = getPipeline();
        pipeline.apply(Create.of(asList(1, 2, 3), asList(4, 5, 6)).withCoder(org.apache.beam.sdk.coders.ListCoder.of(VarIntCoder.of()))).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<List<Integer>, Integer>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                List<Integer> inputList = c.element();
                inputList.set(0, 37);
                c.output(12);
            }
        }));
        thrown.expect(IllegalMutationException.class);
        thrown.expectMessage("Input");
        thrown.expectMessage("must not be mutated");
        pipeline.run();
    }

    /**
     * Tests that a {@link DoFn} that mutates an input with a bad equals() still fails in the {@link DirectRunner}.
     */
    @Test
    public void testMutatingInputCoderDoFnError() throws Exception {
        Pipeline pipeline = getPipeline();
        pipeline.apply(Create.of(new byte[]{ 1, 2, 3 }, new byte[]{ 4, 5, 6 })).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<byte[], Integer>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                byte[] inputArray = c.element();
                inputArray[0] = 10;
                c.output(13);
            }
        }));
        thrown.expect(IllegalMutationException.class);
        thrown.expectMessage("Input");
        thrown.expectMessage("must not be mutated");
        pipeline.run();
    }

    @Test
    public void testUnencodableOutputElement() throws Exception {
        Pipeline p = getPipeline();
        PCollection<Long> pcollection = p.apply(Create.of(((Void) (null)))).apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<Void, Long>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                c.output(null);
            }
        })).setCoder(VarLongCoder.of());
        pcollection.apply(ParDo.of(new org.apache.beam.sdk.transforms.DoFn<Long, Long>() {
            @ProcessElement
            public void unreachable(ProcessContext c) {
                Assert.fail("Pipeline should fail to encode a null Long in VarLongCoder");
            }
        }));
        thrown.expectCause(Matchers.isA(CoderException.class));
        thrown.expectMessage("cannot encode a null Long");
        p.run();
    }

    @Test
    public void testUnencodableOutputFromBoundedRead() throws Exception {
        Pipeline p = getPipeline();
        p.apply(GenerateSequence.from(0).to(10)).setCoder(new DirectRunnerTest.LongNoDecodeCoder());
        thrown.expectCause(Matchers.isA(CoderException.class));
        thrown.expectMessage("Cannot decode a long");
        p.run();
    }

    @Test
    public void testUnencodableOutputFromUnboundedRead() {
        Pipeline p = getPipeline();
        p.apply(GenerateSequence.from(0)).setCoder(new DirectRunnerTest.LongNoDecodeCoder());
        thrown.expectCause(Matchers.isA(CoderException.class));
        thrown.expectMessage("Cannot decode a long");
        p.run();
    }

    private static class LongNoDecodeCoder extends AtomicCoder<Long> {
        @Override
        public void encode(Long value, OutputStream outStream) throws IOException {
        }

        @Override
        public Long decode(InputStream inStream) throws IOException {
            throw new CoderException("Cannot decode a long");
        }
    }

    private static class MustSplitSource<T> extends BoundedSource<T> {
        public static <T> BoundedSource<T> of(BoundedSource<T> underlying) {
            return new DirectRunnerTest.MustSplitSource(underlying);
        }

        private final BoundedSource<T> underlying;

        public MustSplitSource(BoundedSource<T> underlying) {
            this.underlying = underlying;
        }

        @Override
        public List<? extends BoundedSource<T>> split(long desiredBundleSizeBytes, PipelineOptions options) throws Exception {
            // Must have more than
            checkState((desiredBundleSizeBytes < (getEstimatedSizeBytes(options))), "Must split into more than one source");
            return underlying.split(desiredBundleSizeBytes, options);
        }

        @Override
        public long getEstimatedSizeBytes(PipelineOptions options) throws Exception {
            return underlying.getEstimatedSizeBytes(options);
        }

        @Override
        public BoundedReader<T> createReader(PipelineOptions options) throws IOException {
            throw new IllegalStateException("The MustSplitSource cannot create a reader without being split first");
        }

        @Override
        public void validate() {
            underlying.validate();
        }

        @Override
        public Coder<T> getOutputCoder() {
            return underlying.getOutputCoder();
        }
    }
}
