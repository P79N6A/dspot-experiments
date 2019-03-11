/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdbi.doc;


import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.rule.PgDatabaseRule;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.junit.Rule;
import org.junit.Test;


// end::sqlObject[]
public class GeneratedKeysTest {
    @Rule
    public PgDatabaseRule dbRule = new PgDatabaseRule().withPlugin(new SqlObjectPlugin()).withPlugin(new PostgresPlugin());

    private Jdbi db;

    // tag::setup[]
    public static class User {
        final int id;

        final String name;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // end::setup[]
    // tag::fluent[]
    @Test
    public void fluentInsertKeys() {
        db.useHandle(( handle) -> {
            jdbi.doc.User data = handle.createUpdate("INSERT INTO users (name) VALUES(?)").bind(0, "Data").executeAndReturnGeneratedKeys().mapTo(.class).findOnly();
            assertEquals(1, data.id);// This value is generated by the database

            assertEquals("Data", data.name);
        });
    }

    // end::fluent[]
    // tag::sqlObject[]
    @Test
    public void sqlObjectBatchKeys() {
        db.useExtension(GeneratedKeysTest.UserDao.class, ( dao) -> {
            List<jdbi.doc.User> users = dao.createUsers("Alice", "Bob", "Charlie");
            assertEquals(3, users.size());
            assertEquals(1, users.get(0).id);
            assertEquals("Alice", users.get(0).name);
            assertEquals(2, users.get(1).id);
            assertEquals("Bob", users.get(1).name);
            assertEquals(3, users.get(2).id);
            assertEquals("Charlie", users.get(2).name);
        });
    }

    public interface UserDao {
        @SqlBatch("INSERT INTO users (name) VALUES(?)")
        @GetGeneratedKeys
        java.util.List<GeneratedKeysTest.User> createUsers(String... names);
    }
}
