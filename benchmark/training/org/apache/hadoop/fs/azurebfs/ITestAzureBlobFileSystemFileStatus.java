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
package org.apache.hadoop.fs.azurebfs;


import CommonConfigurationKeys.FS_PERMISSIONS_UMASK_KEY;
import java.io.IOException;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test FileStatus.
 */
public class ITestAzureBlobFileSystemFileStatus extends AbstractAbfsIntegrationTest {
    private static final String DEFAULT_FILE_PERMISSION_VALUE = "640";

    private static final String DEFAULT_DIR_PERMISSION_VALUE = "750";

    private static final String DEFAULT_UMASK_VALUE = "027";

    private static final String FULL_PERMISSION = "777";

    private static final Path TEST_FILE = new Path("testFile");

    private static final Path TEST_FOLDER = new Path("testDir");

    public ITestAzureBlobFileSystemFileStatus() throws Exception {
        super();
    }

    @Test
    public void testEnsureStatusWorksForRoot() throws Exception {
        final AzureBlobFileSystem fs = this.getFileSystem();
        Path root = new Path("/");
        FileStatus[] rootls = fs.listStatus(root);
        Assert.assertEquals("root listing", 0, rootls.length);
    }

    @Test
    public void testFileStatusPermissionsAndOwnerAndGroup() throws Exception {
        final AzureBlobFileSystem fs = this.getFileSystem();
        fs.getConf().set(FS_PERMISSIONS_UMASK_KEY, ITestAzureBlobFileSystemFileStatus.DEFAULT_UMASK_VALUE);
        touch(ITestAzureBlobFileSystemFileStatus.TEST_FILE);
        validateStatus(fs, ITestAzureBlobFileSystemFileStatus.TEST_FILE, false);
    }

    @Test
    public void testFolderStatusPermissionsAndOwnerAndGroup() throws Exception {
        final AzureBlobFileSystem fs = this.getFileSystem();
        fs.getConf().set(FS_PERMISSIONS_UMASK_KEY, ITestAzureBlobFileSystemFileStatus.DEFAULT_UMASK_VALUE);
        fs.mkdirs(ITestAzureBlobFileSystemFileStatus.TEST_FOLDER);
        validateStatus(fs, ITestAzureBlobFileSystemFileStatus.TEST_FOLDER, true);
    }

    @Test
    public void testAbfsPathWithHost() throws IOException {
        AzureBlobFileSystem fs = this.getFileSystem();
        Path pathWithHost1 = new Path("abfs://mycluster/abfs/file1.txt");
        Path pathwithouthost1 = new Path("/abfs/file1.txt");
        Path pathWithHost2 = new Path("abfs://mycluster/abfs/file2.txt");
        Path pathwithouthost2 = new Path("/abfs/file2.txt");
        // verify compatibility of this path format
        fs.create(pathWithHost1);
        Assert.assertTrue(fs.exists(pathwithouthost1));
        fs.create(pathwithouthost2);
        Assert.assertTrue(fs.exists(pathWithHost2));
        // verify get
        FileStatus fileStatus1 = fs.getFileStatus(pathWithHost1);
        Assert.assertEquals(pathwithouthost1.getName(), fileStatus1.getPath().getName());
        FileStatus fileStatus2 = fs.getFileStatus(pathwithouthost2);
        Assert.assertEquals(pathWithHost2.getName(), fileStatus2.getPath().getName());
    }
}
