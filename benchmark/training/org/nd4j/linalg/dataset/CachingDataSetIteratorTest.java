/**
 * *****************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ****************************************************************************
 */
package org.nd4j.linalg.dataset;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.cache.DataSetCache;
import org.nd4j.linalg.dataset.api.iterator.cache.InFileDataSetCache;
import org.nd4j.linalg.dataset.api.iterator.cache.InMemoryDataSetCache;
import org.nd4j.linalg.factory.Nd4jBackend;


/**
 * Created by anton on 7/18/16.
 */
@RunWith(Parameterized.class)
public class CachingDataSetIteratorTest extends BaseNd4jTest {
    public CachingDataSetIteratorTest(Nd4jBackend backend) {
        super(backend);
    }

    @Test
    public void testInMemory() {
        DataSetCache cache = new InMemoryDataSetCache();
        runDataSetTest(cache);
    }

    @Test
    public void testInFile() throws IOException {
        Path cacheDir = Files.createTempDirectory("nd4j-data-set-cache-test");
        DataSetCache cache = new InFileDataSetCache(cacheDir);
        runDataSetTest(cache);
        FileUtils.deleteDirectory(cacheDir.toFile());
    }

    private class PreProcessor implements DataSetPreProcessor {
        private int callCount;

        @Override
        public void preProcess(org.nd4j.linalg.dataset.api.DataSet toPreProcess) {
            (callCount)++;
        }

        public int getCallCount() {
            return callCount;
        }
    }
}
