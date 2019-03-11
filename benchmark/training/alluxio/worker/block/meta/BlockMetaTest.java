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
package alluxio.worker.block.meta;


import alluxio.util.io.BufferUtils;
import alluxio.util.io.PathUtils;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 * Unit tests for {@link BlockMeta}.
 */
public class BlockMetaTest {
    private static final long TEST_SESSION_ID = 2;

    private static final long TEST_BLOCK_ID = 9;

    private static final long TEST_BLOCK_SIZE = 100;

    private static final int TEST_TIER_ORDINAL = 0;

    private static final String TEST_TIER_ALIAS = "MEM";

    private static final long[] TEST_TIER_CAPACITY_BYTES = new long[]{ 100 };

    private static final String TEST_WORKER_DIR = "testworker";

    private BlockMeta mBlockMeta;

    private TempBlockMeta mTempBlockMeta;

    private String mTestDirPath;

    private String mTestBlockDirPath;

    /**
     * Rule to create a new temporary folder during each test.
     */
    @Rule
    public TemporaryFolder mFolder = new TemporaryFolder();

    /**
     * Tests the {@link BlockMeta#getBlockSize()} method.
     */
    @Test
    public void getBlockSize() throws IOException {
        // With the block file not really existing, expect committed block size to be zero.
        mBlockMeta = new BlockMeta(mTempBlockMeta);
        Assert.assertEquals(0, mBlockMeta.getBlockSize());
        // With the block file partially written, expect committed block size equals real file size.
        byte[] buf = BufferUtils.getIncreasingByteArray((((int) (BlockMetaTest.TEST_BLOCK_SIZE)) - 1));
        BufferUtils.writeBufferToFile(mTempBlockMeta.getCommitPath(), buf);
        mBlockMeta = new BlockMeta(mTempBlockMeta);
        Assert.assertEquals(((BlockMetaTest.TEST_BLOCK_SIZE) - 1), mBlockMeta.getBlockSize());
        // With the block file fully written, expect committed block size equals target block size.
        buf = BufferUtils.getIncreasingByteArray(((int) (BlockMetaTest.TEST_BLOCK_SIZE)));
        BufferUtils.writeBufferToFile(mTempBlockMeta.getCommitPath(), buf);
        mBlockMeta = new BlockMeta(mTempBlockMeta);
        Assert.assertEquals(BlockMetaTest.TEST_BLOCK_SIZE, mBlockMeta.getBlockSize());
    }

    /**
     * Tests the {@link BlockMeta#getPath()} method.
     */
    @Test
    public void getPath() {
        mBlockMeta = new BlockMeta(mTempBlockMeta);
        Assert.assertEquals(PathUtils.concatPath(mTestBlockDirPath, BlockMetaTest.TEST_BLOCK_ID), mBlockMeta.getPath());
    }
}
