/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.rest.service.api.identity;


import RestUrls.URL_GROUP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.flowable.idm.api.Group;
import org.flowable.rest.service.BaseSpringRestTestCase;
import org.flowable.rest.service.api.RestUrls;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Frederik Heremans
 */
public class GroupResourceTest extends BaseSpringRestTestCase {
    /**
     * Test getting a single group.
     */
    @Test
    public void testGetGroup() throws Exception {
        try {
            Group testGroup = identityService.newGroup("testgroup");
            testGroup.setName("Test group");
            testGroup.setType("Test type");
            identityService.saveGroup(testGroup);
            CloseableHttpResponse response = executeRequest(new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "testgroup")))), HttpStatus.SC_OK);
            JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
            closeResponse(response);
            Assert.assertNotNull(responseNode);
            Assert.assertEquals("testgroup", responseNode.get("id").textValue());
            Assert.assertEquals("Test group", responseNode.get("name").textValue());
            Assert.assertEquals("Test type", responseNode.get("type").textValue());
            Assert.assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(URL_GROUP, testGroup.getId())));
            Group createdGroup = identityService.createGroupQuery().groupId("testgroup").singleResult();
            Assert.assertNotNull(createdGroup);
            Assert.assertEquals("Test group", createdGroup.getName());
            Assert.assertEquals("Test type", createdGroup.getType());
        } finally {
            try {
                identityService.deleteGroup("testgroup");
            } catch (Throwable ignore) {
                // Ignore, since the group may not have been created in the test
                // or already deleted
            }
        }
    }

    /**
     * Test getting an unexisting group.
     */
    @Test
    public void testGetUnexistingGroup() throws Exception {
        closeResponse(executeRequest(new HttpGet(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "unexisting")))), HttpStatus.SC_NOT_FOUND));
    }

    /**
     * Test deleting a single group.
     */
    @Test
    public void testDeleteGroup() throws Exception {
        try {
            Group testGroup = identityService.newGroup("testgroup");
            testGroup.setName("Test group");
            testGroup.setType("Test type");
            identityService.saveGroup(testGroup);
            closeResponse(executeRequest(new HttpDelete(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "testgroup")))), HttpStatus.SC_NO_CONTENT));
            Assert.assertNull(identityService.createGroupQuery().groupId("testgroup").singleResult());
        } finally {
            try {
                identityService.deleteGroup("testgroup");
            } catch (Throwable ignore) {
                // Ignore, since the group may not have been created in the test
                // or already deleted
            }
        }
    }

    /**
     * Test deleting an unexisting group.
     */
    @Test
    public void testDeleteUnexistingGroup() throws Exception {
        closeResponse(executeRequest(new HttpDelete(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "unexisting")))), HttpStatus.SC_NOT_FOUND));
    }

    /**
     * Test updating a single group.
     */
    @Test
    public void testUpdateGroup() throws Exception {
        try {
            Group testGroup = identityService.newGroup("testgroup");
            testGroup.setName("Test group");
            testGroup.setType("Test type");
            identityService.saveGroup(testGroup);
            ObjectNode requestNode = objectMapper.createObjectNode();
            requestNode.put("name", "Updated group");
            requestNode.put("type", "Updated type");
            HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "testgroup"))));
            httpPut.setEntity(new StringEntity(requestNode.toString()));
            CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
            JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
            closeResponse(response);
            Assert.assertNotNull(responseNode);
            Assert.assertEquals("testgroup", responseNode.get("id").textValue());
            Assert.assertEquals("Updated group", responseNode.get("name").textValue());
            Assert.assertEquals("Updated type", responseNode.get("type").textValue());
            Assert.assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(URL_GROUP, testGroup.getId())));
            Group createdGroup = identityService.createGroupQuery().groupId("testgroup").singleResult();
            Assert.assertNotNull(createdGroup);
            Assert.assertEquals("Updated group", createdGroup.getName());
            Assert.assertEquals("Updated type", createdGroup.getType());
        } finally {
            try {
                identityService.deleteGroup("testgroup");
            } catch (Throwable ignore) {
                // Ignore, since the group may not have been created in the test
                // or already deleted
            }
        }
    }

    /**
     * Test updating a single group passing in no fields in the json, user should remain unchanged.
     */
    @Test
    public void testUpdateGroupNoFields() throws Exception {
        try {
            Group testGroup = identityService.newGroup("testgroup");
            testGroup.setName("Test group");
            testGroup.setType("Test type");
            identityService.saveGroup(testGroup);
            ObjectNode requestNode = objectMapper.createObjectNode();
            HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "testgroup"))));
            httpPut.setEntity(new StringEntity(requestNode.toString()));
            CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
            JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
            closeResponse(response);
            Assert.assertNotNull(responseNode);
            Assert.assertEquals("testgroup", responseNode.get("id").textValue());
            Assert.assertEquals("Test group", responseNode.get("name").textValue());
            Assert.assertEquals("Test type", responseNode.get("type").textValue());
            Assert.assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(URL_GROUP, testGroup.getId())));
            Group createdGroup = identityService.createGroupQuery().groupId("testgroup").singleResult();
            Assert.assertNotNull(createdGroup);
            Assert.assertEquals("Test group", createdGroup.getName());
            Assert.assertEquals("Test type", createdGroup.getType());
        } finally {
            try {
                identityService.deleteGroup("testgroup");
            } catch (Throwable ignore) {
                // Ignore, since the group may not have been created in the test
                // or already deleted
            }
        }
    }

    /**
     * Test updating a single user passing in null-values.
     */
    @Test
    public void testUpdateGroupNullFields() throws Exception {
        try {
            Group testGroup = identityService.newGroup("testgroup");
            testGroup.setName("Test group");
            testGroup.setType("Test type");
            identityService.saveGroup(testGroup);
            ObjectNode requestNode = objectMapper.createObjectNode();
            requestNode.set("name", null);
            requestNode.set("type", null);
            HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "testgroup"))));
            httpPut.setEntity(new StringEntity(requestNode.toString()));
            CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
            JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
            closeResponse(response);
            Assert.assertNotNull(responseNode);
            Assert.assertEquals("testgroup", responseNode.get("id").textValue());
            Assert.assertNull(responseNode.get("name").textValue());
            Assert.assertNull(responseNode.get("type").textValue());
            Assert.assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(URL_GROUP, testGroup.getId())));
            Group createdGroup = identityService.createGroupQuery().groupId("testgroup").singleResult();
            Assert.assertNotNull(createdGroup);
            Assert.assertNull(createdGroup.getName());
            Assert.assertNull(createdGroup.getType());
        } finally {
            try {
                identityService.deleteGroup("testgroup");
            } catch (Throwable ignore) {
                // Ignore, since the group may not have been created in the test
                // or already deleted
            }
        }
    }

    /**
     * Test updating an unexisting group.
     */
    @Test
    public void testUpdateUnexistingGroup() throws Exception {
        HttpPut httpPut = new HttpPut(((BaseSpringRestTestCase.SERVER_URL_PREFIX) + (RestUrls.createRelativeResourceUrl(URL_GROUP, "unexisting"))));
        httpPut.setEntity(new StringEntity(objectMapper.createObjectNode().toString()));
        closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
    }
}
