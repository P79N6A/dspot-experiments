/**
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */
package io.crate.integrationtests;


import Constants.JAVA_VERSION;
import Constants.JVM_NAME;
import Constants.JVM_VENDOR;
import Constants.JVM_VERSION;
import ESIntegTestCase.ClusterScope;
import Version.CURRENT;
import io.crate.testing.SQLResponse;
import io.crate.testing.UseJdbc;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.util.Constants;
import org.elasticsearch.env.NodeEnvironment;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Test;


@ClusterScope(numClientNodes = 0, numDataNodes = 2, supportsDedicatedMasters = false)
public class NodeStatsTest extends SQLTransportIntegrationTest {
    @Test
    public void testSysNodesMem() throws Exception {
        SQLResponse response = execute("select mem['free'], mem['used'], mem['free_percent'], mem['used_percent'] from sys.nodes limit 1");
        long free = ((long) (response.rows()[0][0]));
        long used = ((long) (response.rows()[0][1]));
        double free_percent = (((Number) (response.rows()[0][2])).intValue()) * 0.01;
        double used_percent = (((Number) (response.rows()[0][3])).intValue()) * 0.01;
        double calculated_free_percent = free / ((double) (free + used));
        double calculated_used_percent = used / ((double) (free + used));
        double max_delta = 0.02;// result should not differ from calculated result more than 2%

        double free_delta = Math.abs((calculated_free_percent - free_percent));
        double used_delta = Math.abs((calculated_used_percent - used_percent));
        assertThat(free_delta, Is.is(Matchers.lessThan(max_delta)));
        assertThat(used_delta, Is.is(Matchers.lessThan(max_delta)));
    }

    // because of json some values are transfered as integer instead of long
    @SuppressWarnings("ConstantConditions")
    @Test
    @UseJdbc(0)
    public void testThreadPools() throws Exception {
        SQLResponse response = execute("select thread_pools from sys.nodes limit 1");
        Object[] threadPools = ((Object[]) (response.rows()[0][0]));
        assertThat(threadPools.length, Matchers.greaterThanOrEqualTo(1));
        Map<String, Object> threadPool = null;
        for (Object t : threadPools) {
            Map<String, Object> map = ((Map<String, Object>) (t));
            if (map.get("name").equals("generic")) {
                threadPool = map;
                break;
            }
        }
        assertThat(threadPool.get("name"), Is.is("generic"));
        assertThat(((Integer) (threadPool.get("active"))), Matchers.greaterThanOrEqualTo(0));
        assertThat(((Long) (threadPool.get("rejected"))), Matchers.greaterThanOrEqualTo(0L));
        assertThat(((Integer) (threadPool.get("largest"))), Matchers.greaterThanOrEqualTo(0));
        assertThat(((Long) (threadPool.get("completed"))), Matchers.greaterThanOrEqualTo(0L));
        assertThat(((Integer) (threadPool.get("threads"))), Matchers.greaterThanOrEqualTo(0));
        assertThat(((Integer) (threadPool.get("queue"))), Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    public void testThreadPoolValue() throws Exception {
        SQLResponse response = execute("select thread_pools['name'], thread_pools['queue'] from sys.nodes limit 1");
        assertThat(response.rowCount(), Is.is(1L));
        Object[] objects = ((Object[]) (response.rows()[0][0]));
        String[] names = Arrays.copyOf(objects, objects.length, String[].class);
        assertThat(names.length, Matchers.greaterThanOrEqualTo(1));
        assertThat(names, Matchers.hasItemInArray("generic"));
        Object[] queues = ((Object[]) (response.rows()[0][1]));
        assertThat(queues.length, Matchers.greaterThanOrEqualTo(1));
        assertThat(((Integer) (queues[0])), Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    public void testNetwork() throws Exception {
        SQLResponse response = execute("select network from sys.nodes limit 1");
        assertThat(response.rowCount(), Is.is(1L));
        Map<String, Object> network = ((Map<String, Object>) (response.rows()[0][0]));
        assertThat(network, Matchers.hasKey("tcp"));
        Map<String, Object> tcp = ((Map<String, Object>) (network.get("tcp")));
        assertNetworkTCP(tcp);
        response = execute("select network['tcp'] from sys.nodes limit 1");
        assertThat(response.rowCount(), Is.is(1L));
        tcp = ((Map<String, Object>) (response.rows()[0][0]));
        assertNetworkTCP(tcp);
    }

    @Test
    public void testNetworkTcpConnectionFields() throws Exception {
        SQLResponse response = execute(("select " + ((((("network['tcp']['connections']['initiated'], " + "network['tcp']['connections']['accepted'], ") + "network['tcp']['connections']['curr_established'],") + "network['tcp']['connections']['dropped'],") + "network['tcp']['connections']['embryonic_dropped']") + " from sys.nodes limit 1")));
        assertThat(response.rowCount(), Is.is(1L));
        for (int i = 0; i < (response.cols().length); i++) {
            assertThat(((Long) (response.rows()[0][i])), Matchers.greaterThanOrEqualTo((-1L)));
        }
    }

    @Test
    public void testNetworkTcpPacketsFields() throws Exception {
        SQLResponse response = execute(("select " + ((((("network['tcp']['packets']['sent'], " + "network['tcp']['packets']['received'], ") + "network['tcp']['packets']['retransmitted'], ") + "network['tcp']['packets']['errors_received'], ") + "network['tcp']['packets']['rst_sent'] ") + "from sys.nodes limit 1")));
        assertThat(response.rowCount(), Is.is(1L));
        for (int i = 0; i < (response.cols().length); i++) {
            assertThat(((Long) (response.rows()[0][i])), Matchers.greaterThanOrEqualTo((-1L)));
        }
    }

    // because of json some values are transfered as integer instead of long
    @Test
    @UseJdbc(0)
    public void testSysNodesOs() throws Exception {
        SQLResponse response = execute("select os from sys.nodes limit 1");
        Map results = ((Map) (response.rows()[0][0]));
        assertThat(response.rowCount(), Is.is(1L));
        assertThat(((Long) (results.get("timestamp"))), Matchers.greaterThan(0L));
        assertThat(((Long) (results.get("uptime"))), Matchers.greaterThanOrEqualTo((-1L)));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("system"))), Matchers.greaterThanOrEqualTo(((short) (-1))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("system"))), Matchers.lessThanOrEqualTo(((short) (100))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("user"))), Matchers.greaterThanOrEqualTo(((short) (-1))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("user"))), Matchers.lessThanOrEqualTo(((short) (100))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("used"))), Matchers.greaterThanOrEqualTo(((short) (-1))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("used"))), Matchers.lessThanOrEqualTo(((short) (100))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("idle"))), Matchers.greaterThanOrEqualTo(((short) (-1))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("idle"))), Matchers.lessThanOrEqualTo(((short) (100))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("stolen"))), Matchers.greaterThanOrEqualTo(((short) (-1))));
        assertThat(((Short) (((Map) (results.get("cpu"))).get("stolen"))), Matchers.lessThanOrEqualTo(((short) (100))));
    }

    @Test
    public void testSysNodesCgroup() throws Exception {
        if ((Constants.LINUX) && (!("true".equals(System.getenv("SHIPPABLE"))))) {
            // cgroups are only available on Linux
            SQLResponse response = execute(("select" + ((((((((" os['cgroup']['cpuacct']['control_group']," + " os['cgroup']['cpuacct']['usage_nanos'],") + " os['cgroup']['cpu']['control_group'],") + " os['cgroup']['cpu']['cfs_period_micros'],") + " os['cgroup']['cpu']['cfs_quota_micros'],") + " os['cgroup']['cpu']['num_elapsed_periods'],") + " os['cgroup']['cpu']['num_times_throttled'],") + " os['cgroup']['cpu']['time_throttled_nanos']") + " from sys.nodes limit 1")));
            assertThat(response.rowCount(), Is.is(1L));
            assertThat(response.rows()[0][0], Matchers.notNullValue());
            assertThat(((long) (response.rows()[0][1])), Matchers.greaterThanOrEqualTo(0L));
            assertThat(response.rows()[0][2], Matchers.notNullValue());
            assertThat(((long) (response.rows()[0][3])), Matchers.greaterThanOrEqualTo(0L));
            assertThat(((long) (response.rows()[0][4])), Matchers.anyOf(Matchers.equalTo((-1L)), Matchers.greaterThanOrEqualTo(0L)));
            assertThat(((long) (response.rows()[0][5])), Matchers.greaterThanOrEqualTo(0L));
            assertThat(((long) (response.rows()[0][6])), Matchers.greaterThanOrEqualTo(0L));
            assertThat(((long) (response.rows()[0][7])), Matchers.greaterThanOrEqualTo(0L));
        } else {
            // for all other OS cgroup fields should return `null`
            response = execute(("select os['cgroup']," + ((((((((((" os['cgroup']['cpuacct']," + " os['cgroup']['cpuacct']['control_group'],") + " os['cgroup']['cpuacct']['usage_nanos'],") + " os['cgroup']['cpu'],") + " os['cgroup']['cpu']['control_group'],") + " os['cgroup']['cpu']['cfs_period_micros'],") + " os['cgroup']['cpu']['cfs_quota_micros'],") + " os['cgroup']['cpu']['num_elapsed_periods'],") + " os['cgroup']['cpu']['num_times_throttled'],") + " os['cgroup']['cpu']['time_throttled_nanos']") + " from sys.nodes limit 1")));
            assertThat(response.rowCount(), Is.is(1L));
            for (int i = 0; i <= 10; i++) {
                assertNull(response.rows()[0][i]);
            }
        }
    }

    @Test
    public void testSysNodsOsInfo() throws Exception {
        SQLResponse response = execute("select os_info from sys.nodes limit 1");
        Map results = ((Map) (response.rows()[0][0]));
        assertThat(response.rowCount(), Is.is(1L));
        assertThat(((Integer) (results.get("available_processors"))), Matchers.greaterThan(0));
        assertEquals(Constants.OS_NAME, results.get("name"));
        assertEquals(Constants.OS_ARCH, results.get("arch"));
        assertEquals(Constants.OS_VERSION, results.get("version"));
        Map<String, Object> jvmObj = new HashMap<>(4);
        jvmObj.put("version", JAVA_VERSION);
        jvmObj.put("vm_name", JVM_NAME);
        jvmObj.put("vm_vendor", JVM_VENDOR);
        jvmObj.put("vm_version", JVM_VERSION);
        assertEquals(jvmObj, results.get("jvm"));
    }

    @Test
    public void testSysNodesProcess() throws Exception {
        SQLResponse response = execute(("select process['open_file_descriptors'], " + ("process['max_open_file_descriptors'] " + "from sys.nodes limit 1")));
        for (int i = 0; i < (response.cols().length); i++) {
            assertThat(((Long) (response.rows()[0][i])), Matchers.greaterThanOrEqualTo((-1L)));
        }
    }

    // because of json some values are transfered as integer instead of long
    @Test
    @UseJdbc(0)
    public void testFs() throws Exception {
        SQLResponse response = execute("select fs from sys.nodes limit 1");
        assertThat(response.rowCount(), Is.is(1L));
        assertThat(response.rows()[0][0], Matchers.instanceOf(Map.class));
        Map<String, Object> fs = ((Map<String, Object>) (response.rows()[0][0]));
        assertThat(fs.keySet().size(), Is.is(3));
        assertThat(fs.keySet(), Matchers.hasItems("total", "disks", "data"));
        Map<String, Object> total = ((Map<String, Object>) (fs.get("total")));
        assertThat(total.keySet(), Matchers.hasItems("size", "used", "available", "reads", "writes", "bytes_written", "bytes_read"));
        for (Object val : total.values()) {
            assertThat(((Long) (val)), Matchers.greaterThanOrEqualTo((-1L)));
        }
        Object[] disks = ((Object[]) (fs.get("disks")));
        if ((disks.length) > 0) {
            // on travis there are no accessible disks
            assertThat(disks.length, Matchers.greaterThanOrEqualTo(1));
            Map<String, Object> someDisk = ((Map<String, Object>) (disks[0]));
            assertThat(someDisk.keySet().size(), Is.is(8));
            assertThat(someDisk.keySet(), Matchers.hasItems("dev", "size", "used", "available", "reads", "writes", "bytes_read", "bytes_written"));
            for (Map.Entry<String, Object> entry : someDisk.entrySet()) {
                if (!(entry.getKey().equals("dev"))) {
                    assertThat(((Long) (entry.getValue())), Matchers.greaterThanOrEqualTo((-1L)));
                }
            }
        }
        Object[] data = ((Object[]) (fs.get("data")));
        if ((data.length) > 0) {
            // without sigar, no data definition returned
            int numDataPaths = internalCluster().getInstance(NodeEnvironment.class).nodeDataPaths().length;
            assertThat(data.length, Is.is(numDataPaths));
            Map<String, Object> someData = ((Map<String, Object>) (data[0]));
            assertThat(someData.keySet().size(), Is.is(2));
            assertThat(someData.keySet(), Matchers.hasItems("dev", "path"));
        }
    }

    @Test
    public void testFsNoRootFS() throws Exception {
        SQLResponse response = execute("select fs['data']['dev'], fs['disks'] from sys.nodes");
        assertThat(response.rowCount(), Is.is(2L));
        for (Object[] row : response.rows()) {
            // data device name
            for (Object diskDevName : ((Object[]) (row[0]))) {
                assertThat(((String) (diskDevName)), Is.is(Matchers.not("rootfs")));
            }
            Object[] disks = ((Object[]) (row[1]));
            // disks device name
            for (Object disk : disks) {
                String diskDevName = ((String) (((Map<String, Object>) (disk)).get("dev")));
                assertThat(diskDevName, Is.is(Matchers.notNullValue()));
                assertThat(diskDevName, Is.is(Matchers.not("rootfs")));
            }
        }
    }

    @Test
    public void testSysNodesObjectArrayStringChildColumn() throws Exception {
        SQLResponse response = execute("select fs['data']['path'] from sys.nodes");
        assertThat(response.rowCount(), Matchers.is(2L));
        for (Object path : ((Object[]) (response.rows()[0][0]))) {
            assertThat(path, Matchers.instanceOf(String.class));
        }
    }

    @Test
    public void testVersion() throws Exception {
        SQLResponse response = execute(("select version, version['number'], " + ("version['build_hash'], version['build_snapshot'] " + "from sys.nodes limit 1")));
        assertThat(response.rowCount(), Is.is(1L));
        assertThat(response.rows()[0][0], Matchers.instanceOf(Map.class));
        assertThat(((Map<String, Object>) (response.rows()[0][0])), Matchers.allOf(Matchers.hasKey("number"), Matchers.hasKey("build_hash"), Matchers.hasKey("build_snapshot")));
        assertThat(((String) (response.rows()[0][1])), Is.is(CURRENT.externalNumber()));
        assertThat(response.rows()[0][2], Matchers.instanceOf(String.class));
        assertThat(((Boolean) (response.rows()[0][3])), Is.is(CURRENT.isSnapshot()));
    }

    @Test
    public void testRegexpMatchOnNode() throws Exception {
        SQLResponse response = execute("select name from sys.nodes where name ~ 'node_s[0-1]{1,2}' order by name");
        assertThat(response.rowCount(), Is.is(2L));
        assertThat(((String) (response.rows()[0][0])), Is.is("node_s0"));
        assertThat(((String) (response.rows()[1][0])), Is.is("node_s1"));
    }
}
