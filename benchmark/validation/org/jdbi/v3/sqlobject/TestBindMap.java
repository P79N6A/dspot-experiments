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
package org.jdbi.v3.sqlobject;


import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Something;
import org.jdbi.v3.core.mapper.SomethingMapper;
import org.jdbi.v3.core.rule.H2DatabaseRule;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMap;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TestBindMap {
    @Rule
    public H2DatabaseRule dbRule = new H2DatabaseRule().withSomething().withPlugin(new SqlObjectPlugin());

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Handle handle;

    private TestBindMap.Dao dao;

    @Test
    public void testBindMap() {
        handle.execute("insert into something (id, name) values (1, 'Alice')");
        dao.update(1, Collections.singletonMap("name", "Alicia"));
        assertThat(dao.get(1).getName()).isEqualTo("Alicia");
    }

    @Test
    public void testBindMapPrefixed() {
        handle.execute("insert into something (id, name) values (1, 'Alice')");
        dao.updatePrefix(1, Collections.singletonMap("name", "Alicia"));
        assertThat(dao.get(1).getName()).isEqualTo("Alicia");
    }

    @Test
    public void testBindMapKeyInKeysAndMap() {
        handle.execute("insert into something (id, name) values (2, 'Bob')");
        dao.updateNameKey(2, Collections.singletonMap("name", "Rob"));
        assertThat(dao.get(2).getName()).isEqualTo("Rob");
    }

    @Test
    public void testBindMapKeyInKeysNotInMap() {
        handle.execute("insert into something(id, name) values (2, 'Bob')");
        dao.updateNameKey(2, Collections.emptyMap());
        assertThat(dao.get(2).getName()).isNull();
    }

    @Test
    public void testBindMapKeyInMapNotInKeys() {
        handle.execute("insert into something(id, name) values (3, 'Carol')");
        dao.updateNameKey(3, ImmutableMap.of("name", "Cheryl", "integerValue", 3));
        assertThat(dao.get(3).getName()).isEqualTo("Cheryl");
        assertThat(dao.get(3).getIntegerValue()).isNull();
    }

    @Test
    public void testBindMapKeyNotInMapOrKeys() {
        handle.execute("insert into something(id, name) values (3, 'Carol')");
        exception.expect(UnableToCreateStatementException.class);
        dao.update(3, Collections.emptyMap());
    }

    @Test
    public void testBindMapConvertKeysStringKeys() {
        handle.execute("insert into something(id, name) values (4, 'Dave')");
        dao.updateConvertKeys(4, Collections.singletonMap("name", "David"));
        assertThat(dao.get(4).getName()).isEqualTo("David");
    }

    @Test
    public void testBindMapConvertKeysNonStringKeys() {
        handle.execute("insert into something(id, name) values (4, 'Dave')");
        dao.updateConvertKeys(4, Collections.singletonMap(new TestBindMap.MapKey("name"), "David"));
        assertThat(dao.get(4).getName()).isEqualTo("David");
    }

    @Test
    public void testBindMapNonStringKeys() {
        handle.execute("insert into something(id, name) values (5, 'Edward')");
        exception.expect(IllegalArgumentException.class);
        dao.update(5, Collections.singletonMap(new TestBindMap.MapKey("name"), "Jacob"));
    }

    public static class MapKey {
        private final String value;

        MapKey(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public interface Dao {
        @SqlUpdate("update something set name=:name where id=:id")
        void update(@Bind
        int id, @BindMap
        Map<Object, Object> map);

        @SqlUpdate("update something set name=:map.name where id=:id")
        void updatePrefix(@Bind
        int id, @BindMap("map")
        Map<String, Object> map);

        @SqlUpdate("update something set name=:name where id=:id")
        void updateNameKey(@Bind
        int id, @BindMap(keys = "name")
        Map<String, Object> map);

        @SqlUpdate("update something set name=:name where id=:id")
        void updateConvertKeys(@Bind
        int id, @BindMap(convertKeys = true)
        Map<Object, Object> map);

        @SqlQuery("select * from something where id=:id")
        @UseRowMapper(SomethingMapper.class)
        Something get(long id);
    }
}
