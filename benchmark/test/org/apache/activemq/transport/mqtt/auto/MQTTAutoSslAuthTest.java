/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.transport.mqtt.auto;


import org.apache.activemq.transport.mqtt.MQTTTestSupport;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class MQTTAutoSslAuthTest extends MQTTTestSupport {
    private final String protocol;

    private boolean hasCertificate = false;

    /**
     *
     *
     * @param isNio
     * 		
     */
    public MQTTAutoSslAuthTest(String protocol) {
        this.protocol = protocol;
        protocolConfig = "transport.needClientAuth=true";
    }

    @Test(timeout = 60 * 1000)
    public void testMQTT311Connection() throws Exception {
        MQTT mqtt = createMQTTConnection();
        mqtt.setClientId("foo");
        mqtt.setVersion("3.1.1");
        final BlockingConnection connection = mqtt.blockingConnection();
        connection.connect();
        connection.disconnect();
        Assert.assertTrue(hasCertificate);
    }
}
