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
package org.apache.flink.runtime.fs.hdfs;


import java.net.URI;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.LimitedConnectionsFileSystem;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 * Test that the Hadoop file system wrapper correctly picks up connection limiting
 * settings for the correct file systems.
 */
public class LimitedConnectionsConfigurationTest {
    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void testConfiguration() throws Exception {
        // nothing configured, we should get a regular file system
        FileSystem hdfs = FileSystem.get(URI.create("hdfs://localhost:12345/a/b/c"));
        FileSystem ftpfs = FileSystem.get(URI.create("ftp://localhost:12345/a/b/c"));
        Assert.assertFalse((hdfs instanceof LimitedConnectionsFileSystem));
        Assert.assertFalse((ftpfs instanceof LimitedConnectionsFileSystem));
        // configure some limits, which should cause "fsScheme" to be limited
        final Configuration config = new Configuration();
        config.setInteger("fs.hdfs.limit.total", 40);
        config.setInteger("fs.hdfs.limit.input", 39);
        config.setInteger("fs.hdfs.limit.output", 38);
        config.setInteger("fs.hdfs.limit.timeout", 23456);
        config.setInteger("fs.hdfs.limit.stream-timeout", 34567);
        try {
            FileSystem.initialize(config);
            hdfs = FileSystem.get(URI.create("hdfs://localhost:12345/a/b/c"));
            ftpfs = FileSystem.get(URI.create("ftp://localhost:12345/a/b/c"));
            Assert.assertTrue((hdfs instanceof LimitedConnectionsFileSystem));
            Assert.assertFalse((ftpfs instanceof LimitedConnectionsFileSystem));
            LimitedConnectionsFileSystem limitedFs = ((LimitedConnectionsFileSystem) (hdfs));
            Assert.assertEquals(40, limitedFs.getMaxNumOpenStreamsTotal());
            Assert.assertEquals(39, limitedFs.getMaxNumOpenInputStreams());
            Assert.assertEquals(38, limitedFs.getMaxNumOpenOutputStreams());
            Assert.assertEquals(23456, limitedFs.getStreamOpenTimeout());
            Assert.assertEquals(34567, limitedFs.getStreamInactivityTimeout());
        } finally {
            // clear all settings
            FileSystem.initialize(new Configuration());
        }
    }
}
