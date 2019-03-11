/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.ml.composition.bagging;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.ml.IgniteModel;
import org.apache.ignite.ml.TestUtils;
import org.apache.ignite.ml.common.TrainerTest;
import org.apache.ignite.ml.composition.predictionsaggregator.OnMajorityPredictionsAggregator;
import org.apache.ignite.ml.dataset.Dataset;
import org.apache.ignite.ml.dataset.DatasetBuilder;
import org.apache.ignite.ml.environment.LearningEnvironment;
import org.apache.ignite.ml.environment.LearningEnvironmentBuilder;
import org.apache.ignite.ml.math.functions.IgniteTriFunction;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.primitives.vector.VectorUtils;
import org.apache.ignite.ml.optimization.updatecalculators.SimpleGDUpdateCalculator;
import org.apache.ignite.ml.regressions.logistic.LogisticRegressionModel;
import org.apache.ignite.ml.regressions.logistic.LogisticRegressionSGDTrainer;
import org.apache.ignite.ml.trainers.DatasetTrainer;
import org.apache.ignite.ml.trainers.FeatureLabelExtractor;
import org.apache.ignite.ml.trainers.TrainerTransformers;
import org.junit.Test;


/**
 * Tests for bagging algorithm.
 */
public class BaggingTest extends TrainerTest {
    /**
     * Dependency of weights of first model in ensemble after training in
     * {@link BaggingTest#testNaiveBaggingLogRegression()}. This dependency is tested to ensure that it is
     * fully determined by provided seeds.
     */
    private static Map<Integer, Vector> firstModelWeights;

    static {
        BaggingTest.firstModelWeights = new HashMap();
        BaggingTest.firstModelWeights.put(1, VectorUtils.of((-0.14721735583126058), 4.366377931980097));
        BaggingTest.firstModelWeights.put(2, VectorUtils.of(0.37824664453495443, 2.9422474282114495));
        BaggingTest.firstModelWeights.put(3, VectorUtils.of((-1.584467989609169), 2.8467326345685824));
        BaggingTest.firstModelWeights.put(4, VectorUtils.of((-2.543461229777167), 0.1317660102621108));
        BaggingTest.firstModelWeights.put(13, VectorUtils.of((-1.6329364937353634), 0.39278455436019116));
    }

    /**
     * Test that count of entries in context is equal to initial dataset size * subsampleRatio.
     */
    @Test
    public void testBaggingContextCount() {
        count(( ctxCount, countData, integer) -> ctxCount);
    }

    /**
     * Test that count of entries in data is equal to initial dataset size * subsampleRatio.
     */
    @Test
    public void testBaggingDataCount() {
        count(( ctxCount, countData, integer) -> countData.cnt);
    }

    /**
     * Test that bagged log regression makes correct predictions.
     */
    @Test
    public void testNaiveBaggingLogRegression() {
        Map<Integer, Double[]> cacheMock = getCacheMock(TrainerTest.twoLinearlySeparableClasses);
        DatasetTrainer<LogisticRegressionModel, Double> trainer = new LogisticRegressionSGDTrainer().withUpdatesStgy(new org.apache.ignite.ml.nn.UpdatesStrategy(new SimpleGDUpdateCalculator(0.2), SimpleGDParameterUpdate::sumLocal, SimpleGDParameterUpdate::avg)).withMaxIterations(30000).withLocIterations(100).withBatchSize(10).withSeed(123L);
        BaggedTrainer<Double> baggedTrainer = TrainerTransformers.makeBagged(trainer, 7, 0.7, 2, 2, new OnMajorityPredictionsAggregator()).withEnvironmentBuilder(TestUtils.testEnvBuilder());
        BaggedModel mdl = baggedTrainer.fit(cacheMock, parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 1, v.length)), ( k, v) -> v[0]);
        Vector weights = weights();
        TestUtils.assertEquals(BaggingTest.firstModelWeights.get(parts), weights, 0.0);
        TestUtils.assertEquals(0, mdl.predict(VectorUtils.of(100, 10)), TrainerTest.PRECISION);
        TestUtils.assertEquals(1, mdl.predict(VectorUtils.of(10, 100)), TrainerTest.PRECISION);
    }

    /**
     * Trainer used to count entries in context or in data.
     */
    protected static class CountTrainer extends DatasetTrainer<IgniteModel<Vector, Double>, Double> {
        /**
         * Function specifying which entries to count.
         */
        private final IgniteTriFunction<Long, BaggingTest.CountData, LearningEnvironment, Long> cntr;

        /**
         * Construct instance of this class.
         *
         * @param cntr
         * 		Function specifying which entries to count.
         */
        public CountTrainer(IgniteTriFunction<Long, BaggingTest.CountData, LearningEnvironment, Long> cntr) {
            this.cntr = cntr;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public <K, V> IgniteModel<Vector, Double> fit(DatasetBuilder<K, V> datasetBuilder, FeatureLabelExtractor<K, V, Double> extractor) {
            Dataset<Long, BaggingTest.CountData> dataset = datasetBuilder.build(TestUtils.testEnvBuilder(), ( env, upstreamData, upstreamDataSize) -> upstreamDataSize, ( env, upstreamData, upstreamDataSize, ctx) -> new org.apache.ignite.ml.composition.bagging.CountData(upstreamDataSize));
            Long cnt = dataset.computeWithCtx(cntr, BaggingTest::plusOfNullables);
            return ( x) -> Double.valueOf(cnt);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public boolean isUpdateable(IgniteModel<Vector, Double> mdl) {
            return true;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        protected <K, V> IgniteModel<Vector, Double> updateModel(IgniteModel<Vector, Double> mdl, DatasetBuilder<K, V> datasetBuilder, FeatureLabelExtractor<K, V, Double> extractor) {
            return fit(datasetBuilder, extractor);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public BaggingTest.CountTrainer withEnvironmentBuilder(LearningEnvironmentBuilder envBuilder) {
            return ((BaggingTest.CountTrainer) (super.withEnvironmentBuilder(envBuilder)));
        }
    }

    /**
     * Data for count trainer.
     */
    protected static class CountData implements AutoCloseable {
        /**
         * Counter.
         */
        private long cnt;

        /**
         * Construct instance of this class.
         *
         * @param cnt
         * 		Counter.
         */
        public CountData(long cnt) {
            this.cnt = cnt;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void close() throws Exception {
            // No-op
        }
    }
}
