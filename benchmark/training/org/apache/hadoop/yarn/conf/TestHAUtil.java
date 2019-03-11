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
package org.apache.hadoop.yarn.conf;


import YarnConfiguration.AUTO_FAILOVER_EMBEDDED;
import YarnConfiguration.AUTO_FAILOVER_ENABLED;
import YarnConfiguration.CURATOR_LEADER_ELECTOR;
import YarnConfiguration.NM_ADDRESS;
import YarnConfiguration.RM_ADDRESS;
import YarnConfiguration.RM_HA_ENABLED;
import YarnConfiguration.RM_HA_ID;
import YarnConfiguration.RM_HA_IDS;
import YarnConfiguration.RM_HOSTNAME;
import java.util.Collection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.exceptions.YarnRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import static HAUtil.BAD_CONFIG_MESSAGE_PREFIX;
import static HAUtil.NO_LEADER_ELECTION_MESSAGE;
import static YarnConfiguration.RM_HA_ID;


public class TestHAUtil {
    private Configuration conf;

    private static final String RM1_ADDRESS_UNTRIMMED = "  \t\t\n 1.2.3.4:8021  \n\t ";

    private static final String RM1_ADDRESS = TestHAUtil.RM1_ADDRESS_UNTRIMMED.trim();

    private static final String RM2_ADDRESS = "localhost:8022";

    private static final String RM3_ADDRESS = "localhost:8033";

    private static final String RM1_NODE_ID_UNTRIMMED = "rm1 ";

    private static final String RM1_NODE_ID = TestHAUtil.RM1_NODE_ID_UNTRIMMED.trim();

    private static final String RM2_NODE_ID = "rm2";

    private static final String RM3_NODE_ID = "rm3";

    private static final String RM_INVALID_NODE_ID = ".rm";

    private static final String RM_NODE_IDS_UNTRIMMED = ((TestHAUtil.RM1_NODE_ID_UNTRIMMED) + ",") + (TestHAUtil.RM2_NODE_ID);

    private static final String RM_NODE_IDS = ((TestHAUtil.RM1_NODE_ID) + ",") + (TestHAUtil.RM2_NODE_ID);

    @Test
    public void testGetRMServiceId() throws Exception {
        conf.set(RM_HA_IDS, (((TestHAUtil.RM1_NODE_ID) + ",") + (TestHAUtil.RM2_NODE_ID)));
        Collection<String> rmhaIds = HAUtil.getRMHAIds(conf);
        Assert.assertEquals(2, rmhaIds.size());
        String[] ids = rmhaIds.toArray(new String[0]);
        Assert.assertEquals(TestHAUtil.RM1_NODE_ID, ids[0]);
        Assert.assertEquals(TestHAUtil.RM2_NODE_ID, ids[1]);
    }

    @Test
    public void testGetRMId() throws Exception {
        conf.set(RM_HA_ID, TestHAUtil.RM1_NODE_ID);
        Assert.assertEquals(("Does not honor " + (RM_HA_ID)), TestHAUtil.RM1_NODE_ID, HAUtil.getRMHAId(conf));
        conf.clear();
        Assert.assertNull((("Return null when " + (RM_HA_ID)) + " is not set"), HAUtil.getRMHAId(conf));
    }

    @Test
    public void testVerifyAndSetConfiguration() throws Exception {
        Configuration myConf = new Configuration(conf);
        try {
            HAUtil.verifyAndSetConfiguration(myConf);
        } catch (YarnRuntimeException e) {
            Assert.fail("Should not throw any exceptions.");
        }
        Assert.assertEquals("Should be saved as Trimmed collection", StringUtils.getStringCollection(TestHAUtil.RM_NODE_IDS), HAUtil.getRMHAIds(myConf));
        Assert.assertEquals("Should be saved as Trimmed string", TestHAUtil.RM1_NODE_ID, HAUtil.getRMHAId(myConf));
        for (String confKey : YarnConfiguration.getServiceAddressConfKeys(myConf)) {
            Assert.assertEquals(("RPC address not set for " + confKey), TestHAUtil.RM1_ADDRESS, myConf.get(confKey));
        }
        myConf = new Configuration(conf);
        myConf.set(RM_HA_IDS, TestHAUtil.RM1_NODE_ID);
        try {
            HAUtil.verifyAndSetConfiguration(myConf);
        } catch (YarnRuntimeException e) {
            Assert.assertEquals("YarnRuntimeException by verifyAndSetRMHAIds()", ((BAD_CONFIG_MESSAGE_PREFIX) + (HAUtil.getInvalidValueMessage(RM_HA_IDS, ((myConf.get(RM_HA_IDS)) + "\nHA mode requires atleast two RMs")))), e.getMessage());
        }
        myConf = new Configuration(conf);
        // simulate the case YarnConfiguration.RM_HA_ID is not set
        myConf.set(RM_HA_IDS, (((TestHAUtil.RM1_NODE_ID) + ",") + (TestHAUtil.RM2_NODE_ID)));
        for (String confKey : YarnConfiguration.getServiceAddressConfKeys(myConf)) {
            myConf.set(HAUtil.addSuffix(confKey, TestHAUtil.RM1_NODE_ID), TestHAUtil.RM1_ADDRESS);
            myConf.set(HAUtil.addSuffix(confKey, TestHAUtil.RM2_NODE_ID), TestHAUtil.RM2_ADDRESS);
        }
        try {
            HAUtil.verifyAndSetConfiguration(myConf);
        } catch (YarnRuntimeException e) {
            Assert.assertEquals("YarnRuntimeException by getRMId()", ((BAD_CONFIG_MESSAGE_PREFIX) + (HAUtil.getNeedToSetValueMessage(RM_HA_ID))), e.getMessage());
        }
        myConf = new Configuration(conf);
        myConf.set(RM_HA_ID, TestHAUtil.RM_INVALID_NODE_ID);
        myConf.set(RM_HA_IDS, (((TestHAUtil.RM_INVALID_NODE_ID) + ",") + (TestHAUtil.RM1_NODE_ID)));
        for (String confKey : YarnConfiguration.getServiceAddressConfKeys(myConf)) {
            // simulate xml with invalid node id
            myConf.set((confKey + (TestHAUtil.RM_INVALID_NODE_ID)), TestHAUtil.RM_INVALID_NODE_ID);
        }
        try {
            HAUtil.verifyAndSetConfiguration(myConf);
        } catch (YarnRuntimeException e) {
            Assert.assertEquals("YarnRuntimeException by addSuffix()", ((BAD_CONFIG_MESSAGE_PREFIX) + (HAUtil.getInvalidValueMessage(RM_HA_ID, TestHAUtil.RM_INVALID_NODE_ID))), e.getMessage());
        }
        myConf = new Configuration();
        // simulate the case HAUtil.RM_RPC_ADDRESS_CONF_KEYS are not set
        myConf.set(RM_HA_ID, TestHAUtil.RM1_NODE_ID);
        myConf.set(RM_HA_IDS, (((TestHAUtil.RM1_NODE_ID) + ",") + (TestHAUtil.RM2_NODE_ID)));
        try {
            HAUtil.verifyAndSetConfiguration(myConf);
            Assert.fail("Should throw YarnRuntimeException. by Configuration#set()");
        } catch (YarnRuntimeException e) {
            String confKey = HAUtil.addSuffix(RM_ADDRESS, TestHAUtil.RM1_NODE_ID);
            Assert.assertEquals("YarnRuntimeException by Configuration#set()", ((BAD_CONFIG_MESSAGE_PREFIX) + (HAUtil.getNeedToSetValueMessage((((HAUtil.addSuffix(RM_HOSTNAME, TestHAUtil.RM1_NODE_ID)) + " or ") + confKey)))), e.getMessage());
        }
        // simulate the case YarnConfiguration.RM_HA_IDS doesn't contain
        // the value of YarnConfiguration.RM_HA_ID
        myConf = new Configuration(conf);
        myConf.set(RM_HA_IDS, (((TestHAUtil.RM2_NODE_ID) + ",") + (TestHAUtil.RM3_NODE_ID)));
        myConf.set(RM_HA_ID, TestHAUtil.RM1_NODE_ID_UNTRIMMED);
        for (String confKey : YarnConfiguration.getServiceAddressConfKeys(myConf)) {
            myConf.set(HAUtil.addSuffix(confKey, TestHAUtil.RM1_NODE_ID), TestHAUtil.RM1_ADDRESS_UNTRIMMED);
            myConf.set(HAUtil.addSuffix(confKey, TestHAUtil.RM2_NODE_ID), TestHAUtil.RM2_ADDRESS);
            myConf.set(HAUtil.addSuffix(confKey, TestHAUtil.RM3_NODE_ID), TestHAUtil.RM3_ADDRESS);
        }
        try {
            HAUtil.verifyAndSetConfiguration(myConf);
        } catch (YarnRuntimeException e) {
            Assert.assertEquals("YarnRuntimeException by getRMId()'s validation", ((BAD_CONFIG_MESSAGE_PREFIX) + (HAUtil.getRMHAIdNeedToBeIncludedMessage("[rm2, rm3]", TestHAUtil.RM1_NODE_ID))), e.getMessage());
        }
        // simulate the case that no leader election is enabled
        myConf = new Configuration(conf);
        myConf.setBoolean(RM_HA_ENABLED, true);
        myConf.setBoolean(AUTO_FAILOVER_ENABLED, true);
        myConf.setBoolean(AUTO_FAILOVER_EMBEDDED, false);
        myConf.setBoolean(CURATOR_LEADER_ELECTOR, false);
        try {
            HAUtil.verifyAndSetConfiguration(myConf);
        } catch (YarnRuntimeException e) {
            Assert.assertEquals("YarnRuntimeException by getRMId()'s validation", ((BAD_CONFIG_MESSAGE_PREFIX) + (NO_LEADER_ELECTION_MESSAGE)), e.getMessage());
        }
    }

    @Test
    public void testGetConfKeyForRMInstance() {
        Assert.assertTrue("RM instance id is not suffixed", HAUtil.getConfKeyForRMInstance(RM_ADDRESS, conf).contains(HAUtil.getRMHAId(conf)));
        Assert.assertFalse("RM instance id is suffixed", HAUtil.getConfKeyForRMInstance(NM_ADDRESS, conf).contains(HAUtil.getRMHAId(conf)));
    }
}
