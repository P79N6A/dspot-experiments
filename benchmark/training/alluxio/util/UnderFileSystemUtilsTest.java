/**
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */
package alluxio.util;


import alluxio.AlluxioURI;
import org.junit.Assert;
import org.junit.Test;


public final class UnderFileSystemUtilsTest {
    @Test
    public void getBucketName() throws Exception {
        Assert.assertEquals("s3-bucket-name", UnderFileSystemUtils.getBucketName(new AlluxioURI("s3://s3-bucket-name/")));
        Assert.assertEquals("s3a_bucket_name", UnderFileSystemUtils.getBucketName(new AlluxioURI("s3a://s3a_bucket_name/")));
        Assert.assertEquals("a.b.c", UnderFileSystemUtils.getBucketName(new AlluxioURI("gs://a.b.c/folder/sub-folder/")));
        Assert.assertEquals("container&", UnderFileSystemUtils.getBucketName(new AlluxioURI("swift://container&/folder/file")));
        Assert.assertEquals("oss", UnderFileSystemUtils.getBucketName(new AlluxioURI("oss://oss/folder/.file")));
    }
}
