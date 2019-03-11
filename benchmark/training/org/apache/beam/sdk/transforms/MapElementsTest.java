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
package org.apache.beam.sdk.transforms;


import DisplayData.Builder;
import java.io.Serializable;
import java.util.Set;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.transforms.display.DisplayDataEvaluator;
import org.apache.beam.sdk.transforms.display.DisplayDataMatchers;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.apache.beam.sdk.values.TypeDescriptors.integers;


/**
 * Tests for {@link MapElements}.
 */
@RunWith(JUnit4.class)
public class MapElementsTest implements Serializable {
    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Rule
    public transient ExpectedException thrown = ExpectedException.none();

    /**
     * A {@link SimpleFunction} to test that the coder registry can propagate coders that are bound to
     * type variables.
     */
    private static class PolymorphicSimpleFunction<T> extends SimpleFunction<T, T> {
        @Override
        public T apply(T input) {
            return input;
        }
    }

    /**
     * A {@link SimpleFunction} to test that the coder registry can propagate coders that are bound to
     * type variables, when the variable appears nested in the output.
     */
    private static class NestedPolymorphicSimpleFunction<T> extends SimpleFunction<T, KV<T, String>> {
        @Override
        public KV<T, String> apply(T input) {
            return KV.of(input, "hello");
        }
    }

    /**
     * An {@link InferableFunction} to test that the coder registry can propagate coders that are
     * bound to type variables.
     */
    private static class PolymorphicInferableFunction<T> extends InferableFunction<T, T> {
        @Override
        public T apply(T input) throws Exception {
            return input;
        }
    }

    /**
     * An {@link InferableFunction} to test that the coder registry can propagate coders that are
     * bound to type variables, when the variable appears nested in the output.
     */
    private static class NestedPolymorphicInferableFunction<T> extends InferableFunction<T, KV<T, String>> {
        @Override
        public KV<T, String> apply(T input) throws Exception {
            return KV.of(input, "hello");
        }
    }

    /**
     * Basic test of {@link MapElements} with a {@link SimpleFunction}.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testMapSimpleFunction() throws Exception {
        PCollection<Integer> output = pipeline.apply(Create.of(1, 2, 3)).apply(MapElements.via(new SimpleFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return -input;
            }
        }));
        PAssert.that(output).containsInAnyOrder((-2), (-1), (-3));
        pipeline.run();
    }

    /**
     * Basic test of {@link MapElements} with an {@link InferableFunction}.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testMapInferableFunction() throws Exception {
        PCollection<Integer> output = pipeline.apply(Create.of(1, 2, 3)).apply(MapElements.via(new InferableFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) throws Exception {
                return -input;
            }
        }));
        PAssert.that(output).containsInAnyOrder((-2), (-1), (-3));
        pipeline.run();
    }

    /**
     * Basic test of {@link MapElements} with a {@link Fn} and a side input.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testMapBasicWithSideInput() throws Exception {
        final PCollectionView<Integer> view = pipeline.apply("Create base", Create.of(40)).apply(View.asSingleton());
        PCollection<Integer> output = pipeline.apply(Create.of(0, 1, 2)).apply(MapElements.into(integers()).via(Contextful.fn(( element, c) -> element + (c.sideInput(view)), Requirements.requiresSideInputs(view))));
        PAssert.that(output).containsInAnyOrder(40, 41, 42);
        pipeline.run();
    }

    /**
     * Basic test of {@link MapElements} coder propagation with a parametric {@link SimpleFunction}.
     */
    @Test
    public void testPolymorphicSimpleFunction() throws Exception {
        pipeline.enableAbandonedNodeEnforcement(false);
        // This is a consumer to ensure that all coder inference logic is executed.
        // This is the function that needs to propagate the input T to output T
        pipeline.apply(Create.of(1, 2, 3)).apply("Polymorphic Identity", MapElements.via(new MapElementsTest.PolymorphicSimpleFunction())).apply("Test Consumer", MapElements.via(new SimpleFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input;
            }
        }));
    }

    /**
     * Basic test of {@link MapElements} coder propagation with a parametric {@link InferableFunction}.
     */
    @Test
    public void testPolymorphicInferableFunction() throws Exception {
        pipeline.enableAbandonedNodeEnforcement(false);
        pipeline.apply(Create.of(1, 2, 3)).apply("Polymorphic Identity", MapElements.via(new MapElementsTest.PolymorphicInferableFunction())).apply("Test Consumer", MapElements.via(new InferableFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) throws Exception {
                return input;
            }
        }));
    }

    /**
     * Test of {@link MapElements} coder propagation with a parametric {@link SimpleFunction} where
     * the type variable occurs nested within other concrete type constructors.
     */
    @Test
    public void testNestedPolymorphicSimpleFunction() throws Exception {
        pipeline.enableAbandonedNodeEnforcement(false);
        // This is a consumer to ensure that all coder inference logic is executed.
        // This is the function that needs to propagate the input T to output T
        pipeline.apply(Create.of(1, 2, 3)).apply("Polymorphic Identity", MapElements.via(new MapElementsTest.NestedPolymorphicSimpleFunction())).apply("Test Consumer", MapElements.via(new SimpleFunction<KV<Integer, String>, Integer>() {
            @Override
            public Integer apply(KV<Integer, String> input) {
                return 42;
            }
        }));
    }

    /**
     * Test of {@link MapElements} coder propagation with a parametric {@link InferableFunction} where
     * the type variable occurs nested within other concrete type constructors.
     */
    @Test
    public void testNestedPolymorphicInferableFunction() throws Exception {
        pipeline.enableAbandonedNodeEnforcement(false);
        pipeline.apply(Create.of(1, 2, 3)).apply("Polymorphic Identity", MapElements.via(new MapElementsTest.NestedPolymorphicInferableFunction())).apply("Test Consumer", MapElements.via(new InferableFunction<KV<Integer, String>, Integer>() {
            @Override
            public Integer apply(KV<Integer, String> input) throws Exception {
                return 42;
            }
        }));
    }

    /**
     * Basic test of {@link MapElements} with a {@link ProcessFunction}.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testMapBasicProcessFunction() throws Exception {
        PCollection<Integer> output = pipeline.apply(Create.of(1, 2, 3)).apply(MapElements.into(integers()).via(( input) -> -input));
        PAssert.that(output).containsInAnyOrder((-2), (-1), (-3));
        pipeline.run();
    }

    /**
     * Tests that when built with a concrete subclass of {@link SimpleFunction}, the type descriptor
     * of the output reflects its static type.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testSimpleFunctionOutputTypeDescriptor() throws Exception {
        PCollection<String> output = pipeline.apply(Create.of("hello")).apply(MapElements.via(new SimpleFunction<String, String>() {
            @Override
            public String apply(String input) {
                return input;
            }
        }));
        Assert.assertThat(output.getTypeDescriptor(), Matchers.equalTo(((TypeDescriptor<String>) (new TypeDescriptor<String>() {}))));
        Assert.assertThat(pipeline.getCoderRegistry().getCoder(output.getTypeDescriptor()), Matchers.equalTo(pipeline.getCoderRegistry().getCoder(new TypeDescriptor<String>() {})));
        // Make sure the pipeline runs too
        pipeline.run();
    }

    /**
     * Tests that when built with a concrete subclass of {@link InferableFunction}, the type
     * descriptor of the output reflects its static type.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testInferableFunctionOutputTypeDescriptor() throws Exception {
        PCollection<String> output = pipeline.apply(Create.of("hello")).apply(MapElements.via(new InferableFunction<String, String>() {
            @Override
            public String apply(String input) throws Exception {
                return input;
            }
        }));
        Assert.assertThat(output.getTypeDescriptor(), Matchers.equalTo(((TypeDescriptor<String>) (new TypeDescriptor<String>() {}))));
        Assert.assertThat(pipeline.getCoderRegistry().getCoder(output.getTypeDescriptor()), Matchers.equalTo(pipeline.getCoderRegistry().getCoder(new TypeDescriptor<String>() {})));
        // Make sure the pipeline runs too
        pipeline.run();
    }

    @Test
    @Category(NeedsRunner.class)
    public void testVoidValues() throws Exception {
        pipeline.apply(Create.of("hello")).apply(WithKeys.of("k")).apply(new MapElementsTest.VoidValues<String, String>() {});
        // Make sure the pipeline runs
        pipeline.run();
    }

    @Test
    public void testSerializableFunctionDisplayData() {
        SerializableFunction<Integer, Integer> serializableFn = ( input) -> input;
        MapElements<?, ?> serializableMap = MapElements.into(integers()).via(serializableFn);
        Assert.assertThat(DisplayData.from(serializableMap), DisplayDataMatchers.hasDisplayItem("class", serializableFn.getClass()));
    }

    @Test
    public void testProcessFunctionDisplayData() {
        ProcessFunction<Integer, Integer> processFn = ( input) -> input;
        MapElements<?, ?> processMap = MapElements.into(integers()).via(processFn);
        Assert.assertThat(DisplayData.from(processMap), DisplayDataMatchers.hasDisplayItem("class", processFn.getClass()));
    }

    @Test
    public void testSimpleFunctionClassDisplayData() {
        SimpleFunction<?, ?> simpleFn = new SimpleFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input;
            }
        };
        MapElements<?, ?> simpleMap = MapElements.via(simpleFn);
        Assert.assertThat(DisplayData.from(simpleMap), DisplayDataMatchers.hasDisplayItem("class", simpleFn.getClass()));
    }

    @Test
    public void testInferableFunctionClassDisplayData() {
        InferableFunction<?, ?> inferableFn = new InferableFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) throws Exception {
                return input;
            }
        };
        MapElements<?, ?> inferableMap = MapElements.via(inferableFn);
        Assert.assertThat(DisplayData.from(inferableMap), DisplayDataMatchers.hasDisplayItem("class", inferableFn.getClass()));
    }

    @Test
    public void testSimpleFunctionDisplayData() {
        SimpleFunction<Integer, ?> simpleFn = new SimpleFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input;
            }

            @Override
            public void populateDisplayData(DisplayData.Builder builder) {
                builder.add(DisplayData.item("foo", "baz"));
            }
        };
        MapElements<?, ?> simpleMap = MapElements.via(simpleFn);
        Assert.assertThat(DisplayData.from(simpleMap), DisplayDataMatchers.hasDisplayItem("class", simpleFn.getClass()));
        Assert.assertThat(DisplayData.from(simpleMap), DisplayDataMatchers.hasDisplayItem("foo", "baz"));
    }

    @Test
    public void testInferableFunctionDisplayData() {
        InferableFunction<Integer, ?> inferableFn = new InferableFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input;
            }

            @Override
            public void populateDisplayData(DisplayData.Builder builder) {
                builder.add(DisplayData.item("foo", "baz"));
            }
        };
        MapElements<?, ?> inferableMap = MapElements.via(inferableFn);
        Assert.assertThat(DisplayData.from(inferableMap), DisplayDataMatchers.hasDisplayItem("class", inferableFn.getClass()));
        Assert.assertThat(DisplayData.from(inferableMap), DisplayDataMatchers.hasDisplayItem("foo", "baz"));
    }

    @Test
    @Category(ValidatesRunner.class)
    public void testPrimitiveDisplayData() {
        SimpleFunction<Integer, ?> mapFn = new SimpleFunction<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input;
            }
        };
        MapElements<Integer, ?> map = MapElements.via(mapFn);
        DisplayDataEvaluator evaluator = DisplayDataEvaluator.create();
        Set<DisplayData> displayData = evaluator.displayDataForPrimitiveTransforms(map);
        Assert.assertThat("MapElements should include the mapFn in its primitive display data", displayData, Matchers.hasItem(DisplayDataMatchers.hasDisplayItem("class", mapFn.getClass())));
    }

    static class VoidValues<K, V> extends PTransform<PCollection<KV<K, V>>, PCollection<KV<K, Void>>> {
        @Override
        public PCollection<KV<K, Void>> expand(PCollection<KV<K, V>> input) {
            return input.apply(MapElements.via(new SimpleFunction<KV<K, V>, KV<K, Void>>() {
                @Override
                public KV<K, Void> apply(KV<K, V> input) {
                    return KV.of(input.getKey(), null);
                }
            }));
        }
    }

    /**
     * Basic test of {@link MapElements} with a lambda (which is instantiated as a {@link SerializableFunction}).
     */
    @Test
    @Category(NeedsRunner.class)
    public void testMapLambda() throws Exception {
        PCollection<Integer> output = pipeline.apply(Create.of(1, 2, 3)).apply(// Note that the type annotation is required.
        MapElements.into(integers()).via((Integer i) -> i * 2));
        PAssert.that(output).containsInAnyOrder(6, 2, 4);
        pipeline.run();
    }

    /**
     * Basic test of {@link MapElements} with a lambda wrapped into a {@link SimpleFunction} to
     * remember its type.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testMapWrappedLambda() throws Exception {
        PCollection<Integer> output = pipeline.apply(Create.of(1, 2, 3)).apply(MapElements.via(new SimpleFunction<Integer, Integer>((Integer i) -> i * 2) {}));
        PAssert.that(output).containsInAnyOrder(6, 2, 4);
        pipeline.run();
    }

    /**
     * Basic test of {@link MapElements} with a method reference.
     */
    @Test
    @Category(NeedsRunner.class)
    public void testMapMethodReference() throws Exception {
        PCollection<Integer> output = pipeline.apply(Create.of(1, 2, 3)).apply(// Note that the type annotation is required.
        MapElements.into(integers()).via(new MapElementsTest.Doubler()::doubleIt));
        PAssert.that(output).containsInAnyOrder(6, 2, 4);
        pipeline.run();
    }

    private static class Doubler implements Serializable {
        public int doubleIt(int val) {
            return val * 2;
        }
    }
}
