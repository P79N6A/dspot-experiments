/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.connect.transforms;


import Schema.Type.STRUCT;
import java.util.Collections;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.Assert;
import org.junit.Test;


public class HoistFieldTest {
    private final HoistField<SinkRecord> xform = new HoistField.Key<>();

    @Test
    public void schemaless() {
        xform.configure(Collections.singletonMap("field", "magic"));
        final SinkRecord record = new SinkRecord("test", 0, null, 42, null, null, 0);
        final SinkRecord transformedRecord = xform.apply(record);
        Assert.assertNull(transformedRecord.keySchema());
        Assert.assertEquals(Collections.singletonMap("magic", 42), transformedRecord.key());
    }

    @Test
    public void withSchema() {
        xform.configure(Collections.singletonMap("field", "magic"));
        final SinkRecord record = new SinkRecord("test", 0, Schema.INT32_SCHEMA, 42, null, null, 0);
        final SinkRecord transformedRecord = xform.apply(record);
        Assert.assertEquals(STRUCT, transformedRecord.keySchema().type());
        Assert.assertEquals(record.keySchema(), transformedRecord.keySchema().field("magic").schema());
        Assert.assertEquals(42, get("magic"));
    }
}
