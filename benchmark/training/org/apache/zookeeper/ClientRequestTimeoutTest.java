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
package org.apache.zookeeper;


import CreateMode.PERSISTENT;
import CreateMode.PERSISTENT_SEQUENTIAL;
import Ids.OPEN_ACL_UNSAFE;
import KeeperException.Code.REQUESTTIMEOUT;
import ZooDefs.OpCode;
import java.io.IOException;
import org.apache.zookeeper.client.HostProvider;
import org.apache.zookeeper.server.quorum.QuorumPeerTestBase;
import org.apache.zookeeper.test.ClientBase;
import org.junit.Assert;
import org.junit.Test;


public class ClientRequestTimeoutTest extends QuorumPeerTestBase {
    private static final int SERVER_COUNT = 3;

    private boolean dropPacket = false;

    private int dropPacketType = OpCode.create;

    @Test(timeout = 120000)
    public void testClientRequestTimeout() throws Exception {
        int requestTimeOut = 15000;
        System.setProperty("zookeeper.request.timeout", Integer.toString(requestTimeOut));
        final int[] clientPorts = new int[ClientRequestTimeoutTest.SERVER_COUNT];
        StringBuilder sb = new StringBuilder();
        String server;
        for (int i = 0; i < (ClientRequestTimeoutTest.SERVER_COUNT); i++) {
            clientPorts[i] = PortAssignment.unique();
            server = (((((("server." + i) + "=127.0.0.1:") + (PortAssignment.unique())) + ":") + (PortAssignment.unique())) + ":participant;127.0.0.1:") + (clientPorts[i]);
            sb.append((server + "\n"));
        }
        String currentQuorumCfgSection = sb.toString();
        QuorumPeerTestBase.MainThread[] mt = new QuorumPeerTestBase.MainThread[ClientRequestTimeoutTest.SERVER_COUNT];
        for (int i = 0; i < (ClientRequestTimeoutTest.SERVER_COUNT); i++) {
            mt[i] = new QuorumPeerTestBase.MainThread(i, clientPorts[i], currentQuorumCfgSection, false);
            mt[i].start();
        }
        // ensure server started
        for (int i = 0; i < (ClientRequestTimeoutTest.SERVER_COUNT); i++) {
            Assert.assertTrue((("waiting for server " + i) + " being up"), ClientBase.waitForServerUp(("127.0.0.1:" + (clientPorts[i])), ClientBase.CONNECTION_TIMEOUT));
        }
        ClientBase.CountdownWatcher watch1 = new ClientBase.CountdownWatcher();
        ClientRequestTimeoutTest.CustomZooKeeper zk = new ClientRequestTimeoutTest.CustomZooKeeper(getCxnString(clientPorts), ClientBase.CONNECTION_TIMEOUT, watch1);
        watch1.waitForConnected(ClientBase.CONNECTION_TIMEOUT);
        String data = "originalData";
        // lets see one successful operation
        zk.create("/clientHang1", data.getBytes(), OPEN_ACL_UNSAFE, PERSISTENT_SEQUENTIAL);
        // now make environment for client hang
        dropPacket = true;
        dropPacketType = OpCode.create;
        // Test synchronous API
        try {
            zk.create("/clientHang2", data.getBytes(), OPEN_ACL_UNSAFE, PERSISTENT);
            Assert.fail("KeeperException is expected.");
        } catch (KeeperException exception) {
            Assert.assertEquals(REQUESTTIMEOUT.intValue(), exception.code().intValue());
        }
        // do cleanup
        close();
        for (int i = 0; i < (ClientRequestTimeoutTest.SERVER_COUNT); i++) {
            mt[i].shutdown();
        }
    }

    class CustomClientCnxn extends ClientCnxn {
        public CustomClientCnxn(String chrootPath, HostProvider hostProvider, int sessionTimeout, ZooKeeper zooKeeper, ClientWatchManager watcher, ClientCnxnSocket clientCnxnSocket, boolean canBeReadOnly) throws IOException {
            super(chrootPath, hostProvider, sessionTimeout, zooKeeper, watcher, clientCnxnSocket, canBeReadOnly);
        }

        @Override
        public void finishPacket(Packet p) {
            if ((dropPacket) && ((p.requestHeader.getType()) == (dropPacketType))) {
                // do nothing, just return, it is the same as packet is dropped
                // by the network
                return;
            }
            super.finishPacket(p);
        }
    }

    class CustomZooKeeper extends ZooKeeper {
        public CustomZooKeeper(String connectString, int sessionTimeout, Watcher watcher) throws IOException {
            super(connectString, sessionTimeout, watcher);
        }

        @Override
        protected ClientCnxn createConnection(String chrootPath, HostProvider hostProvider, int sessionTimeout, ZooKeeper zooKeeper, ClientWatchManager watcher, ClientCnxnSocket clientCnxnSocket, boolean canBeReadOnly) throws IOException {
            return new ClientRequestTimeoutTest.CustomClientCnxn(chrootPath, hostProvider, sessionTimeout, zooKeeper, watcher, clientCnxnSocket, canBeReadOnly);
        }
    }
}
