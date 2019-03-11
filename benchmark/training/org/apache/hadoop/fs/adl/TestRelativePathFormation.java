/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.fs.adl;


import TokenProviderType.Custom;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.junit.Test;


/**
 * This class verifies path conversion to SDK.
 */
public class TestRelativePathFormation {
    @Test
    public void testToRelativePath() throws IOException, URISyntaxException {
        AdlFileSystem fs = new AdlFileSystem();
        Configuration configuration = new Configuration();
        configuration.setEnum(AdlConfKeys.AZURE_AD_TOKEN_PROVIDER_TYPE_KEY, Custom);
        configuration.set(AdlConfKeys.AZURE_AD_TOKEN_PROVIDER_CLASS_KEY, "org.apache.hadoop.fs.adl.common.CustomMockTokenProvider");
        fs.initialize(new URI("adl://temp.account.net"), configuration);
        Assert.assertEquals("/usr", fs.toRelativeFilePath(new Path("/usr")));
        Assert.assertEquals("/usr", fs.toRelativeFilePath(new Path("adl://temp.account.net/usr")));
        // When working directory is set.
        fs.setWorkingDirectory(new Path("/a/b/"));
        Assert.assertEquals("/usr", fs.toRelativeFilePath(new Path("/usr")));
        Assert.assertEquals("/a/b/usr", fs.toRelativeFilePath(new Path("usr")));
        Assert.assertEquals("/usr", fs.toRelativeFilePath(new Path("adl://temp.account.net/usr")));
        Assert.assertEquals("/usr", fs.toRelativeFilePath(new Path("wasb://temp.account.net/usr")));
    }
}
