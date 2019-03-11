/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.as.test.integration.naming;


import BouncyCastleProvider.PROVIDER_NAME;
import NamingExtension.SUBSYSTEM_NAME;
import java.security.Security;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.directory.InitialDirContext;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.AnnotationUtils;
import org.apache.directory.server.core.annotations.ContextEntry;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.factory.DSAnnotationProcessor;
import org.apache.directory.server.factory.ServerAnnotationProcessor;
import org.apache.directory.server.ldap.LdapServer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.integration.ldap.InMemoryDirectoryServiceFactory;
import org.jboss.as.test.integration.security.common.ManagedCreateLdapServer;
import org.jboss.as.test.integration.security.common.ManagedCreateTransport;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test for external context binding. There are tests which use a usual InitialContext and treat it
 * as an external context and tests which connect to an actual external LDAP server.
 *
 * @author Stuart Douglas, Jan Martiska
 */
@RunWith(Arquillian.class)
@ServerSetup({ ExternalContextBindingTestCase.ObjectFactoryWithEnvironmentBindingTestCaseServerSetup.class, ExternalContextBindingTestCase.PrepareExternalLDAPServerSetup.class })
public class ExternalContextBindingTestCase {
    private static Logger LOGGER = Logger.getLogger(ExternalContextBindingTestCase.class);

    private static final String MODULE_NAME = "org.jboss.as.naming";

    public static final int LDAP_PORT = 10389;

    static class ObjectFactoryWithEnvironmentBindingTestCaseServerSetup implements ServerSetupTask {
        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            // bind the object factory
            ModelNode address = createAddress("nocache");
            ModelNode bindingAdd = new ModelNode();
            bindingAdd.get(OP).set(ADD);
            bindingAdd.get(OP_ADDR).set(address);
            bindingAdd.get(BINDING_TYPE).set(EXTERNAL_CONTEXT);
            bindingAdd.get(MODULE).set(ExternalContextBindingTestCase.MODULE_NAME);
            bindingAdd.get(CLASS).set(InitialContext.class.getName());
            ModelNode addResult = managementClient.getControllerClient().execute(bindingAdd);
            Assert.assertFalse(addResult.get(FAILURE_DESCRIPTION).toString(), addResult.get(FAILURE_DESCRIPTION).isDefined());
            ExternalContextBindingTestCase.LOGGER.trace("Object factory bound.");
            address = createAddress("cache");
            bindingAdd = new ModelNode();
            bindingAdd.get(OP).set(ADD);
            bindingAdd.get(OP_ADDR).set(address);
            bindingAdd.get(BINDING_TYPE).set(EXTERNAL_CONTEXT);
            bindingAdd.get(MODULE).set(ExternalContextBindingTestCase.MODULE_NAME);
            bindingAdd.get(CACHE).set(true);
            bindingAdd.get(CLASS).set(InitialContext.class.getName());
            addResult = managementClient.getControllerClient().execute(bindingAdd);
            Assert.assertFalse(addResult.get(FAILURE_DESCRIPTION).toString(), addResult.get(FAILURE_DESCRIPTION).isDefined());
            ExternalContextBindingTestCase.LOGGER.trace("Object factory bound.");
            address = createAddress("ldap");
            bindingAdd = new ModelNode();
            bindingAdd.get(OP).set(ADD);
            bindingAdd.get(OP_ADDR).set(address);
            bindingAdd.get(BINDING_TYPE).set(EXTERNAL_CONTEXT);
            bindingAdd.get(MODULE).set(ExternalContextBindingTestCase.MODULE_NAME);
            bindingAdd.get(CLASS).set(InitialDirContext.class.getName());
            bindingAdd.get(ENVIRONMENT).add("java.naming.provider.url", ((("ldap://" + (managementClient.getMgmtAddress())) + ":") + (ExternalContextBindingTestCase.LDAP_PORT)));
            bindingAdd.get(ENVIRONMENT).add("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
            bindingAdd.get(ENVIRONMENT).add(Context.SECURITY_AUTHENTICATION, "simple");
            bindingAdd.get(ENVIRONMENT).add(Context.SECURITY_PRINCIPAL, "uid=jduke,ou=People,dc=jboss,dc=org");
            bindingAdd.get(ENVIRONMENT).add(Context.SECURITY_CREDENTIALS, "theduke");
            addResult = managementClient.getControllerClient().execute(bindingAdd);
            Assert.assertFalse(addResult.get(FAILURE_DESCRIPTION).toString(), addResult.get(FAILURE_DESCRIPTION).isDefined());
            address = createAddress("ldap-cache");
            bindingAdd = new ModelNode();
            bindingAdd.get(OP).set(ADD);
            bindingAdd.get(OP_ADDR).set(address);
            bindingAdd.get(BINDING_TYPE).set(EXTERNAL_CONTEXT);
            bindingAdd.get(MODULE).set(ExternalContextBindingTestCase.MODULE_NAME);
            bindingAdd.get(CLASS).set(InitialDirContext.class.getName());
            bindingAdd.get(CACHE).set(true);
            bindingAdd.get(ENVIRONMENT).add("java.naming.provider.url", ((("ldap://" + (managementClient.getMgmtAddress())) + ":") + (ExternalContextBindingTestCase.LDAP_PORT)));
            bindingAdd.get(ENVIRONMENT).add("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
            bindingAdd.get(ENVIRONMENT).add(Context.SECURITY_AUTHENTICATION, "simple");
            bindingAdd.get(ENVIRONMENT).add(Context.SECURITY_PRINCIPAL, "uid=jduke,ou=People,dc=jboss,dc=org");
            bindingAdd.get(ENVIRONMENT).add(Context.SECURITY_CREDENTIALS, "theduke");
            addResult = managementClient.getControllerClient().execute(bindingAdd);
            Assert.assertFalse(addResult.get(FAILURE_DESCRIPTION).toString(), addResult.get(FAILURE_DESCRIPTION).isDefined());
        }

        private ModelNode createAddress(String part) {
            final ModelNode address = new ModelNode();
            address.add(SUBSYSTEM, SUBSYSTEM_NAME);
            address.add(BINDING, ("java:global/" + part));
            return address;
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
            // unbind the object factorys
            ModelNode bindingRemove = new ModelNode();
            bindingRemove.get(OP).set(REMOVE);
            bindingRemove.get(OP_ADDR).set(createAddress("nocache"));
            bindingRemove.get(OPERATION_HEADERS).get(ALLOW_RESOURCE_SERVICE_RESTART).set(true);
            ModelNode removeResult = managementClient.getControllerClient().execute(bindingRemove);
            Assert.assertFalse(removeResult.get(FAILURE_DESCRIPTION).toString(), removeResult.get(FAILURE_DESCRIPTION).isDefined());
            ExternalContextBindingTestCase.LOGGER.trace("Object factory with uncached InitialContext unbound.");
            bindingRemove = new ModelNode();
            bindingRemove.get(OP).set(REMOVE);
            bindingRemove.get(OP_ADDR).set(createAddress("cache"));
            bindingRemove.get(OPERATION_HEADERS).get(ALLOW_RESOURCE_SERVICE_RESTART).set(true);
            removeResult = managementClient.getControllerClient().execute(bindingRemove);
            Assert.assertFalse(removeResult.get(FAILURE_DESCRIPTION).toString(), removeResult.get(FAILURE_DESCRIPTION).isDefined());
            ExternalContextBindingTestCase.LOGGER.trace("Object factory with cached InitialContext unbound.");
            bindingRemove = new ModelNode();
            bindingRemove.get(OP).set(REMOVE);
            bindingRemove.get(OP_ADDR).set(createAddress("ldap"));
            bindingRemove.get(OPERATION_HEADERS).get(ALLOW_RESOURCE_SERVICE_RESTART).set(true);
            removeResult = managementClient.getControllerClient().execute(bindingRemove);
            Assert.assertFalse(removeResult.get(FAILURE_DESCRIPTION).toString(), removeResult.get(FAILURE_DESCRIPTION).isDefined());
            ExternalContextBindingTestCase.LOGGER.trace("Object factory with uncached InitialDirContext unbound.");
            bindingRemove = new ModelNode();
            bindingRemove.get(OP).set(REMOVE);
            bindingRemove.get(OP_ADDR).set(createAddress("ldap-cache"));
            bindingRemove.get(OPERATION_HEADERS).get(ALLOW_RESOURCE_SERVICE_RESTART).set(true);
            removeResult = managementClient.getControllerClient().execute(bindingRemove);
            Assert.assertFalse(removeResult.get(FAILURE_DESCRIPTION).toString(), removeResult.get(FAILURE_DESCRIPTION).isDefined());
            ExternalContextBindingTestCase.LOGGER.trace("Object factory with cached InitialDirContext unbound.");
        }
    }

    // @formatter:off
    // @formatter:on
    @CreateDS(name = "JBossDS-ExternalContextBindingTestCase", factory = InMemoryDirectoryServiceFactory.class, partitions = { @CreatePartition(name = "jboss", suffix = "dc=jboss,dc=org", contextEntry = @ContextEntry(entryLdif = "dn: dc=jboss,dc=org\n" + (("dc: jboss\n" + "objectClass: top\n") + "objectClass: domain\n\n"))) })
    @CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP", port = ExternalContextBindingTestCase.LDAP_PORT) })
    static class PrepareExternalLDAPServerSetup implements ServerSetupTask {
        private DirectoryService directoryService;

        private LdapServer ldapServer;

        private boolean removeBouncyCastle = false;

        public void fixTransportAddress(ManagedCreateLdapServer createLdapServer, String address) {
            final CreateTransport[] createTransports = createLdapServer.transports();
            for (int i = 0; i < (createTransports.length); i++) {
                final ManagedCreateTransport mgCreateTransport = new ManagedCreateTransport(createTransports[i]);
                mgCreateTransport.setAddress(address);
                createTransports[i] = mgCreateTransport;
            }
        }

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            try {
                if ((Security.getProvider(PROVIDER_NAME)) == null) {
                    Security.addProvider(new BouncyCastleProvider());
                    removeBouncyCastle = true;
                }
            } catch (SecurityException ex) {
                ExternalContextBindingTestCase.LOGGER.warn("Cannot register BouncyCastleProvider", ex);
            }
            directoryService = DSAnnotationProcessor.getDirectoryService();
            final SchemaManager schemaManager = directoryService.getSchemaManager();
            try {
                for (LdifEntry ldifEntry : new LdifReader(ExternalContextBindingTestCase.class.getResourceAsStream(((ExternalContextBindingTestCase.class.getSimpleName()) + ".ldif")))) {
                    directoryService.getAdminSession().add(new org.apache.directory.api.ldap.model.entry.DefaultEntry(schemaManager, ldifEntry.getEntry()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            final ManagedCreateLdapServer createLdapServer = new ManagedCreateLdapServer(((CreateLdapServer) (AnnotationUtils.getInstance(CreateLdapServer.class))));
            fixTransportAddress(createLdapServer, managementClient.getMgmtAddress());
            ldapServer = ServerAnnotationProcessor.instantiateLdapServer(createLdapServer, directoryService);
            ldapServer.start();
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            ldapServer.stop();
            directoryService.shutdown();
            if (removeBouncyCastle) {
                try {
                    Security.removeProvider(PROVIDER_NAME);
                } catch (SecurityException ex) {
                    ExternalContextBindingTestCase.LOGGER.warn("Cannot deregister BouncyCastleProvider", ex);
                }
            }
        }
    }

    @Test
    public void testBasicWithoutCache() throws Exception {
        InitialContext context = new InitialContext();
        InitialContext lookupContext = ((InitialContext) (context.lookup("java:global/nocache")));
        Assert.assertNotNull(lookupContext);
        LookupEjb ejb = ((LookupEjb) (lookupContext.lookup("java:module/LookupEjb")));
        Assert.assertNotNull(ejb);
        InitialContext newLookupContext = ((InitialContext) (context.lookup("java:global/nocache")));
        Assert.assertNotSame(lookupContext, newLookupContext);
        Assert.assertEquals(InitialContext.class, lookupContext.getClass());
    }

    @Test
    public void testBasicWithCache() throws Exception {
        InitialContext context = new InitialContext();
        InitialContext lookupContext = ((InitialContext) (context.lookup("java:global/cache")));
        Assert.assertNotNull(lookupContext);
        LookupEjb ejb = ((LookupEjb) (lookupContext.lookup("java:module/LookupEjb")));
        Assert.assertNotNull(ejb);
        InitialContext newLookupContext = ((InitialContext) (context.lookup("java:global/cache")));
        Assert.assertSame(lookupContext, newLookupContext);
        // close should have no effect
        lookupContext.close();
        ejb = ((LookupEjb) (lookupContext.lookup("java:module/LookupEjb")));
        Assert.assertNotNull(ejb);
        Assert.assertNotSame(InitialContext.class, lookupContext.getClass());
    }

    @Test
    public void testWithActualLDAPContextWithoutCache() throws Exception {
        testWithActualLDAPContext(false);
    }

    @Test
    public void testWithActualLDAPContextWithCache() throws Exception {
        testWithActualLDAPContext(true);
    }
}
