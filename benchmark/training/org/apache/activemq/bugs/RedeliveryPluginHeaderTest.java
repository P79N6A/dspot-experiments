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
package org.apache.activemq.bugs;


import DeliveryMode.PERSISTENT;
import Session.SESSION_TRANSACTED;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Testing if the the broker "sends" the message as expected after the redeliveryPlugin has redelivered the
 * message previously.
 */
public class RedeliveryPluginHeaderTest extends TestCase {
    private static final String TEST_QUEUE_ONE = "TEST_QUEUE_ONE";

    private static final String TEST_QUEUE_TWO = "TEST_QUEUE_TWO";

    private static final Logger LOG = LoggerFactory.getLogger(RedeliveryPluginHeaderTest.class);

    private String transportURL;

    private BrokerService broker;

    /**
     * Test
     * - consumes message from Queue1
     * - rolls back message to Queue1 and message is scheduled for redelivery to Queue1 by brokers plugin
     * - consumes message from Queue1 again
     * - sends same message to Queue2
     * - expects to consume message from Queue2 immediately
     */
    public void testSendAfterRedelivery() throws Exception {
        broker = this.createBroker(false);
        broker.start();
        broker.waitUntilStarted();
        RedeliveryPluginHeaderTest.LOG.info("***Broker started...");
        // pushed message to broker
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(((transportURL) + "?trace=true&jms.redeliveryPolicy.maximumRedeliveries=0&jms.redeliveryPolicy.preDispatchCheck=true"));
        Connection connection = factory.createConnection();
        connection.start();
        try {
            Session session = connection.createSession(true, SESSION_TRANSACTED);
            Destination destinationQ1 = session.createQueue(RedeliveryPluginHeaderTest.TEST_QUEUE_ONE);
            Destination destinationQ2 = session.createQueue(RedeliveryPluginHeaderTest.TEST_QUEUE_TWO);
            MessageProducer producerQ1 = session.createProducer(destinationQ1);
            producerQ1.setDeliveryMode(PERSISTENT);
            Message m = session.createTextMessage("testMessage");
            RedeliveryPluginHeaderTest.LOG.info("*** send message to broker...");
            producerQ1.send(m);
            session.commit();
            // consume message from Q1 and rollback to get it redelivered
            MessageConsumer consumerQ1 = session.createConsumer(destinationQ1);
            RedeliveryPluginHeaderTest.LOG.info("*** consume message from Q1 and rolled back..");
            TextMessage textMessage = ((TextMessage) (consumerQ1.receive()));
            RedeliveryPluginHeaderTest.LOG.info(("got redelivered: " + textMessage));
            TestCase.assertFalse("JMSRedelivered flag is not set", textMessage.getJMSRedelivered());
            session.rollback();
            RedeliveryPluginHeaderTest.LOG.info("*** consumed message from Q1 again and sending to Q2..");
            TextMessage textMessage2 = ((TextMessage) (consumerQ1.receive()));
            RedeliveryPluginHeaderTest.LOG.info(("got: " + textMessage2));
            session.commit();
            TestCase.assertTrue("JMSRedelivered flag is set", textMessage2.getJMSRedelivered());
            // send message to Q2 and consume from Q2
            MessageConsumer consumerQ2 = session.createConsumer(destinationQ2);
            MessageProducer producer_two = session.createProducer(destinationQ2);
            producer_two.send(textMessage2);
            session.commit();
            // Message should be available straight away on the queue_two
            Message textMessage3 = consumerQ2.receive(1000);
            TestCase.assertNotNull("should have consumed a message from TEST_QUEUE_TWO", textMessage3);
            TestCase.assertFalse("JMSRedelivered flag is not set", textMessage3.getJMSRedelivered());
            session.commit();
        } finally {
            if (connection != null) {
                connection.close();
            }
            if ((broker) != null) {
                broker.stop();
            }
        }
    }
}
