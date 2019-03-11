/**
 * Copyright (C) 2018 the original author or authors.
 *
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
package org.springframework.cloud.alibaba.nacos.registry;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryAutoConfiguration;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.discovery.NacosDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;


/**
 *
 *
 * @author xiaojing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NacosAutoServiceRegistrationIpTests.TestConfig.class, properties = { "spring.application.name=myTestService1", "spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848", "spring.cloud.nacos.discovery.ip=123.123.123.123" }, webEnvironment = RANDOM_PORT)
public class NacosAutoServiceRegistrationIpTests {
    @Autowired
    private NacosRegistration registration;

    @Autowired
    private NacosAutoServiceRegistration nacosAutoServiceRegistration;

    @Autowired
    private NacosDiscoveryProperties properties;

    @Test
    public void contextLoads() throws Exception {
        Assert.assertNotNull("NacosRegistration was not created", registration);
        Assert.assertNotNull("NacosDiscoveryProperties was not created", properties);
        Assert.assertNotNull("NacosAutoServiceRegistration was not created", nacosAutoServiceRegistration);
        checkoutNacosDiscoveryServiceIP();
    }

    @Configuration
    @EnableAutoConfiguration
    @ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, NacosDiscoveryClientAutoConfiguration.class, NacosDiscoveryAutoConfiguration.class })
    public static class TestConfig {}
}
