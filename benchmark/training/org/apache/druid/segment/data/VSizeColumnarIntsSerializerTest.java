/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.segment.data;


import java.util.Random;
import org.apache.druid.segment.writeout.OffHeapMemorySegmentWriteOutMedium;
import org.apache.druid.segment.writeout.SegmentWriteOutMedium;
import org.junit.Test;


public class VSizeColumnarIntsSerializerTest {
    private static final int[] MAX_VALUES = new int[]{ 255, 65535, 16777215, 268435455 };

    private final SegmentWriteOutMedium segmentWriteOutMedium = new OffHeapMemorySegmentWriteOutMedium();

    private final Random rand = new Random(0);

    private int[] vals;

    @Test
    public void testAdd() throws Exception {
        for (int maxValue : VSizeColumnarIntsSerializerTest.MAX_VALUES) {
            generateVals(((rand.nextInt(100)) + 10), maxValue);
            checkSerializedSizeAndData();
        }
    }

    @Test
    public void testWriteEmpty() throws Exception {
        vals = new int[0];
        checkSerializedSizeAndData();
    }
}
