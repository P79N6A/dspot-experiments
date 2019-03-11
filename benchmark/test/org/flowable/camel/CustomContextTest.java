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
package org.flowable.camel;


import FlowableProducer.PROCESS_ID_PROPERTY;
import java.util.Collections;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.flowable.engine.test.Deployment;
import org.flowable.spring.impl.test.SpringFlowableTestCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@Tag("camel")
@ContextConfiguration("classpath:generic-camel-flowable-context.xml")
public class CustomContextTest extends SpringFlowableTestCase {
    @Autowired
    protected CamelContext camelContext;

    protected MockEndpoint service1;

    protected MockEndpoint service2;

    @Test
    @Deployment(resources = { "process/custom.bpmn20.xml" })
    public void testRunProcess() throws Exception {
        CamelContext ctx = applicationContext.getBean(CamelContext.class);
        ProducerTemplate tpl = ctx.createProducerTemplate();
        service1.expectedBodiesReceived("ala");
        Exchange exchange = ctx.getEndpoint("direct:start").createExchange();
        exchange.getIn().setBody(Collections.singletonMap("var1", "ala"));
        tpl.send("direct:start", exchange);
        String instanceId = ((String) (exchange.getProperty("PROCESS_ID_PROPERTY")));
        tpl.sendBodyAndProperty("direct:receive", null, PROCESS_ID_PROPERTY, instanceId);
        assertProcessEnded(instanceId);
        service1.assertIsSatisfied();
        @SuppressWarnings("rawtypes")
        Map m = service2.getExchanges().get(0).getIn().getBody(Map.class);
        assertEquals("ala", m.get("var1"));
        assertEquals("var2", m.get("var2"));
    }
}
