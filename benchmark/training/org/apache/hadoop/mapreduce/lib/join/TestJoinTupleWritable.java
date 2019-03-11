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
package org.apache.hadoop.mapreduce.lib.join;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Random;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Assert;
import org.junit.Test;


public class TestJoinTupleWritable {
    @Test
    public void testIterable() throws Exception {
        Random r = new Random();
        Writable[] writs = new Writable[]{ new BooleanWritable(r.nextBoolean()), new FloatWritable(r.nextFloat()), new FloatWritable(r.nextFloat()), new IntWritable(r.nextInt()), new LongWritable(r.nextLong()), new BytesWritable("dingo".getBytes()), new LongWritable(r.nextLong()), new IntWritable(r.nextInt()), new BytesWritable("yak".getBytes()), new IntWritable(r.nextInt()) };
        TupleWritable t = new TupleWritable(writs);
        for (int i = 0; i < 6; ++i) {
            t.setWritten(i);
        }
        verifIter(writs, t, 0);
    }

    @Test
    public void testNestedIterable() throws Exception {
        Random r = new Random();
        Writable[] writs = new Writable[]{ new BooleanWritable(r.nextBoolean()), new FloatWritable(r.nextFloat()), new FloatWritable(r.nextFloat()), new IntWritable(r.nextInt()), new LongWritable(r.nextLong()), new BytesWritable("dingo".getBytes()), new LongWritable(r.nextLong()), new IntWritable(r.nextInt()), new BytesWritable("yak".getBytes()), new IntWritable(r.nextInt()) };
        TupleWritable sTuple = makeTuple(writs);
        Assert.assertTrue("Bad count", ((writs.length) == (verifIter(writs, sTuple, 0))));
    }

    @Test
    public void testWritable() throws Exception {
        Random r = new Random();
        Writable[] writs = new Writable[]{ new BooleanWritable(r.nextBoolean()), new FloatWritable(r.nextFloat()), new FloatWritable(r.nextFloat()), new IntWritable(r.nextInt()), new LongWritable(r.nextLong()), new BytesWritable("dingo".getBytes()), new LongWritable(r.nextLong()), new IntWritable(r.nextInt()), new BytesWritable("yak".getBytes()), new IntWritable(r.nextInt()) };
        TupleWritable sTuple = makeTuple(writs);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sTuple.write(new DataOutputStream(out));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TupleWritable dTuple = new TupleWritable();
        dTuple.readFields(new DataInputStream(in));
        Assert.assertTrue("Failed to write/read tuple", sTuple.equals(dTuple));
    }

    @Test
    public void testWideWritable() throws Exception {
        Writable[] manyWrits = makeRandomWritables(131);
        TupleWritable sTuple = new TupleWritable(manyWrits);
        for (int i = 0; i < (manyWrits.length); i++) {
            if ((i % 3) == 0) {
                sTuple.setWritten(i);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sTuple.write(new DataOutputStream(out));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TupleWritable dTuple = new TupleWritable();
        dTuple.readFields(new DataInputStream(in));
        Assert.assertTrue("Failed to write/read tuple", sTuple.equals(dTuple));
        Assert.assertEquals("All tuple data has not been read from the stream", (-1), in.read());
    }

    @Test
    public void testWideWritable2() throws Exception {
        Writable[] manyWrits = makeRandomWritables(71);
        TupleWritable sTuple = new TupleWritable(manyWrits);
        for (int i = 0; i < (manyWrits.length); i++) {
            sTuple.setWritten(i);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sTuple.write(new DataOutputStream(out));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TupleWritable dTuple = new TupleWritable();
        dTuple.readFields(new DataInputStream(in));
        Assert.assertTrue("Failed to write/read tuple", sTuple.equals(dTuple));
        Assert.assertEquals("All tuple data has not been read from the stream", (-1), in.read());
    }

    /**
     * Tests a tuple writable with more than 64 values and the values set written
     * spread far apart.
     */
    @Test
    public void testSparseWideWritable() throws Exception {
        Writable[] manyWrits = makeRandomWritables(131);
        TupleWritable sTuple = new TupleWritable(manyWrits);
        for (int i = 0; i < (manyWrits.length); i++) {
            if ((i % 65) == 0) {
                sTuple.setWritten(i);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sTuple.write(new DataOutputStream(out));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TupleWritable dTuple = new TupleWritable();
        dTuple.readFields(new DataInputStream(in));
        Assert.assertTrue("Failed to write/read tuple", sTuple.equals(dTuple));
        Assert.assertEquals("All tuple data has not been read from the stream", (-1), in.read());
    }

    @Test
    public void testWideTuple() throws Exception {
        Text emptyText = new Text("Should be empty");
        Writable[] values = new Writable[64];
        Arrays.fill(values, emptyText);
        values[42] = new Text("Number 42");
        TupleWritable tuple = new TupleWritable(values);
        tuple.setWritten(42);
        for (int pos = 0; pos < (tuple.size()); pos++) {
            boolean has = tuple.has(pos);
            if (pos == 42) {
                Assert.assertTrue(has);
            } else {
                Assert.assertFalse(("Tuple position is incorrectly labelled as set: " + pos), has);
            }
        }
    }

    @Test
    public void testWideTuple2() throws Exception {
        Text emptyText = new Text("Should be empty");
        Writable[] values = new Writable[64];
        Arrays.fill(values, emptyText);
        values[9] = new Text("Number 9");
        TupleWritable tuple = new TupleWritable(values);
        tuple.setWritten(9);
        for (int pos = 0; pos < (tuple.size()); pos++) {
            boolean has = tuple.has(pos);
            if (pos == 9) {
                Assert.assertTrue(has);
            } else {
                Assert.assertFalse(("Tuple position is incorrectly labelled as set: " + pos), has);
            }
        }
    }

    /**
     * Tests that we can write more than 64 values.
     */
    @Test
    public void testWideTupleBoundary() throws Exception {
        Text emptyText = new Text("Should not be set written");
        Writable[] values = new Writable[65];
        Arrays.fill(values, emptyText);
        values[64] = new Text("Should be the only value set written");
        TupleWritable tuple = new TupleWritable(values);
        tuple.setWritten(64);
        for (int pos = 0; pos < (tuple.size()); pos++) {
            boolean has = tuple.has(pos);
            if (pos == 64) {
                Assert.assertTrue(has);
            } else {
                Assert.assertFalse(("Tuple position is incorrectly labelled as set: " + pos), has);
            }
        }
    }
}
