/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.yarn.server.resourcemanager.webapp.fairscheduler;


import MediaType.APPLICATION_JSON;
import YarnConfiguration.RM_SCHEDULER;
import com.google.inject.Guice;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.test.framework.WebAppDescriptor;
import javax.ws.rs.core.MediaType;
import org.apache.hadoop.http.JettyUtils;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.resourcemanager.MockRM;
import org.apache.hadoop.yarn.server.resourcemanager.ResourceManager;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.QueueManager;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.JAXBContextResolver;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.RMWebServices;
import org.apache.hadoop.yarn.webapp.GenericExceptionHandler;
import org.apache.hadoop.yarn.webapp.GuiceServletConfig;
import org.apache.hadoop.yarn.webapp.JerseyTestBase;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests RM Webservices fair scheduler resources.
 */
public class TestRMWebServicesFairScheduler extends JerseyTestBase {
    private static MockRM rm;

    private static YarnConfiguration conf;

    private static class WebServletModule extends ServletModule {
        @Override
        protected void configureServlets() {
            bind(JAXBContextResolver.class);
            bind(RMWebServices.class);
            bind(GenericExceptionHandler.class);
            TestRMWebServicesFairScheduler.conf = new YarnConfiguration();
            TestRMWebServicesFairScheduler.conf.setClass(RM_SCHEDULER, FairScheduler.class, ResourceScheduler.class);
            TestRMWebServicesFairScheduler.rm = new MockRM(TestRMWebServicesFairScheduler.conf);
            bind(ResourceManager.class).toInstance(TestRMWebServicesFairScheduler.rm);
            serve("/*").with(GuiceContainer.class);
        }
    }

    static {
        GuiceServletConfig.setInjector(Guice.createInjector(new TestRMWebServicesFairScheduler.WebServletModule()));
    }

    public TestRMWebServicesFairScheduler() {
        super(new WebAppDescriptor.Builder("org.apache.hadoop.yarn.server.resourcemanager.webapp").contextListenerClass(GuiceServletConfig.class).filterClass(GuiceFilter.class).contextPath("jersey-guice-filter").servletPath("/").build());
    }

    @Test
    public void testClusterScheduler() throws JSONException {
        WebResource r = resource();
        ClientResponse response = r.path("ws").path("v1").path("cluster").path("scheduler").accept(APPLICATION_JSON).get(ClientResponse.class);
        Assert.assertEquals((((MediaType.APPLICATION_JSON_TYPE) + "; ") + (JettyUtils.UTF_8)), response.getType().toString());
        JSONObject json = response.getEntity(JSONObject.class);
        verifyClusterScheduler(json);
    }

    @Test
    public void testClusterSchedulerSlash() throws JSONException {
        WebResource r = resource();
        ClientResponse response = r.path("ws").path("v1").path("cluster").path("scheduler/").accept(APPLICATION_JSON).get(ClientResponse.class);
        Assert.assertEquals((((MediaType.APPLICATION_JSON_TYPE) + "; ") + (JettyUtils.UTF_8)), response.getType().toString());
        JSONObject json = response.getEntity(JSONObject.class);
        verifyClusterScheduler(json);
    }

    @Test
    public void testClusterSchedulerWithSubQueues() throws JSONException {
        FairScheduler scheduler = ((FairScheduler) (getResourceScheduler()));
        QueueManager queueManager = scheduler.getQueueManager();
        // create LeafQueue
        queueManager.getLeafQueue("root.q.subqueue1", true);
        queueManager.getLeafQueue("root.q.subqueue2", true);
        WebResource r = resource();
        ClientResponse response = r.path("ws").path("v1").path("cluster").path("scheduler").accept(APPLICATION_JSON).get(ClientResponse.class);
        Assert.assertEquals((((MediaType.APPLICATION_JSON_TYPE) + "; ") + (JettyUtils.UTF_8)), response.getType().toString());
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray subQueueInfo = json.getJSONObject("scheduler").getJSONObject("schedulerInfo").getJSONObject("rootQueue").getJSONObject("childQueues").getJSONArray("queue").getJSONObject(1).getJSONObject("childQueues").getJSONArray("queue");
        // subQueueInfo is consist of subqueue1 and subqueue2 info
        Assert.assertEquals(2, subQueueInfo.length());
        // Verify 'childQueues' field is omitted from FairSchedulerLeafQueueInfo.
        try {
            subQueueInfo.getJSONObject(1).getJSONObject("childQueues");
            Assert.fail(("FairSchedulerQueueInfo should omit field 'childQueues'" + "if child queue is empty."));
        } catch (JSONException je) {
            Assert.assertEquals("JSONObject[\"childQueues\"] not found.", je.getMessage());
        }
    }
}
