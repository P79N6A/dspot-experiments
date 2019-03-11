/**
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights
 * Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is
 * distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either
 * express or implied. See the License for the specific language
 * governing
 * permissions and limitations under the License.
 */
package com.amazonaws.event;


import ProgressEventType.CLIENT_REQUEST_STARTED_EVENT;
import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressListener.ExceptionReporter;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;


public class ProgressListenerTest {
    // Listener is executed synchronously
    @Test
    public void throwErrorWithSyncListener() {
        ProgressListener syncListener = new SyncProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                throw new Error();
            }
        };
        ExceptionReporter reporter = ExceptionReporter.wrap(syncListener);
        SDKProgressPublisher.publishProgress(syncListener, CLIENT_REQUEST_STARTED_EVENT);
        Assert.assertNull(reporter.getCause());
        SDKProgressPublisher.publishProgress(reporter, CLIENT_REQUEST_STARTED_EVENT);
        Assert.assertNotNull(reporter.getCause());
        try {
            reporter.throwExceptionIfAny();
            Assert.fail();
        } catch (AmazonClientException ex) {
            // expected
        }
    }

    // Listener is executed asynchronously
    @Test
    public void throwErrorWithAsyncListener() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        ProgressListener asyncListener = new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                throw new Error();
            }
        };
        ExceptionReporter reporter = new ExceptionReporter(asyncListener) {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                super.progressChanged(progressEvent);
                latch.countDown();
            }
        };
        SDKProgressPublisher.publishProgress(reporter, CLIENT_REQUEST_STARTED_EVENT);
        latch.await();
        Assert.assertNotNull(reporter.getCause());
        try {
            reporter.throwExceptionIfAny();
            Assert.fail();
        } catch (AmazonClientException ex) {
            // expected
        }
    }
}
