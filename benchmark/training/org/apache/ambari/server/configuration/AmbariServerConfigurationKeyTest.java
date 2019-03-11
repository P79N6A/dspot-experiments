/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.configuration;


import AmbariServerConfigurationCategory.LDAP_CONFIGURATION;
import AmbariServerConfigurationKey.BIND_PASSWORD;
import AmbariServerConfigurationKey.LDAP_ENABLED;
import AmbariServerConfigurationKey.TRUST_STORE_PASSWORD;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

import static AmbariServerConfigurationKey.TPROXY_ALLOWED_HOSTS;


public class AmbariServerConfigurationKeyTest {
    @Test
    public void testTranslateNullCategory() {
        Assert.assertNull(AmbariServerConfigurationKey.translate(null, "some.property"));
    }

    @Test
    public void testTranslateNullPropertyName() {
        Assert.assertNull(AmbariServerConfigurationKey.translate(LDAP_CONFIGURATION, null));
    }

    @Test
    public void testTranslateInvalidPropertyName() {
        Assert.assertNull(AmbariServerConfigurationKey.translate(LDAP_CONFIGURATION, "invalid_property_name"));
    }

    @Test
    public void testTranslateExpected() {
        Assert.assertSame(LDAP_ENABLED, AmbariServerConfigurationKey.translate(LDAP_CONFIGURATION, LDAP_ENABLED.key()));
    }

    @Test
    public void testTranslateRegex() {
        AmbariServerConfigurationKey keyWithRegex = TPROXY_ALLOWED_HOSTS;
        Assert.assertTrue(keyWithRegex.isRegex());
        Assert.assertSame(keyWithRegex, AmbariServerConfigurationKey.translate(keyWithRegex.getConfigurationCategory(), "ambari.tproxy.proxyuser.knox.hosts"));
        Assert.assertSame(keyWithRegex, AmbariServerConfigurationKey.translate(keyWithRegex.getConfigurationCategory(), "ambari.tproxy.proxyuser.not.knox.hosts"));
        AmbariServerConfigurationKey translatedKey = AmbariServerConfigurationKey.translate(keyWithRegex.getConfigurationCategory(), "ambari.tproxy.proxyuser.not.knox.groups");
        Assert.assertNotNull(translatedKey);
        Assert.assertNotSame(keyWithRegex, translatedKey);
        Assert.assertNull(AmbariServerConfigurationKey.translate(keyWithRegex.getConfigurationCategory(), "ambari.tproxy.proxyuser.not.knox.invalid"));
    }

    @Test
    public void testFindPasswordConfigurations() throws Exception {
        final Set<String> passwordConfigurations = AmbariServerConfigurationKey.findPasswordConfigurations();
        Assert.assertEquals(2, passwordConfigurations.size());
        Assert.assertTrue(passwordConfigurations.contains(BIND_PASSWORD.key()));
        Assert.assertTrue(passwordConfigurations.contains(TRUST_STORE_PASSWORD.key()));
    }
}
