/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.protocol.protobuf.v1.operations;


import BasicTypes.EncodedValue;
import BasicTypes.EncodedValue.ValueCase.STRINGRESULT;
import RegionAPI.GetRequest;
import RegionAPI.GetResponse;
import org.apache.geode.cache.RegionDestroyedException;
import org.apache.geode.internal.protocol.TestExecutionContext;
import org.apache.geode.internal.protocol.protobuf.v1.BasicTypes;
import org.apache.geode.internal.protocol.protobuf.v1.ProtobufRequestUtilities;
import org.apache.geode.internal.protocol.protobuf.v1.ProtobufSerializationService;
import org.apache.geode.internal.protocol.protobuf.v1.RegionAPI;
import org.apache.geode.internal.protocol.protobuf.v1.Result;
import org.apache.geode.internal.protocol.protobuf.v1.Success;
import org.apache.geode.internal.protocol.protobuf.v1.serialization.exception.DecodingException;
import org.apache.geode.test.junit.categories.ClientServerTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


@Category({ ClientServerTest.class })
public class GetRequestOperationHandlerJUnitTest extends OperationHandlerJUnitTest {
    private final String TEST_KEY = "my key";

    private final String TEST_VALUE = "my value";

    private final String TEST_REGION = "test region";

    private final String MISSING_REGION = "missing region";

    private final String MISSING_KEY = "missing key";

    private final String NULLED_KEY = "nulled key";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void processReturnsTheEncodedValueFromTheRegion() throws Exception {
        RegionAPI.GetRequest getRequest = generateTestRequest(false, false, false);
        Result result = operationHandler.process(serializationService, getRequest, TestExecutionContext.getNoAuthCacheExecutionContext(cacheStub));
        Assert.assertTrue((result instanceof Success));
        RegionAPI.GetResponse response = ((RegionAPI.GetResponse) (result.getMessage()));
        Assert.assertEquals(STRINGRESULT, response.getResult().getValueCase());
        String actualValue = response.getResult().getStringResult();
        Assert.assertEquals(TEST_VALUE, actualValue);
    }

    @Test
    public void processReturnsUnsucessfulResponseForInvalidRegion() throws Exception {
        RegionAPI.GetRequest getRequest = generateTestRequest(true, false, false);
        expectedException.expect(RegionDestroyedException.class);
        Result response = operationHandler.process(serializationService, getRequest, TestExecutionContext.getNoAuthCacheExecutionContext(cacheStub));
    }

    @Test
    public void processReturnsKeyNotFoundWhenKeyIsNotFound() throws Exception {
        RegionAPI.GetRequest getRequest = generateTestRequest(false, true, false);
        Result response = operationHandler.process(serializationService, getRequest, TestExecutionContext.getNoAuthCacheExecutionContext(cacheStub));
        Assert.assertTrue((response instanceof Success));
    }

    @Test
    public void processReturnsLookupFailureWhenKeyFoundWithNoValue() throws Exception {
        RegionAPI.GetRequest getRequest = generateTestRequest(false, false, true);
        Result<RegionAPI.GetResponse> response = operationHandler.process(serializationService, getRequest, TestExecutionContext.getNoAuthCacheExecutionContext(cacheStub));
        Assert.assertTrue((response instanceof Success));
        Assert.assertNull(serializationService.decode(response.getMessage().getResult()));
    }

    @Test(expected = DecodingException.class)
    public void processThrowsExceptionWhenUnableToDecodeRequest() throws Exception {
        Exception exception = new DecodingException("error finding codec for type");
        ProtobufSerializationService serializationServiceStub = Mockito.mock(ProtobufSerializationService.class);
        Mockito.when(serializationServiceStub.decode(ArgumentMatchers.any())).thenThrow(exception);
        BasicTypes.EncodedValue encodedKey = EncodedValue.newBuilder().setJsonObjectResult("{\"someKey\":\"someValue\"}").build();
        RegionAPI.GetRequest getRequest = ProtobufRequestUtilities.createGetRequest(TEST_REGION, encodedKey).getGetRequest();
        operationHandler.process(serializationServiceStub, getRequest, TestExecutionContext.getNoAuthCacheExecutionContext(cacheStub));
    }
}
