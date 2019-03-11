/**
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.spring;


import HttpStatus.OK;
import MediaType.PLAIN_TEXT_UTF_8;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.metric.MoreMeters;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.PathMapping;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.logging.LoggingService;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ArmeriaMeterBindersConfigurationTest.TestConfiguration.class)
@ActiveProfiles({ "local", "autoConfTest" })
@DirtiesContext
public class ArmeriaMeterBindersConfigurationTest {
    @SpringBootApplication
    public static class TestConfiguration {
        @Bean
        public HttpServiceRegistrationBean okService() {
            return new HttpServiceRegistrationBean().setServiceName("okService").setService(new AbstractHttpService() {
                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) {
                    return HttpResponse.of(OK, PLAIN_TEXT_UTF_8, "ok");
                }
            }).setPathMapping(PathMapping.ofExact("/ok")).setDecorators(LoggingService.newDecorator());
        }
    }

    @Rule
    public TestRule globalTimeout = new DisableOnDebug(new Timeout(10, TimeUnit.SECONDS));

    @Inject
    private MeterRegistry registry;

    @Test
    public void testDefaultMetrics() throws Exception {
        final Map<String, Double> measurements = MoreMeters.measureAll(registry);
        assertThat(measurements).containsKeys("jvm.buffer.count#value{id=direct}", "jvm.classes.loaded#value", "jvm.threads.daemon#value", "logback.events#count{level=debug}", "process.uptime#value", "system.cpu.count#value");
        // Use prefix-matching for meter IDs because JVM memory meters differ between JVM versions.
        assertThat(measurements).hasKeySatisfying(new org.assertj.core.api.Condition(( key) -> key.startsWith("jvm.memory.max#value{area=nonheap,id="), "MeterRegistry must contain JVM memory meters"));
        final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        boolean hasOpenFdCount = false;
        try {
            os.getClass().getDeclaredMethod("getOpenFileDescriptorCount");
            hasOpenFdCount = true;
        } catch (Exception ignored) {
            // Not supported
        }
        if (hasOpenFdCount) {
            assertThat(measurements).containsKeys("process.files.open#value");
        }
    }
}
