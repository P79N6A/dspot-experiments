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
package org.apache.ignite.ml.knn;


import NNStrategy.SIMPLE;
import NNStrategy.WEIGHTED;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.ml.knn.classification.KNNClassificationModel;
import org.apache.ignite.ml.knn.classification.KNNClassificationTrainer;
import org.apache.ignite.ml.math.distances.EuclideanDistance;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.primitives.vector.VectorUtils;
import org.apache.ignite.ml.math.primitives.vector.impl.DenseVector;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Tests behaviour of KNNClassification.
 */
@RunWith(Parameterized.class)
public class KNNClassificationTest {
    /**
     * Number of parts to be tested.
     */
    private static final int[] partsToBeTested = new int[]{ 1, 2, 3, 4, 5, 7, 100 };

    /**
     * Number of partitions.
     */
    @Parameterized.Parameter
    public int parts;

    /**
     *
     */
    @Test(expected = IllegalStateException.class)
    public void testNullDataset() {
        new KNNClassificationModel(null).predict(null);
    }

    /**
     *
     */
    @Test
    public void testBinaryClassification() {
        Map<Integer, double[]> data = new HashMap<>();
        data.put(0, new double[]{ 1.0, 1.0, 1.0 });
        data.put(1, new double[]{ 1.0, 2.0, 1.0 });
        data.put(2, new double[]{ 2.0, 1.0, 1.0 });
        data.put(3, new double[]{ -1.0, -1.0, 2.0 });
        data.put(4, new double[]{ -1.0, -2.0, 2.0 });
        data.put(5, new double[]{ -2.0, -1.0, 2.0 });
        KNNClassificationTrainer trainer = new KNNClassificationTrainer();
        NNClassificationModel knnMdl = trainer.fit(data, parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 0, (v.length - 1))), ( k, v) -> v[2]).withK(3).withDistanceMeasure(new EuclideanDistance()).withStrategy(SIMPLE);
        Assert.assertTrue((!(knnMdl.toString().isEmpty())));
        Assert.assertTrue((!(knnMdl.toString(true).isEmpty())));
        Assert.assertTrue((!(knnMdl.toString(false).isEmpty())));
        Vector firstVector = new DenseVector(new double[]{ 2.0, 2.0 });
        Assert.assertEquals(1.0, knnMdl.predict(firstVector), 0);
        Vector secondVector = new DenseVector(new double[]{ -2.0, -2.0 });
        Assert.assertEquals(2.0, knnMdl.predict(secondVector), 0);
    }

    /**
     *
     */
    @Test
    public void testBinaryClassificationWithSmallestK() {
        Map<Integer, double[]> data = new HashMap<>();
        data.put(0, new double[]{ 1.0, 1.0, 1.0 });
        data.put(1, new double[]{ 1.0, 2.0, 1.0 });
        data.put(2, new double[]{ 2.0, 1.0, 1.0 });
        data.put(3, new double[]{ -1.0, -1.0, 2.0 });
        data.put(4, new double[]{ -1.0, -2.0, 2.0 });
        data.put(5, new double[]{ -2.0, -1.0, 2.0 });
        KNNClassificationTrainer trainer = new KNNClassificationTrainer();
        NNClassificationModel knnMdl = trainer.fit(data, parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 0, (v.length - 1))), ( k, v) -> v[2]).withK(1).withDistanceMeasure(new EuclideanDistance()).withStrategy(SIMPLE);
        Vector firstVector = new DenseVector(new double[]{ 2.0, 2.0 });
        Assert.assertEquals(1.0, knnMdl.predict(firstVector), 0);
        Vector secondVector = new DenseVector(new double[]{ -2.0, -2.0 });
        Assert.assertEquals(2.0, knnMdl.predict(secondVector), 0);
    }

    /**
     *
     */
    @Test
    public void testBinaryClassificationFarPointsWithSimpleStrategy() {
        Map<Integer, double[]> data = new HashMap<>();
        data.put(0, new double[]{ 10.0, 10.0, 1.0 });
        data.put(1, new double[]{ 10.0, 20.0, 1.0 });
        data.put(2, new double[]{ -1, -1, 1.0 });
        data.put(3, new double[]{ -2, -2, 2.0 });
        data.put(4, new double[]{ -1.0, -2.0, 2.0 });
        data.put(5, new double[]{ -2.0, -1.0, 2.0 });
        KNNClassificationTrainer trainer = new KNNClassificationTrainer();
        NNClassificationModel knnMdl = trainer.fit(data, parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 0, (v.length - 1))), ( k, v) -> v[2]).withK(3).withDistanceMeasure(new EuclideanDistance()).withStrategy(SIMPLE);
        Vector vector = new DenseVector(new double[]{ -1.01, -1.01 });
        Assert.assertEquals(2.0, knnMdl.predict(vector), 0);
    }

    /**
     *
     */
    @Test
    public void testBinaryClassificationFarPointsWithWeightedStrategy() {
        Map<Integer, double[]> data = new HashMap<>();
        data.put(0, new double[]{ 10.0, 10.0, 1.0 });
        data.put(1, new double[]{ 10.0, 20.0, 1.0 });
        data.put(2, new double[]{ -1, -1, 1.0 });
        data.put(3, new double[]{ -2, -2, 2.0 });
        data.put(4, new double[]{ -1.0, -2.0, 2.0 });
        data.put(5, new double[]{ -2.0, -1.0, 2.0 });
        KNNClassificationTrainer trainer = new KNNClassificationTrainer();
        NNClassificationModel knnMdl = trainer.fit(data, parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 0, (v.length - 1))), ( k, v) -> v[2]).withK(3).withDistanceMeasure(new EuclideanDistance()).withStrategy(WEIGHTED);
        Vector vector = new DenseVector(new double[]{ -1.01, -1.01 });
        Assert.assertEquals(1.0, knnMdl.predict(vector), 0);
    }

    /**
     *
     */
    @Test
    public void testUpdate() {
        Map<Integer, double[]> data = new HashMap<>();
        data.put(0, new double[]{ 10.0, 10.0, 1.0 });
        data.put(1, new double[]{ 10.0, 20.0, 1.0 });
        data.put(2, new double[]{ -1, -1, 1.0 });
        data.put(3, new double[]{ -2, -2, 2.0 });
        data.put(4, new double[]{ -1.0, -2.0, 2.0 });
        data.put(5, new double[]{ -2.0, -1.0, 2.0 });
        KNNClassificationTrainer trainer = new KNNClassificationTrainer();
        KNNClassificationModel originalMdl = ((KNNClassificationModel) (trainer.fit(data, parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 0, (v.length - 1))), ( k, v) -> v[2]).withK(3).withDistanceMeasure(new EuclideanDistance()).withStrategy(WEIGHTED)));
        KNNClassificationModel updatedOnSameDataset = trainer.update(originalMdl, data, parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 0, (v.length - 1))), ( k, v) -> v[2]);
        KNNClassificationModel updatedOnEmptyDataset = trainer.update(originalMdl, new HashMap<Integer, double[]>(), parts, ( k, v) -> VectorUtils.of(Arrays.copyOfRange(v, 0, (v.length - 1))), ( k, v) -> v[2]);
        Vector vector = new DenseVector(new double[]{ -1.01, -1.01 });
        Assert.assertEquals(originalMdl.predict(vector), updatedOnSameDataset.predict(vector));
        Assert.assertEquals(originalMdl.predict(vector), updatedOnEmptyDataset.predict(vector));
    }
}
