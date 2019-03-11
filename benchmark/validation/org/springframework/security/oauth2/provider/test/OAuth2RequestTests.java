/**
 * Copyright 20013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.security.oauth2.provider.test;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Request;


/**
 *
 *
 * @author Dave Syer
 */
public class OAuth2RequestTests {
    private Map<String, String> parameters;

    @Test
    public void testBaseMethods() throws Exception {
        parameters.put("response_type", "token");
        OAuth2Request authorizationRequest = createFromParameters(parameters);
        Assert.assertEquals("theClient", authorizationRequest.getClientId());
    }

    @Test
    public void testImplicitGrantType() throws Exception {
        parameters.put("response_type", "token");
        OAuth2Request authorizationRequest = createFromParameters(parameters);
        Assert.assertEquals("implicit", authorizationRequest.getGrantType());
    }

    @Test
    public void testOtherGrantType() throws Exception {
        parameters.put("grant_type", "password");
        OAuth2Request authorizationRequest = createFromParameters(parameters);
        Assert.assertEquals("password", authorizationRequest.getGrantType());
    }

    // gh-724
    @Test
    public void testResourceIdsConstructorAssignment() {
        Set<String> resourceIds = new HashSet<String>(Arrays.asList("resourceId-1", "resourceId-2"));
        OAuth2Request request = new OAuth2Request(Collections.<String, String>emptyMap(), "clientId", Collections.<GrantedAuthority>emptyList(), false, Collections.<String>emptySet(), resourceIds, "redirectUri", Collections.<String>emptySet(), Collections.<String, Serializable>emptyMap());
        Assert.assertNotSame("resourceIds are the same", resourceIds, request.getResourceIds());
    }
}
