/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.facebook;


import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;


/**
 * This test makes sure PowerMock integration works.
 */
@PrepareForTest({ FacebookSdk.class })
public class PowerMockIntegrationTest extends FacebookPowerMockTestCase {
    @Test
    public void testStaticMethodOverrides() {
        mockStatic(FacebookSdk.class);
        String applicationId = "1234";
        when(FacebookSdk.getApplicationId()).thenReturn(applicationId);
        Assert.assertEquals(applicationId, FacebookSdk.getApplicationId());
        String clientToken = "clienttoken";
        when(FacebookSdk.getClientToken()).thenReturn(clientToken);
        Assert.assertEquals(clientToken, FacebookSdk.getClientToken());
    }
}
