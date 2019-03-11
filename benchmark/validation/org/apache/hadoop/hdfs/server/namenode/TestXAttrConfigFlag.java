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
package org.apache.hadoop.hdfs.server.namenode;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests that the configuration flag that controls support for XAttrs is off
 * and causes all attempted operations related to XAttrs to fail.  The
 * NameNode can still load XAttrs from fsimage or edits.
 */
public class TestXAttrConfigFlag {
    private static final Path PATH = new Path("/path");

    private MiniDFSCluster cluster;

    private DistributedFileSystem fs;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testSetXAttr() throws Exception {
        initCluster(true, false);
        fs.mkdirs(TestXAttrConfigFlag.PATH);
        expectException();
        fs.setXAttr(TestXAttrConfigFlag.PATH, "user.foo", null);
    }

    @Test
    public void testGetXAttrs() throws Exception {
        initCluster(true, false);
        fs.mkdirs(TestXAttrConfigFlag.PATH);
        expectException();
        fs.getXAttrs(TestXAttrConfigFlag.PATH);
    }

    @Test
    public void testRemoveXAttr() throws Exception {
        initCluster(true, false);
        fs.mkdirs(TestXAttrConfigFlag.PATH);
        expectException();
        fs.removeXAttr(TestXAttrConfigFlag.PATH, "user.foo");
    }

    @Test
    public void testEditLog() throws Exception {
        // With XAttrs enabled, set an XAttr.
        initCluster(true, true);
        fs.mkdirs(TestXAttrConfigFlag.PATH);
        fs.setXAttr(TestXAttrConfigFlag.PATH, "user.foo", null);
        // Restart with XAttrs disabled.  Expect successful restart.
        restart(false, false);
    }

    @Test
    public void testFsImage() throws Exception {
        // With XAttrs enabled, set an XAttr.
        initCluster(true, true);
        fs.mkdirs(TestXAttrConfigFlag.PATH);
        fs.setXAttr(TestXAttrConfigFlag.PATH, "user.foo", null);
        // Save a new checkpoint and restart with XAttrs still enabled.
        restart(true, true);
        // Restart with XAttrs disabled.  Expect successful restart.
        restart(false, false);
    }
}
