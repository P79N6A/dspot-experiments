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
package org.apache.hadoop.ozone.s3.endpoint;


import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.hadoop.ozone.client.OzoneBucket;
import org.apache.hadoop.ozone.client.OzoneClientStub;
import org.apache.hadoop.ozone.client.OzoneVolume;
import org.apache.hadoop.ozone.s3.util.S3Consts;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * This class tests Initiate Multipart Upload request.
 */
public class TestInitiateMultipartUpload {
    @Test
    public void testInitiateMultipartUpload() throws Exception {
        String bucket = "s3bucket";
        String key = "key1";
        OzoneClientStub client = new OzoneClientStub();
        getObjectStore().createS3Bucket("ozone", bucket);
        String volumeName = getObjectStore().getOzoneVolumeName(bucket);
        OzoneVolume volume = getObjectStore().getVolume(volumeName);
        OzoneBucket ozoneBucket = volume.getBucket("s3bucket");
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.getHeaderString(S3Consts.STORAGE_CLASS_HEADER)).thenReturn("STANDARD");
        ObjectEndpoint rest = new ObjectEndpoint();
        rest.setHeaders(headers);
        rest.setClient(client);
        Response response = rest.multipartUpload(bucket, key, "", "", null);
        Assert.assertEquals(response.getStatus(), 200);
        MultipartUploadInitiateResponse multipartUploadInitiateResponse = ((MultipartUploadInitiateResponse) (response.getEntity()));
        Assert.assertNotNull(multipartUploadInitiateResponse.getUploadID());
        String uploadID = multipartUploadInitiateResponse.getUploadID();
        // Calling again should return different uploadID.
        response = rest.multipartUpload(bucket, key, "", "", null);
        Assert.assertEquals(response.getStatus(), 200);
        multipartUploadInitiateResponse = ((MultipartUploadInitiateResponse) (response.getEntity()));
        Assert.assertNotNull(multipartUploadInitiateResponse.getUploadID());
        Assert.assertNotEquals(multipartUploadInitiateResponse.getUploadID(), uploadID);
    }
}
