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
package org.apache.avro;


import Type.STRING;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.avro.file.DataFileConstants;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.util.Utf8;
import org.junit.Assert;
import org.junit.Test;


public class TestDataFileCorruption {
    private static final File DIR = new File("/tmp");

    @Test
    public void testCorruptedFile() throws IOException {
        Schema schema = Schema.create(STRING);
        // Write a data file
        DataFileWriter<Utf8> w = new DataFileWriter(new org.apache.avro.generic.GenericDatumWriter(schema));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        w.create(schema, baos);
        w.append(new Utf8("apple"));
        w.append(new Utf8("banana"));
        w.sync();
        w.append(new Utf8("celery"));
        w.append(new Utf8("date"));
        long pos = w.sync();
        w.append(new Utf8("endive"));
        w.append(new Utf8("fig"));
        w.close();
        // Corrupt the input by inserting some zero bytes before the sync marker for the
        // penultimate block
        byte[] original = baos.toByteArray();
        int corruptPosition = ((int) (pos)) - (DataFileConstants.SYNC_SIZE);
        int corruptedBytes = 3;
        byte[] corrupted = new byte[(original.length) + corruptedBytes];
        System.arraycopy(original, 0, corrupted, 0, corruptPosition);
        System.arraycopy(original, corruptPosition, corrupted, (corruptPosition + corruptedBytes), ((original.length) - corruptPosition));
        File file = makeFile("corrupt");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        out.write(corrupted);
        out.close();
        // Read the data file
        DataFileReader r = new DataFileReader(file, new org.apache.avro.generic.GenericDatumReader(schema));
        Assert.assertEquals("apple", r.next().toString());
        Assert.assertEquals("banana", r.next().toString());
        long prevSync = r.previousSync();
        try {
            r.next();
            Assert.fail("Corrupt block should throw exception");
        } catch (AvroRuntimeException e) {
            Assert.assertEquals("Invalid sync!", e.getCause().getMessage());
        }
        r.sync(prevSync);// go to sync point after previous successful one

        Assert.assertEquals("endive", r.next().toString());
        Assert.assertEquals("fig", r.next().toString());
        Assert.assertFalse(r.hasNext());
    }
}
