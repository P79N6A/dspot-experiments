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
package org.apache.ambari.server.state.cluster;


import MaintenanceState.OFF;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ambari.server.events.listeners.upgrade.HostVersionOutOfSyncListener;
import org.apache.ambari.server.orm.OrmTestHelper;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.Config;
import org.apache.ambari.server.state.ConfigFactory;
import org.apache.ambari.server.state.Service;
import org.apache.ambari.server.state.ServiceComponent;
import org.apache.ambari.server.state.ServiceComponentFactory;
import org.apache.ambari.server.state.ServiceComponentHost;
import org.apache.ambari.server.state.ServiceComponentHostFactory;
import org.apache.ambari.server.state.ServiceFactory;
import org.apache.ambari.server.state.StackId;
import org.apache.ambari.server.state.State.INIT;
import org.apache.ambari.server.state.State.INSTALLED;
import org.apache.ambari.server.state.State.STARTED;
import org.apache.ambari.server.testing.DeadlockWarningThread;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests AMBARI-9368 and AMBARI-9761 which produced a deadlock during read and
 * writes of some of the impl classes.
 */
public class ClusterDeadlockTest {
    private static final int NUMBER_OF_HOSTS = 40;

    private static final int NUMBER_OF_THREADS = 5;

    private final AtomicInteger hostNameCounter = new AtomicInteger(0);

    @Inject
    private Injector injector;

    @Inject
    private Clusters clusters;

    @Inject
    private ServiceFactory serviceFactory;

    @Inject
    private ServiceComponentFactory serviceComponentFactory;

    @Inject
    private ServiceComponentHostFactory serviceComponentHostFactory;

    @Inject
    private ConfigFactory configFactory;

    @Inject
    private OrmTestHelper helper;

    private StackId stackId = new StackId("HDP-0.1");

    private String REPO_VERSION = "0.1-1234";

    /**
     * The cluster.
     */
    private Cluster cluster;

    /**
     *
     */
    private List<String> hostNames = new ArrayList<>(ClusterDeadlockTest.NUMBER_OF_HOSTS);

    /**
     * Tests that concurrent impl serialization and impl writing doesn't cause a
     * deadlock.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testDeadlockBetweenImplementations() throws Exception {
        Service service = cluster.getService("HDFS");
        ServiceComponent nameNodeComponent = service.getServiceComponent("NAMENODE");
        ServiceComponent dataNodeComponent = service.getServiceComponent("DATANODE");
        ServiceComponentHost nameNodeSCH = createNewServiceComponentHost("HDFS", "NAMENODE", "c64-0");
        ServiceComponentHost dataNodeSCH = createNewServiceComponentHost("HDFS", "DATANODE", "c64-0");
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < (ClusterDeadlockTest.NUMBER_OF_THREADS); i++) {
            ClusterDeadlockTest.DeadlockExerciserThread thread = new ClusterDeadlockTest.DeadlockExerciserThread();
            thread.setCluster(cluster);
            thread.setService(service);
            thread.setDataNodeComponent(dataNodeComponent);
            thread.setNameNodeComponent(nameNodeComponent);
            thread.setNameNodeSCH(nameNodeSCH);
            thread.setDataNodeSCH(dataNodeSCH);
            thread.start();
            threads.add(thread);
        }
        DeadlockWarningThread wt = new DeadlockWarningThread(threads);
        while (true) {
            if (!(wt.isAlive())) {
                break;
            }
        } 
        if (wt.isDeadlocked()) {
            Assert.assertFalse(wt.getErrorMessages().toString(), wt.isDeadlocked());
        } else {
            Assert.assertFalse(wt.isDeadlocked());
        }
    }

    /**
     * Tests that while serializing a service component, writes to that service
     * component do not cause a deadlock with the global cluster lock.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testAddingHostComponentsWhileReading() throws Exception {
        Service service = cluster.getService("HDFS");
        ServiceComponent nameNodeComponent = service.getServiceComponent("NAMENODE");
        ServiceComponent dataNodeComponent = service.getServiceComponent("DATANODE");
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ClusterDeadlockTest.ServiceComponentReaderWriterThread thread = new ClusterDeadlockTest.ServiceComponentReaderWriterThread();
            thread.setDataNodeComponent(dataNodeComponent);
            thread.setNameNodeComponent(nameNodeComponent);
            thread.start();
            threads.add(thread);
        }
        DeadlockWarningThread wt = new DeadlockWarningThread(threads);
        while (true) {
            if (!(wt.isAlive())) {
                break;
            }
        } 
        if (wt.isDeadlocked()) {
            Assert.assertFalse(wt.getErrorMessages().toString(), wt.isDeadlocked());
        } else {
            Assert.assertFalse(wt.isDeadlocked());
        }
    }

    /**
     * Tests that no deadlock exists while restarting components and reading from
     * the cluster.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testDeadlockWhileRestartingComponents() throws Exception {
        // for each host, install both components
        List<ServiceComponentHost> serviceComponentHosts = new ArrayList<>();
        for (String hostName : hostNames) {
            serviceComponentHosts.add(createNewServiceComponentHost("HDFS", "NAMENODE", hostName));
            serviceComponentHosts.add(createNewServiceComponentHost("HDFS", "DATANODE", hostName));
        }
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < (ClusterDeadlockTest.NUMBER_OF_THREADS); i++) {
            ClusterDeadlockTest.ClusterReaderThread clusterReaderThread = new ClusterDeadlockTest.ClusterReaderThread();
            ClusterDeadlockTest.ClusterWriterThread clusterWriterThread = new ClusterDeadlockTest.ClusterWriterThread();
            ClusterDeadlockTest.ServiceComponentRestartThread schWriterThread = new ClusterDeadlockTest.ServiceComponentRestartThread(serviceComponentHosts);
            threads.add(clusterReaderThread);
            threads.add(clusterWriterThread);
            threads.add(schWriterThread);
            clusterReaderThread.start();
            clusterWriterThread.start();
            schWriterThread.start();
        }
        DeadlockWarningThread wt = new DeadlockWarningThread(threads, 20, 1000);
        while (true) {
            if (!(wt.isAlive())) {
                break;
            }
        } 
        if (wt.isDeadlocked()) {
            Assert.assertFalse(wt.getErrorMessages().toString(), wt.isDeadlocked());
        } else {
            Assert.assertFalse(wt.isDeadlocked());
        }
    }

    @Test
    public void testDeadlockWithConfigsUpdate() throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < (ClusterDeadlockTest.NUMBER_OF_THREADS); i++) {
            ClusterDeadlockTest.ClusterDesiredConfigsReaderThread readerThread = null;
            for (int j = 0; j < (ClusterDeadlockTest.NUMBER_OF_THREADS); j++) {
                readerThread = new ClusterDeadlockTest.ClusterDesiredConfigsReaderThread();
                threads.add(readerThread);
            }
            for (Config config : cluster.getAllConfigs()) {
                ClusterDeadlockTest.ConfigUpdaterThread configUpdaterThread = new ClusterDeadlockTest.ConfigUpdaterThread(config);
                threads.add(configUpdaterThread);
            }
        }
        for (Thread thread : threads) {
            thread.start();
        }
        DeadlockWarningThread wt = new DeadlockWarningThread(threads);
        while (true) {
            if (!(wt.isAlive())) {
                break;
            }
        } 
        if (wt.isDeadlocked()) {
            Assert.assertFalse(wt.getErrorMessages().toString(), wt.isDeadlocked());
        } else {
            Assert.assertFalse(wt.isDeadlocked());
        }
    }

    private final class ClusterDesiredConfigsReaderThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                cluster.getDesiredConfigs();
            }
        }
    }

    private final class ConfigUpdaterThread extends Thread {
        private Config config;

        public ConfigUpdaterThread(Config config) {
            this.config = config;
        }

        @Override
        public void run() {
            for (int i = 0; i < 300; i++) {
                config.save();
            }
        }
    }

    /**
     * The {@link ClusterReaderThread} reads from a cluster over and over again
     * with a slight pause.
     */
    private final class ClusterReaderThread extends Thread {
        /**
         * {@inheritDoc }
         */
        @Override
        public void run() {
            try {
                for (int i = 0; i < 1000; i++) {
                    cluster.convertToResponse();
                    Thread.sleep(10);
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * The {@link ClusterWriterThread} writes some information to the cluster
     * instance over and over again with a slight pause.
     */
    private final class ClusterWriterThread extends Thread {
        /**
         * {@inheritDoc }
         */
        @Override
        public void run() {
            try {
                for (int i = 0; i < 1500; i++) {
                    cluster.setDesiredStackVersion(stackId);
                    Thread.sleep(10);
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * The {@link ServiceComponentRestartThread} is used to constantly set SCH
     * restart values.
     */
    private final class ServiceComponentRestartThread extends Thread {
        private List<ServiceComponentHost> serviceComponentHosts;

        /**
         * Constructor.
         *
         * @param serviceComponentHosts
         * 		
         */
        private ServiceComponentRestartThread(List<ServiceComponentHost> serviceComponentHosts) {
            this.serviceComponentHosts = serviceComponentHosts;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void run() {
            try {
                for (int i = 0; i < 1000; i++) {
                    // about 30ms to go through all SCHs, no sleep needed
                    for (ServiceComponentHost serviceComponentHost : serviceComponentHosts) {
                        serviceComponentHost.setRestartRequired(true);
                    }
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * The {@link ServiceComponentRestartThread} is used to continuously add
     * components to hosts while reading from those components.
     */
    private final class ServiceComponentReaderWriterThread extends Thread {
        private ServiceComponent nameNodeComponent;

        private ServiceComponent dataNodeComponent;

        /**
         *
         *
         * @param nameNodeComponent
         * 		the nameNodeComponent to set
         */
        public void setNameNodeComponent(ServiceComponent nameNodeComponent) {
            this.nameNodeComponent = nameNodeComponent;
        }

        /**
         *
         *
         * @param dataNodeComponent
         * 		the dataNodeComponent to set
         */
        public void setDataNodeComponent(ServiceComponent dataNodeComponent) {
            this.dataNodeComponent = dataNodeComponent;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void run() {
            try {
                for (int i = 0; i < 15; i++) {
                    int hostNumeric = hostNameCounter.getAndIncrement();
                    nameNodeComponent.convertToResponse();
                    createNewServiceComponentHost("HDFS", "NAMENODE", ("c64-" + hostNumeric));
                    dataNodeComponent.convertToResponse();
                    createNewServiceComponentHost("HDFS", "DATANODE", ("c64-" + hostNumeric));
                    Thread.sleep(10);
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * Tests AMBARI-9368 which produced a deadlock during read and writes of some
     * of the impl classes.
     */
    private static final class DeadlockExerciserThread extends Thread {
        private Cluster cluster;

        private Service service;

        private ServiceComponent nameNodeComponent;

        private ServiceComponent dataNodeComponent;

        private ServiceComponentHost nameNodeSCH;

        private ServiceComponentHost dataNodeSCH;

        /**
         *
         *
         * @param cluster
         * 		the cluster to set
         */
        public void setCluster(Cluster cluster) {
            this.cluster = cluster;
        }

        /**
         *
         *
         * @param service
         * 		the service to set
         */
        public void setService(Service service) {
            this.service = service;
        }

        /**
         *
         *
         * @param nameNodeComponent
         * 		the nameNodeComponent to set
         */
        public void setNameNodeComponent(ServiceComponent nameNodeComponent) {
            this.nameNodeComponent = nameNodeComponent;
        }

        /**
         *
         *
         * @param dataNodeComponent
         * 		the dataNodeComponent to set
         */
        public void setDataNodeComponent(ServiceComponent dataNodeComponent) {
            this.dataNodeComponent = dataNodeComponent;
        }

        /**
         *
         *
         * @param nameNodeSCH
         * 		the nameNodeSCH to set
         */
        public void setNameNodeSCH(ServiceComponentHost nameNodeSCH) {
            this.nameNodeSCH = nameNodeSCH;
        }

        /**
         *
         *
         * @param dataNodeSCH
         * 		the dataNodeSCH to set
         */
        public void setDataNodeSCH(ServiceComponentHost dataNodeSCH) {
            this.dataNodeSCH = dataNodeSCH;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void run() {
            try {
                for (int i = 0; i < 10; i++) {
                    cluster.convertToResponse();
                    service.convertToResponse();
                    nameNodeComponent.convertToResponse();
                    dataNodeComponent.convertToResponse();
                    nameNodeSCH.convertToResponse(null);
                    dataNodeSCH.convertToResponse(null);
                    cluster.setProvisioningState(INIT);
                    service.setMaintenanceState(OFF);
                    nameNodeComponent.setDesiredState(STARTED);
                    dataNodeComponent.setDesiredState(INSTALLED);
                    nameNodeSCH.setState(STARTED);
                    dataNodeSCH.setState(INSTALLED);
                    Thread.sleep(100);
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     *
     */
    private class MockModule implements Module {
        /**
         *
         */
        @Override
        public void configure(Binder binder) {
            // this listener gets in the way of actually testing the concurrency
            // between the threads; it slows them down too much, so mock it out
            binder.bind(HostVersionOutOfSyncListener.class).toInstance(EasyMock.createNiceMock(HostVersionOutOfSyncListener.class));
        }
    }
}
