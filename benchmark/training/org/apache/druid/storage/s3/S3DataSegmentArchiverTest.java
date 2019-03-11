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
package org.apache.druid.storage.s3;


import S3DataSegmentPuller.BUCKET;
import S3DataSegmentPuller.KEY;
import S3StorageDruidModule.SCHEME;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.apache.druid.java.util.common.Intervals;
import org.apache.druid.timeline.DataSegment;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;


public class S3DataSegmentArchiverTest {
    private static final ObjectMapper MAPPER = new DefaultObjectMapper().setInjectableValues(new InjectableValues() {
        @Override
        public Object findInjectableValue(Object valueId, DeserializationContext ctxt, BeanProperty forProperty, Object beanInstance) {
            return S3DataSegmentArchiverTest.PULLER;
        }
    }).registerModule(new SimpleModule("s3-archive-test-module").registerSubtypes(S3LoadSpec.class));

    private static final S3DataSegmentArchiverConfig ARCHIVER_CONFIG = new S3DataSegmentArchiverConfig() {
        @Override
        public String getArchiveBucket() {
            return "archive_bucket";
        }

        @Override
        public String getArchiveBaseKey() {
            return "archive_base_key";
        }
    };

    private static final S3DataSegmentPusherConfig PUSHER_CONFIG = new S3DataSegmentPusherConfig();

    private static final ServerSideEncryptingAmazonS3 S3_SERVICE = new ServerSideEncryptingAmazonS3(EasyMock.createStrictMock(AmazonS3Client.class), new NoopServerSideEncryption());

    private static final S3DataSegmentPuller PULLER = new S3DataSegmentPuller(S3DataSegmentArchiverTest.S3_SERVICE);

    private static final DataSegment SOURCE_SEGMENT = DataSegment.builder().binaryVersion(1).dataSource("dataSource").dimensions(ImmutableList.of()).interval(Intervals.of("2015/2016")).version("version").loadSpec(ImmutableMap.of("type", SCHEME, BUCKET, "source_bucket", KEY, "source_key")).build();

    @Test
    public void testSimpleArchive() throws Exception {
        final DataSegment archivedSegment = S3DataSegmentArchiverTest.SOURCE_SEGMENT.withLoadSpec(ImmutableMap.of("type", SCHEME, BUCKET, S3DataSegmentArchiverTest.ARCHIVER_CONFIG.getArchiveBucket(), KEY, ((S3DataSegmentArchiverTest.ARCHIVER_CONFIG.getArchiveBaseKey()) + "archived")));
        final S3DataSegmentArchiver archiver = new S3DataSegmentArchiver(S3DataSegmentArchiverTest.MAPPER, S3DataSegmentArchiverTest.S3_SERVICE, S3DataSegmentArchiverTest.ARCHIVER_CONFIG, S3DataSegmentArchiverTest.PUSHER_CONFIG) {
            @Override
            public DataSegment move(DataSegment segment, Map<String, Object> targetLoadSpec) {
                return archivedSegment;
            }
        };
        Assert.assertEquals(archivedSegment, archiver.archive(S3DataSegmentArchiverTest.SOURCE_SEGMENT));
    }

    @Test
    public void testSimpleArchiveDoesntMove() throws Exception {
        final S3DataSegmentArchiver archiver = new S3DataSegmentArchiver(S3DataSegmentArchiverTest.MAPPER, S3DataSegmentArchiverTest.S3_SERVICE, S3DataSegmentArchiverTest.ARCHIVER_CONFIG, S3DataSegmentArchiverTest.PUSHER_CONFIG) {
            @Override
            public DataSegment move(DataSegment segment, Map<String, Object> targetLoadSpec) {
                return S3DataSegmentArchiverTest.SOURCE_SEGMENT;
            }
        };
        Assert.assertNull(archiver.archive(S3DataSegmentArchiverTest.SOURCE_SEGMENT));
    }

    @Test
    public void testSimpleRestore() throws Exception {
        final DataSegment archivedSegment = S3DataSegmentArchiverTest.SOURCE_SEGMENT.withLoadSpec(ImmutableMap.of("type", SCHEME, BUCKET, S3DataSegmentArchiverTest.ARCHIVER_CONFIG.getArchiveBucket(), KEY, ((S3DataSegmentArchiverTest.ARCHIVER_CONFIG.getArchiveBaseKey()) + "archived")));
        final S3DataSegmentArchiver archiver = new S3DataSegmentArchiver(S3DataSegmentArchiverTest.MAPPER, S3DataSegmentArchiverTest.S3_SERVICE, S3DataSegmentArchiverTest.ARCHIVER_CONFIG, S3DataSegmentArchiverTest.PUSHER_CONFIG) {
            @Override
            public DataSegment move(DataSegment segment, Map<String, Object> targetLoadSpec) {
                return archivedSegment;
            }
        };
        Assert.assertEquals(archivedSegment, archiver.restore(S3DataSegmentArchiverTest.SOURCE_SEGMENT));
    }

    @Test
    public void testSimpleRestoreDoesntMove() throws Exception {
        final S3DataSegmentArchiver archiver = new S3DataSegmentArchiver(S3DataSegmentArchiverTest.MAPPER, S3DataSegmentArchiverTest.S3_SERVICE, S3DataSegmentArchiverTest.ARCHIVER_CONFIG, S3DataSegmentArchiverTest.PUSHER_CONFIG) {
            @Override
            public DataSegment move(DataSegment segment, Map<String, Object> targetLoadSpec) {
                return S3DataSegmentArchiverTest.SOURCE_SEGMENT;
            }
        };
        Assert.assertNull(archiver.restore(S3DataSegmentArchiverTest.SOURCE_SEGMENT));
    }
}
