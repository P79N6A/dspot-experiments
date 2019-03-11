/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.plugin.configuration;


import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


public class ConfigurationTest {
    @Test
    public void testConfigSerialization() throws Exception {
        final ImmutableMap<String, Object> emptyMap = ImmutableMap.of();
        final Configuration emptyConfig = new Configuration(emptyMap);
        Assert.assertNull(emptyConfig.serializeToJson());
        Assert.assertNull(new Configuration(null).serializeToJson());
        final Map<String, Object> map = new HashMap<>();
        // Test might be broken depending on the iteration order...
        map.put("b", 1);
        map.put("a", 1);
        final String json = new Configuration(map).serializeToJson();
        Assert.assertEquals("{\"source\":{\"a\":1,\"b\":1}}", json);
        final Configuration config = Configuration.deserializeFromJson(json);
        Assert.assertTrue(config.intIsSet("a"));
        Assert.assertTrue(config.intIsSet("b"));
        final Configuration emptyConfigFromNull = Configuration.deserializeFromJson(null);
        Assert.assertNotNull(emptyConfigFromNull);
    }
}
