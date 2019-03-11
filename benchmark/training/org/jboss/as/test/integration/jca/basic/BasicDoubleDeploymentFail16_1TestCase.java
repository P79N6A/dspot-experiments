/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.integration.jca.basic;


import Namespace.RESOURCEADAPTERS_1_0;
import java.util.List;
import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.connector.subsystems.resourceadapters.ResourceAdapterSubsystemParser;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.test.integration.jca.rar.MultipleAdminObject1;
import org.jboss.as.test.integration.jca.rar.MultipleConnectionFactory1;
import org.jboss.as.test.integration.management.base.AbstractMgmtServerSetupTask;
import org.jboss.as.test.integration.management.base.ContainerResourceMgmtTestBase;
import org.jboss.as.test.shared.FileUtils;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test failure of activation for non ID-ed RAs
 *
 * @author baranowb
 */
@RunWith(Arquillian.class)
@ServerSetup(BasicDoubleDeploymentFail16_1TestCase.BasicDoubleDeploymentTestCaseSetup.class)
public class BasicDoubleDeploymentFail16_1TestCase extends ContainerResourceMgmtTestBase {
    // deployment archive name must match "archive" element.
    private static final String DEPLOYMENT_MODULE_NAME = "basic";

    private static final String DEPLOYMENT_NAME = (BasicDoubleDeploymentFail16_1TestCase.DEPLOYMENT_MODULE_NAME) + ".rar";

    private static final String SUB_DEPLOYMENT_MODULE_NAME = "somejar";

    private static final String SUB_DEPLOYMENT_NAME = (BasicDoubleDeploymentFail16_1TestCase.SUB_DEPLOYMENT_MODULE_NAME) + ".jar";

    private static final String RAR_1_NAME = BasicDoubleDeploymentFail16_1TestCase.DEPLOYMENT_NAME;

    // private static final String RAR_2_NAME = "XXX2";
    static class BasicDoubleDeploymentTestCaseSetup extends AbstractMgmtServerSetupTask {
        @Override
        public void doSetup(final ManagementClient managementClient) throws Exception {
            String xml = FileUtils.readFile(BasicDeployment16TestCase.class, "basic16.xml");
            List<ModelNode> operations = xmlToModelOperations(xml, RESOURCEADAPTERS_1_0.getUriString(), new ResourceAdapterSubsystemParser());
            executeOperation(operationListToCompositeOperation(operations));
            xml = FileUtils.readFile(BasicDeployment16TestCase.class, "basic16-duplicate.xml");
            operations = xmlToModelOperations(xml, RESOURCEADAPTERS_1_0.getUriString(), new ResourceAdapterSubsystemParser());
            final ModelNode result = executeOperation(operationListToCompositeOperation(operations), false);
            Assert.assertTrue((!(Operations.isSuccessfulOutcome(result))));
            final String failureDescription = result.get("result").get("step-1").get("failure-description").asString();
            Assert.assertTrue(failureDescription.startsWith("WFLYCTL0212: Duplicate resource"));
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
            try {
                remove(getAddress(BasicDoubleDeploymentFail16_1TestCase.RAR_1_NAME));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private ModelNode getAddress(final String name) {
            final ModelNode address = new ModelNode();
            address.add("subsystem", "resource-adapters");
            address.add("resource-adapter", name);
            address.protect();
            return address;
        }
    }

    @Resource(mappedName = "java:jboss/name1")
    private MultipleConnectionFactory1 connectionFactory1;

    @Resource(mappedName = "java:jboss/Name3")
    private MultipleAdminObject1 adminObject1;

    /**
     * Test configuration
     *
     * @throws Throwable
     * 		Thrown if case of an error
     */
    @Test
    public void testConfiguration() throws Throwable {
        Assert.assertNotNull("CF1 not found", connectionFactory1);
        Assert.assertNotNull("AO1 not found", adminObject1);
    }

    @Test(expected = NameNotFoundException.class)
    public void testNonExistingConfig_1() throws Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.lookup("java:jboss/name1-2");
    }

    @Test(expected = NameNotFoundException.class)
    public void testNonExistingConfig_2() throws Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.lookup("java:jboss/Name3-2");
    }
}
