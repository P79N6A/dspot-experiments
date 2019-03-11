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
package org.apache.zookeeper.test;


import CreateMode.EPHEMERAL;
import CreateMode.PERSISTENT;
import Ids.OPEN_ACL_UNSAFE;
import java.io.IOException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Test;


public class StatTest extends ClientBase {
    private ZooKeeper zk;

    @Test
    public void testBasic() throws IOException, InterruptedException, KeeperException {
        String name = "/foo";
        zk.create(name, name.getBytes(), OPEN_ACL_UNSAFE, PERSISTENT);
        Stat stat;
        stat = newStat();
        zk.getData(name, false, stat);
        Assert.assertEquals(stat.getCzxid(), stat.getMzxid());
        Assert.assertEquals(stat.getCzxid(), stat.getPzxid());
        Assert.assertEquals(stat.getCtime(), stat.getMtime());
        Assert.assertEquals(0, stat.getCversion());
        Assert.assertEquals(0, stat.getVersion());
        Assert.assertEquals(0, stat.getAversion());
        Assert.assertEquals(0, stat.getEphemeralOwner());
        Assert.assertEquals(name.length(), stat.getDataLength());
        Assert.assertEquals(0, stat.getNumChildren());
    }

    @Test
    public void testChild() throws IOException, InterruptedException, KeeperException {
        String name = "/foo";
        zk.create(name, name.getBytes(), OPEN_ACL_UNSAFE, PERSISTENT);
        String childname = name + "/bar";
        zk.create(childname, childname.getBytes(), OPEN_ACL_UNSAFE, EPHEMERAL);
        Stat stat;
        stat = newStat();
        zk.getData(name, false, stat);
        Assert.assertEquals(stat.getCzxid(), stat.getMzxid());
        Assert.assertEquals(((stat.getCzxid()) + 1), stat.getPzxid());
        Assert.assertEquals(stat.getCtime(), stat.getMtime());
        Assert.assertEquals(1, stat.getCversion());
        Assert.assertEquals(0, stat.getVersion());
        Assert.assertEquals(0, stat.getAversion());
        Assert.assertEquals(0, stat.getEphemeralOwner());
        Assert.assertEquals(name.length(), stat.getDataLength());
        Assert.assertEquals(1, stat.getNumChildren());
        stat = newStat();
        zk.getData(childname, false, stat);
        Assert.assertEquals(stat.getCzxid(), stat.getMzxid());
        Assert.assertEquals(stat.getCzxid(), stat.getPzxid());
        Assert.assertEquals(stat.getCtime(), stat.getMtime());
        Assert.assertEquals(0, stat.getCversion());
        Assert.assertEquals(0, stat.getVersion());
        Assert.assertEquals(0, stat.getAversion());
        Assert.assertEquals(zk.getSessionId(), stat.getEphemeralOwner());
        Assert.assertEquals(childname.length(), stat.getDataLength());
        Assert.assertEquals(0, stat.getNumChildren());
    }

    @Test
    public void testChildren() throws IOException, InterruptedException, KeeperException {
        String name = "/foo";
        zk.create(name, name.getBytes(), OPEN_ACL_UNSAFE, PERSISTENT);
        for (int i = 0; i < 10; i++) {
            String childname = (name + "/bar") + i;
            zk.create(childname, childname.getBytes(), OPEN_ACL_UNSAFE, EPHEMERAL);
            Stat stat;
            stat = newStat();
            zk.getData(name, false, stat);
            Assert.assertEquals(stat.getCzxid(), stat.getMzxid());
            Assert.assertEquals((((stat.getCzxid()) + i) + 1), stat.getPzxid());
            Assert.assertEquals(stat.getCtime(), stat.getMtime());
            Assert.assertEquals((i + 1), stat.getCversion());
            Assert.assertEquals(0, stat.getVersion());
            Assert.assertEquals(0, stat.getAversion());
            Assert.assertEquals(0, stat.getEphemeralOwner());
            Assert.assertEquals(name.length(), stat.getDataLength());
            Assert.assertEquals((i + 1), stat.getNumChildren());
        }
    }

    @Test
    public void testDataSizeChange() throws IOException, InterruptedException, KeeperException {
        String name = "/foo";
        zk.create(name, name.getBytes(), OPEN_ACL_UNSAFE, PERSISTENT);
        Stat stat;
        stat = newStat();
        zk.getData(name, false, stat);
        Assert.assertEquals(stat.getCzxid(), stat.getMzxid());
        Assert.assertEquals(stat.getCzxid(), stat.getPzxid());
        Assert.assertEquals(stat.getCtime(), stat.getMtime());
        Assert.assertEquals(0, stat.getCversion());
        Assert.assertEquals(0, stat.getVersion());
        Assert.assertEquals(0, stat.getAversion());
        Assert.assertEquals(0, stat.getEphemeralOwner());
        Assert.assertEquals(name.length(), stat.getDataLength());
        Assert.assertEquals(0, stat.getNumChildren());
        zk.setData(name, (name + name).getBytes(), (-1));
        stat = newStat();
        zk.getData(name, false, stat);
        Assert.assertNotSame(stat.getCzxid(), stat.getMzxid());
        Assert.assertEquals(stat.getCzxid(), stat.getPzxid());
        Assert.assertNotSame(stat.getCtime(), stat.getMtime());
        Assert.assertEquals(0, stat.getCversion());
        Assert.assertEquals(1, stat.getVersion());
        Assert.assertEquals(0, stat.getAversion());
        Assert.assertEquals(0, stat.getEphemeralOwner());
        Assert.assertEquals(((name.length()) * 2), stat.getDataLength());
        Assert.assertEquals(0, stat.getNumChildren());
    }
}
