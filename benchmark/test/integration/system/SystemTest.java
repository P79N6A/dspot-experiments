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
package integration.system;


import com.jayway.restassured.RestAssured;
import integration.BaseRestTest;
import integration.BaseRestTestHelper;
import integration.RequiresAuthentication;
import org.junit.Test;


@RequiresAuthentication
public class SystemTest extends BaseRestTest {
    @Test
    public void system() {
        RestAssured.given().when().get("/system").then().body(".", BaseRestTestHelper.containsAllKeys("cluster_id", "codename", "facility", "hostname", "is_processing", "lb_status", "lifecycle", "node_id", "operating_system", "started_at", "timezone", "version"));
    }
}
