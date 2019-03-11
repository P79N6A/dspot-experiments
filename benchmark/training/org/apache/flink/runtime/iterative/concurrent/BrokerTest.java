/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.runtime.iterative.concurrent;


import java.util.Random;
import java.util.concurrent.Callable;
import org.apache.flink.util.Preconditions;
import org.junit.Test;


/**
 * Tests for {@link Broker}.
 */
public class BrokerTest {
    @Test
    public void mediation() throws Exception {
        Random random = new Random();
        for (int n = 0; n < 20; n++) {
            mediate(((random.nextInt(10)) + 1));
        }
    }

    class IterationHead implements Callable<StringPair> {
        private final Random random;

        private final Broker<String> broker;

        private final String key;

        private final String value;

        IterationHead(Broker<String> broker, Integer key, String value) {
            this.broker = broker;
            this.key = String.valueOf(key);
            this.value = value;
            random = new Random();
        }

        @Override
        public StringPair call() throws Exception {
            Thread.sleep(random.nextInt(10));
            // System.out.println("Head " + key + " hands in " + value);
            broker.handIn(key, value);
            Thread.sleep(random.nextInt(10));
            return null;
        }
    }

    class IterationTail implements Callable<StringPair> {
        private final Random random;

        private final Broker<String> broker;

        private final String key;

        IterationTail(Broker<String> broker, Integer key) {
            this.broker = broker;
            this.key = String.valueOf(key);
            random = new Random();
        }

        @Override
        public StringPair call() throws Exception {
            Thread.sleep(random.nextInt(10));
            // System.out.println("Tail " + key + " asks for handover");
            String value = broker.getAndRemove(key);
            // System.out.println("Tail " + key + " received " + value);
            Preconditions.checkNotNull(value);
            return new StringPair(key, value);
        }
    }
}
