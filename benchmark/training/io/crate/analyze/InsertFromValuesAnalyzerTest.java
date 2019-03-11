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
package io.crate.analyze;


import ArithmeticFunctions.Names.ADD;
import DataTypes.INTEGER;
import DataTypes.STRING;
import com.google.common.collect.ImmutableMap;
import io.crate.exceptions.ColumnUnknownException;
import io.crate.exceptions.ColumnValidationException;
import io.crate.exceptions.InvalidColumnNameException;
import io.crate.exceptions.OperationOnInaccessibleRelationException;
import io.crate.exceptions.ValidationException;
import io.crate.expression.symbol.Function;
import io.crate.expression.symbol.Symbol;
import io.crate.metadata.ColumnIdent;
import io.crate.metadata.RelationName;
import io.crate.metadata.Routing;
import io.crate.metadata.Schemas;
import io.crate.metadata.doc.DocTableInfo;
import io.crate.metadata.table.TestingTableInfo;
import io.crate.sql.parser.ParsingException;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import io.crate.testing.SymbolMatchers;
import io.crate.types.ObjectType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Test;


public class InsertFromValuesAnalyzerTest extends CrateDummyClusterServiceUnitTest {
    private static final RelationName NESTED_CLUSTERED_TABLE_IDENT = new RelationName(Schemas.DOC_SCHEMA_NAME, "nested_clustered");

    private static final DocTableInfo NESTED_CLUSTERED_TABLE_INFO = new TestingTableInfo.Builder(InsertFromValuesAnalyzerTest.NESTED_CLUSTERED_TABLE_IDENT, new Routing(ImmutableMap.of())).add("o", ObjectType.builder().setInnerType("c", STRING).build(), null).add("o2", ObjectType.builder().setInnerType("p", STRING).build(), null).add("k", INTEGER, null).clusteredBy("o.c").addPrimaryKey("o2.p").build();

    private static final RelationName THREE_PK_TABLE_IDENT = new RelationName(Schemas.DOC_SCHEMA_NAME, "three_pk");

    private static final DocTableInfo THREE_PK_TABLE_INFO = new TestingTableInfo.Builder(InsertFromValuesAnalyzerTest.THREE_PK_TABLE_IDENT, new Routing(ImmutableMap.of())).add("a", INTEGER).add("b", INTEGER).add("c", INTEGER).add("d", INTEGER).addPrimaryKey("a").addPrimaryKey("b").addPrimaryKey("c").build();

    private SQLExecutor e;

    @Test
    public void testInsertWithColumns() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, name) values (1, 'Trillian')");
        assertThat(analysis.tableInfo().ident(), Is.is(TableDefinitions.USER_TABLE_IDENT));
        assertThat(analysis.columns().size(), Is.is(2));
        assertThat(analysis.columns().get(0).column().name(), Is.is("id"));
        assertEquals(DataTypes.LONG, analysis.columns().get(0).valueType());
        assertThat(analysis.columns().get(1).column().name(), Is.is("name"));
        assertEquals(STRING, analysis.columns().get(1).valueType());
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0).length, Is.is(2));
        assertThat(analysis.sourceMaps().get(0)[0], Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is("Trillian"));
    }

    @Test
    public void testInsertWithTwistedColumns() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (name, id) values ('Trillian', 2)");
        assertThat(analysis.tableInfo().ident(), Is.is(TableDefinitions.USER_TABLE_IDENT));
        assertThat(analysis.columns().size(), Is.is(2));
        assertThat(analysis.columns().get(0).column().name(), Is.is("name"));
        assertEquals(STRING, analysis.columns().get(0).valueType());
        assertThat(analysis.columns().get(1).column().name(), Is.is("id"));
        assertEquals(DataTypes.LONG, analysis.columns().get(1).valueType());
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0).length, Is.is(2));
        assertThat(analysis.sourceMaps().get(0)[0], Is.is("Trillian"));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is(2L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithColumnsAndTooManyValues() throws Exception {
        e.analyze("insert into users (name, id) values ('Trillian', 2, true)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithColumnsAndTooLessValues() throws Exception {
        e.analyze("insert into users (name, id) values ('Trillian')");
    }

    @Test(expected = ValidationException.class)
    public void testInsertWithWrongType() throws Exception {
        e.analyze("insert into users (name, id) values (1, 'Trillian')");
    }

    @Test
    public void testInsertWithNumericTypeOutOfRange() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage("Validation failed for bytes: Cannot cast 1234 to type byte");
        e.analyze("insert into users (name, id, bytes) values ('Trillian', 4, 1234)");
    }

    @Test(expected = ValidationException.class)
    public void testInsertWithWrongParameterType() throws Exception {
        e.analyze("insert into users (name, id) values (?, ?)", new Object[]{ 1, true });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertSameReferenceRepeated() throws Exception {
        e.analyze("insert into users (name, name) values ('Trillian', 'Ford')");
    }

    @Test
    public void testInsertWithConvertedTypes() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, name, awesome) values ($1, 'Trillian', $2)", new Object[]{ 1.0F, "true" });
        assertEquals(DataTypes.LONG, analysis.columns().get(0).valueType());
        assertEquals(DataTypes.BOOLEAN, analysis.columns().get(2).valueType());
        assertThat(analysis.sourceMaps().get(0).length, Is.is(3));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1L));
        assertThat(((Boolean) (analysis.sourceMaps().get(0)[2])), Is.is(true));
    }

    @Test
    public void testInsertWithFunction() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, name) values (ABS(-1), 'Trillian')");
        assertThat(analysis.tableInfo().ident(), Is.is(TableDefinitions.USER_TABLE_IDENT));
        assertThat(analysis.columns().size(), Is.is(2));
        assertThat(analysis.columns().get(0).column().name(), Is.is("id"));
        assertEquals(DataTypes.LONG, analysis.columns().get(0).valueType());
        assertThat(analysis.columns().get(1).column().name(), Is.is("name"));
        assertEquals(STRING, analysis.columns().get(1).valueType());
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0).length, Is.is(2));
        assertThat(analysis.sourceMaps().get(0)[0], Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is("Trillian"));
    }

    @Test
    public void testInsertWithoutColumns() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, other_id, name) values (1, 1, 'Trillian')");
        assertThat(analysis.tableInfo().ident(), Is.is(TableDefinitions.USER_TABLE_IDENT));
        assertThat(analysis.columns().size(), Is.is(3));
        assertThat(analysis.columns().get(0).column().name(), Is.is("id"));
        assertEquals(DataTypes.LONG, analysis.columns().get(0).valueType());
        assertThat(analysis.columns().get(1).column().name(), Is.is("other_id"));
        assertEquals(DataTypes.LONG, analysis.columns().get(1).valueType());
        assertThat(analysis.columns().get(2).column().name(), Is.is("name"));
        assertEquals(STRING, analysis.columns().get(2).valueType());
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0).length, Is.is(3));
        assertThat(analysis.sourceMaps().get(0)[0], Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[2], Is.is("Trillian"));
    }

    @Test
    public void testInsertWithoutColumnsAndOnlyOneColumn() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id) values (1)");
        assertThat(analysis.tableInfo().ident(), Is.is(TableDefinitions.USER_TABLE_IDENT));
        assertThat(analysis.columns().size(), Is.is(1));
        assertThat(analysis.columns().get(0).column().name(), Is.is("id"));
        assertEquals(DataTypes.LONG, analysis.columns().get(0).valueType());
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0).length, Is.is(1));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1L));
    }

    @Test
    public void testInsertWithInvalidColumnReference() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("invalid table column reference 'a'");
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users ('a') values (1)");
    }

    @Test
    public void testInsertIntoSysTable() throws Exception {
        expectedException.expect(OperationOnInaccessibleRelationException.class);
        expectedException.expectMessage(("The relation \"sys.nodes\" doesn\'t support or allow INSERT " + "operations, as it is read-only."));
        e.analyze("insert into sys.nodes (id, name) values (666, 'evilNode')");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithoutPrimaryKey() throws Exception {
        e.analyze("insert into users (name) values ('Trillian')");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPrimaryKey() throws Exception {
        e.analyze("insert into users (id) values (?)", new Object[]{ null });
    }

    @Test
    public void testNullLiterals() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, name, awesome, details) values (?, ?, ?, ?)", new Object[]{ 1, null, null, null });
        assertThat(analysis.sourceMaps().get(0).length, Is.is(4));
        assertNull(analysis.sourceMaps().get(0)[1]);
        assertNull(analysis.sourceMaps().get(0)[2]);
        assertNull(analysis.sourceMaps().get(0)[3]);
    }

    @Test
    public void testObjectLiterals() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, name, awesome, details) values (?, ?, ?, ?)", new Object[]{ 1, null, null, new HashMap<String, Object>() {
            {
                put("new_col", "new value");
            }
        } });
        assertThat(analysis.sourceMaps().get(0).length, Is.is(4));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[3], Matchers.instanceOf(Map.class));
    }

    @Test(expected = ColumnValidationException.class)
    public void testInsertArrays() throws Exception {
        // error because in the schema are non-array types:
        e.analyze("insert into users (id, name, awesome, details) values (?, ?, ?, ?)", new Object[]{ new Long[]{ 1L, 2L }, new String[]{ "Karl Liebknecht", "Rosa Luxemburg" }, new Boolean[]{ true, false }, new Map[]{ new HashMap<String, Object>(), new HashMap<String, Object>() } });
    }

    @Test
    public void testInsertObjectArrayParameter() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, friends) values(?, ?)", new Object[]{ 0, new Map[]{ new HashMap<String, Object>() {
            {
                put("name", "Jeltz");
            }
        }, new HashMap<String, Object>() {
            {
                put("name", "Prosser");
            }
        } } });
        assertThat(analysis.sourceMaps().get(0).length, Is.is(2));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(0L));
        assertArrayEquals(((Object[]) (analysis.sourceMaps().get(0)[1])), new Object[]{ new org.elasticsearch.common.collect.MapBuilder<String, Object>().put("name", "Jeltz").map(), new org.elasticsearch.common.collect.MapBuilder<String, Object>().put("name", "Prosser").map() });
    }

    @Test(expected = ColumnValidationException.class)
    public void testInsertInvalidObjectArrayParameter1() throws Exception {
        e.analyze("insert into users (id, friends) values(?, ?)", new Object[]{ 0, new Map[]{ new HashMap<String, Object>() {
            {
                put("id", "Jeltz");
            }
        } } });
    }

    @Test(expected = ColumnValidationException.class)
    public void testInsertInvalidObjectArrayParameter2() throws Exception {
        e.analyze("insert into users (id, friends) values(?, ?)", new Object[]{ 0, new Map[]{ new HashMap<String, Object>() {
            {
                put("id", 1L);
                put("groups", "a");
            }
        } } });
    }

    @Test
    public void testInsertInvalidObjectArrayInObject() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage("Validation failed for details: invalid value for object array type");
        e.analyze(("insert into deeply_nested (details) " + (("values (" + "  {awesome=true, arguments=[1,2,3]}") + ")")));
    }

    @Test
    public void testInsertInvalidObjectArrayFieldInObjectArray() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage("Validation failed for tags['metadata']['id']: Invalid long");
        e.analyze(("insert into deeply_nested (tags) " + ((((("values (" + "  [") + "    {name='right', metadata=[{id=1}, {id=2}]},") + "    {name='wrong', metadata=[{id='foo'}]}") + "  ]") + ")")));
    }

    @Test
    public void testInsertNestedObjectLiteral() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into deeply_nested (tags) " + ((("values ([" + "           {\"name\"=\'cool\', \"metadata\"=[{\"id\"=0}, {\"id\"=1}]}, ") + "           {\"name\"=\'fancy\', \"metadata\"=[{\"id\"=\'2\'}, {\"id\"=3}]}") + "         ])")));
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        Object[] arrayValue = ((Object[]) (analysis.sourceMaps().get(0)[0]));
        assertThat(arrayValue.length, Is.is(2));
        assertThat(arrayValue[0], Matchers.instanceOf(Map.class));
        assertThat(((Map) (arrayValue[0])).get("name"), Is.is("cool"));
        assertThat(((Map) (arrayValue[1])).get("name"), Is.is("fancy"));
        assertThat(Arrays.toString(((Object[]) (((Map) (arrayValue[0])).get("metadata")))), Is.is("[{id=0}, {id=1}]"));
        assertThat(Arrays.toString(((Object[]) (((Map) (arrayValue[1])).get("metadata")))), Is.is("[{id=2}, {id=3}]"));
    }

    @Test
    public void testInsertEmptyObjectArrayParameter() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, friends) values(?, ?)", new Object[]{ 0, new Map[0] });
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(0L));
        assertThat(((Object[]) (analysis.sourceMaps().get(0)[1])).length, Is.is(0));
    }

    @Test(expected = InvalidColumnNameException.class)
    public void testInsertSystemColumn() throws Exception {
        e.analyze("insert into users (id, _id) values (?, ?)", new Object[]{ 1, "1" });
    }

    @Test
    public void testNestedPk() throws Exception {
        // FYI: insert nested clustered by test here too
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into nested_pk (id, o) values (?, ?)", new Object[]{ 1, new org.elasticsearch.common.collect.MapBuilder<String, Object>().put("b", 4).map() });
        assertThat(analysis.ids().size(), Is.is(1));
        assertThat(analysis.ids().get(0), Is.is(generateId(Arrays.asList(new ColumnIdent("id"), new ColumnIdent("o.b")), Arrays.asList("1", "4"), new ColumnIdent("o.b"))));
    }

    @Test
    public void testNestedPkAllColumns() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into nested_pk values (?, ?)", new Object[]{ 1, new org.elasticsearch.common.collect.MapBuilder<String, Object>().put("b", 4).map() });
        assertThat(analysis.ids().size(), Is.is(1));
        assertThat(analysis.ids().get(0), Is.is(generateId(Arrays.asList(new ColumnIdent("id"), new ColumnIdent("o.b")), Arrays.asList("1", "4"), new ColumnIdent("o.b"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingNestedPk() throws Exception {
        e.analyze("insert into nested_pk (id) values (?)", new Object[]{ 1 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingNestedPkInMap() throws Exception {
        e.analyze("insert into nested_pk (id, o) values (?, ?)", new Object[]{ 1, new HashMap<String, Object>() });
    }

    @Test
    public void testTwistedNestedPk() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into nested_pk (o, id) values (?, ?)", new Object[]{ new org.elasticsearch.common.collect.MapBuilder<String, Object>().put("b", 4).map(), 1 });
        assertThat(analysis.ids().get(0), Is.is(generateId(Arrays.asList(new ColumnIdent("id"), new ColumnIdent("o.b")), Arrays.asList("1", "4"), new ColumnIdent("o.b"))));
    }

    @Test
    public void testInsertMultipleValues() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into users (id, name, awesome) values (?, ?, ?), (?, ?, ?)", new Object[]{ 99, "Marvin", true, 42, "Deep Thought", false });
        assertThat(analysis.sourceMaps().size(), Is.is(2));
        assertThat(analysis.sourceMaps().get(0)[0], Is.is(99L));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is("Marvin"));
        assertThat(analysis.sourceMaps().get(0)[2], Is.is(true));
        assertThat(analysis.sourceMaps().get(1)[0], Is.is(42L));
        assertThat(analysis.sourceMaps().get(1)[1], Is.is("Deep Thought"));
        assertThat(analysis.sourceMaps().get(1)[2], Is.is(false));
    }

    @Test
    public void testInsertPartitionedTable() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into parted (id, name, date) " + "values (?, ?, ?)"), new Object[]{ 0, "Trillian", 0L });
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0).length, Is.is(3));
        assertThat(analysis.columns().size(), Is.is(3));
        assertThat(analysis.partitionMaps().size(), Is.is(1));
        assertThat(analysis.partitionMaps().get(0), Matchers.hasEntry("date", "0"));
    }

    @Test
    public void testInsertIntoPartitionedTableOnlyPartitionColumns() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into parted (date) " + "values (?)"), new Object[]{ 0L });
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0).length, Is.is(1));
        assertThat(analysis.columns().size(), Is.is(1));
        assertThat(analysis.partitionMaps().size(), Is.is(1));
        assertThat(analysis.partitionMaps().get(0), Matchers.hasEntry("date", "0"));
    }

    @Test
    public void bulkIndexPartitionedTable() throws Exception {
        // multiple values
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into parted (id, name, date) " + "values (?, ?, ?), (?, ?, ?), (?, ?, ?)"), new Object[]{ 1, "Trillian", 13963670051500L, 2, "Ford", 0L, 3, "Zaphod", null });
        validateBulkIndexPartitionedTableAnalysis(analysis);
        // bulk args
        analysis = e.analyze(("insert into parted (id, name, date) " + "values (?, ?, ?)"), new Object[][]{ new Object[]{ 1, "Trillian", 13963670051500L }, new Object[]{ 2, "Ford", 0L }, new Object[]{ 3, "Zaphod", null } });
        validateBulkIndexPartitionedTableAnalysis(analysis);
    }

    @Test
    public void testInsertWithMatchPredicateInValues() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage(("Validation failed for awesome: " + "Invalid value 'MATCH((name), 'bar') USING best_fields' in insert statement"));
        e.analyze("insert into users (id, awesome) values (1, match(name, 'bar'))");
    }

    @Test
    public void testInsertNestedPartitionedColumn() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into multi_parted (id, date, obj)" + "values (?, ?, ?), (?, ?, ?)"), new Object[]{ 1, "1970-01-01", new org.elasticsearch.common.collect.MapBuilder<String, Object>().put("name", "Zaphod").map(), 2, "2014-05-21", new org.elasticsearch.common.collect.MapBuilder<String, Object>().put("name", "Arthur").map() });
        assertThat(analysis.generatePartitions(), Matchers.contains(asIndexName(), asIndexName()));
        assertThat(analysis.sourceMaps().size(), Is.is(2));
    }

    @Test
    public void testInsertWithBulkArgs() throws Exception {
        InsertFromValuesAnalyzedStatement analysis;
        analysis = e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{ 1, "foo" }, new Object[]{ 2, "bar" } });
        assertThat(analysis.sourceMaps().size(), Is.is(2));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1L));
        assertThat(((Long) (analysis.sourceMaps().get(1)[0])), Is.is(2L));
    }

    @Test
    public void testInsertWithBulkArgsMultiValue() throws Exception {
        // should be equal to testInsertWithBulkArgs()
        InsertFromValuesAnalyzedStatement analysis;
        analysis = e.analyze("insert into users (id, name) values (?, ?), (?, ?)", new Object[][]{ new Object[]{ 1, "foo", 2, "bar" }// one bulk row
        // one bulk row
        // one bulk row
         });
        assertThat(analysis.sourceMaps().size(), Is.is(2));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1L));
        assertThat(((Long) (analysis.sourceMaps().get(1)[0])), Is.is(2L));
    }

    @Test
    public void testInsertWithBulkArgsTypeMissMatch() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage("Validation failed for id: Cannot cast '11!' to type long");
        e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{ 10, "foo" }, new Object[]{ "11!", "bar" } });
    }

    @Test
    public void testInsertWithBulkArgsMixedLength() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("mixed number of arguments inside bulk arguments");
        e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{ 10, "foo" }, new Object[]{ "11" } });
    }

    @Test
    public void testInsertWithBulkArgsNullValues() throws Exception {
        InsertFromValuesAnalyzedStatement analysis;
        analysis = e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{ 10, "foo" }, new Object[]{ 11, null } });
        assertThat(analysis.sourceMaps().size(), Is.is(2));
        assertEquals(analysis.sourceMaps().get(0)[1], "foo");
        assertEquals(analysis.sourceMaps().get(1)[1], null);
    }

    @Test
    public void testInsertWithBulkArgsNullValuesFirst() throws Exception {
        InsertFromValuesAnalyzedStatement analysis;
        analysis = e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{ 12, null }, new Object[]{ 13, "foo" } });
        assertThat(analysis.sourceMaps().size(), Is.is(2));
        assertEquals(analysis.sourceMaps().get(0)[1], null);
        assertEquals(analysis.sourceMaps().get(1)[1], "foo");
    }

    @Test
    public void testInsertWithBulkArgsArrayNullValuesFirst() throws Exception {
        InsertFromValuesAnalyzedStatement analysis;
        analysis = e.analyze("insert into users (id, new_col) values (?, ?)", new Object[][]{ new Object[]{ 12, new String[]{ null } }, new Object[]{ 13, new String[]{ "foo" } } });
        assertThat(analysis.sourceMaps().size(), Is.is(2));
        assertThat(((Object[]) (analysis.sourceMaps().get(0)[1])), Matchers.arrayContaining(((Object) (null))));
        assertThat(((Object[]) (analysis.sourceMaps().get(1)[1])), Matchers.arrayContaining(((Object) ("foo"))));
    }

    @Test
    public void testInsertBulkArgWithFirstArgsContainsUnrecognizableObject() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(Matchers.allOf(Matchers.startsWith("Got an argument \""), Matchers.endsWith("that couldn't be recognized")));
        e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{ new InsertFromValuesAnalyzerTest.Foo() } });
    }

    @Test
    public void testInsertBulkArgWithUnrecognizableObject() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(Matchers.allOf(Matchers.startsWith("Got an argument \""), Matchers.endsWith("that couldn't be recognized")));
        e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{ 1, "Arthur" }, new Object[]{ new InsertFromValuesAnalyzerTest.Foo(), "Ford" } });
    }

    private static class Foo {}

    @Test
    public void testInsertWithTooFewArguments() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Tried to resolve a parameter but the arguments provided with the SQLRequest don't contain a parameter at position 1");
        e.analyze("insert into users (id, name) values (?, ?)", new Object[]{ 1 });
    }

    @Test
    public void testInsertWithTooFewBulkArguments() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Tried to resolve a parameter but the arguments provided with the SQLRequest don't contain a parameter at position 0");
        e.analyze("insert into users (id, name) values (?, ?)", new Object[][]{ new Object[]{  }, new Object[]{  } });
    }

    @Test
    public void testInvalidTypeParamLiteral() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage(("Validation failed for tags: Cannot cast " + ("[['the', 'answer'], ['what''s', 'the', 'question', '?']] " + "to type string_array")));
        e.analyze("insert into users (id, name, tags) values (42, 'Deep Thought', [['the', 'answer'], ['what''s', 'the', 'question', '?']])");
    }

    @Test
    public void testInvalidTypeParam() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage(("Validation failed for tags: Cannot cast " + ("[['the', 'answer'], ['what''s', 'the', 'question', '?']] " + "to type string_array")));
        e.analyze("insert into users (id, name, tags) values (42, 'Deep Thought', ?)", new Object[]{ new String[][]{ new String[]{ "the", "answer" }, new String[]{ "what's", "the", "question", "?" } } });
    }

    @Test
    public void testInvalidTypeBulkParam() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage(("Validation failed for tags: Cannot cast " + ("[['the', 'answer'], ['what''s', 'the', 'question', '?']] " + "to type string_array")));
        e.analyze("insert into users (id, name, tags) values (42, 'Deep Thought', ?)", new Object[][]{ new Object[]{ new String[][]{ new String[]{ "the", "answer" }, new String[]{ "what's", "the", "question", "?" } } } });
    }

    @Test
    public void testDynamicNestedArrayParamLiteral() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into users (id, name, theses) " + "values (1, 'Marx', [['string1', 'string2']])"));
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0)[0], Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is("Marx"));
        assertThat(((Object[]) (((Object[]) (analysis.sourceMaps().get(0)[2]))[0])), Matchers.arrayContaining(new Object[]{ "string1", "string2" }));
    }

    @Test
    public void testDynamicNestedArrayParam() throws Exception {
        e.analyze("insert into users (id, name, theses) values (1, 'Marx', ?)", new Object[]{ new String[][]{ new String[]{ "string1" }, new String[]{ "string2" } } });
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into users (id, name, theses) " + "values (1, 'Marx', [['string1', 'string2']])"));
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is("Marx"));
        assertThat(((Object[]) (((Object[]) (analysis.sourceMaps().get(0)[2]))[0])), Matchers.arrayContaining(new Object[]{ "string1", "string2" }));
    }

    @Test
    public void testDynamicNestedArrayBulkParam() throws Exception {
        e.analyze("insert into users (id, name, theses) values (1, 'Marx', ?)", new Object[][]{ new Object[]{ new String[][]{ new String[]{ "string1" }, new String[]{ "string2" } } } });
        InsertFromValuesAnalyzedStatement analysis = e.analyze(("insert into users (id, name, theses) " + "values (1, 'Marx', [['string1', 'string2']])"));
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1L));
        assertThat(analysis.sourceMaps().get(0)[1], Is.is("Marx"));
        assertThat(((Object[]) (((Object[]) (analysis.sourceMaps().get(0)[2]))[0])), Matchers.arrayContaining(new Object[]{ "string1", "string2" }));
    }

    @Test
    public void testInvalidColumnName() throws Exception {
        expectedException.expect(InvalidColumnNameException.class);
        expectedException.expectMessage("\"newCol[\'a\']\" conflicts with subscript pattern");
        e.analyze("insert into users (\"newCol[\'a\']\") values(test)");
    }

    @Test
    public void testInsertIntoTableWithNestedObjectPrimaryKeyAndNullInsert() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Primary key value must not be NULL");
        e.analyze("insert into nested_pk (o) values (null)");
    }

    @Test
    public void testNestedPrimaryKeyColumnMustNotBeNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Primary key value must not be NULL");
        e.analyze("insert into nested_pk (o) values ({b=null})");
    }

    @Test
    public void testNestedClusteredByColumnMustNotBeNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Clustered by value must not be NULL");
        e.analyze("insert into nested_clustered (o, o2) values ({c=null}, {p=1})");
    }

    @Test
    public void testNestedClusteredByColumnMustNotBeNullWholeObject() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Clustered by value must not be NULL");
        e.analyze("insert into nested_clustered (o, o2) values (null, {p=1})");
    }

    @Test
    public void testInsertIntoTableWithNestedPartitionedByColumnAndNullValue() throws Exception {
        // caused an AssertionError before... now there should be an entry with value null in the partition map
        InsertFromValuesAnalyzedStatement statement = e.analyze("insert into multi_parted (obj) values (null)");
        assertThat(statement.partitionMaps().get(0).containsKey("obj.name"), Is.is(true));
        assertThat(statement.partitionMaps().get(0).get("obj.name"), Matchers.nullValue());
    }

    @Test
    public void testInsertFromValuesWithOnDuplicateKey() {
        var insert = "insert into users (id, name, other_id) values (1, 'Arthur', 10) " + ("on conflict (id) do update set name = substr(excluded.name, 1, 2), " + "other_id = other_id + 100");
        InsertFromValuesAnalyzedStatement statement = e.analyze(insert);
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(1));
        Symbol[] assignments = statement.onDuplicateKeyAssignments().get(0);
        assertThat(assignments.length, Is.is(2));
        assertThat(assignments[0], SymbolMatchers.isLiteral("Ar"));
        assertThat(assignments[1], SymbolMatchers.isFunction(ADD));
        Function function = ((Function) (assignments[1]));
        assertThat(function.arguments().get(0), SymbolMatchers.isReference("other_id"));
    }

    @Test
    public void testInsertFromValuesWithOnConflictAndMultiplePKs() {
        String insertStatement = "insert into three_pk (a, b, c) values (1, 2, 3) on conflict (a, b, c) do update set d = 1";
        InsertFromValuesAnalyzedStatement statement = e.analyze(insertStatement);
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(1));
        assertThat(statement.onDuplicateKeyAssignmentsColumns().size(), Is.is(1));
        String[] cols = statement.onDuplicateKeyAssignmentsColumns().get(0);
        assertThat(cols[0], Is.is("d"));
        Symbol[] assignments = statement.onDuplicateKeyAssignments().get(0);
        assertThat(assignments.length, Is.is(1));
        assertThat(assignments[0], SymbolMatchers.isLiteral(1));
    }

    @Test
    public void testInsertFromValuesWithOnConflictAndNestedColumn() {
        String insertStatement = "insert into nested_clustered (o, o2) values ({c=1}, {p=1}) on conflict (o2.p) do update set k = 1";
        InsertFromValuesAnalyzedStatement statement = e.analyze(insertStatement);
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(1));
        assertThat(statement.onDuplicateKeyAssignmentsColumns().size(), Is.is(1));
        String[] cols = statement.onDuplicateKeyAssignmentsColumns().get(0);
        assertThat(cols[0], Is.is("k"));
        Symbol[] assignments = statement.onDuplicateKeyAssignments().get(0);
        assertThat(assignments.length, Is.is(1));
        assertThat(assignments[0], SymbolMatchers.isLiteral(1));
    }

    @Test
    public void testInsertFromValuesWithOnDuplicateKeyInvalidColumnInValues() throws Exception {
        expectedException.expect(ColumnUnknownException.class);
        expectedException.expectMessage("Column does_not_exist unknown");
        e.analyze(("insert into users (id, name) values (1, 'Arthur') " + "on conflict (id) do update set name = values (does_not_exist)"));
    }

    @Test
    public void testInsertFromValuesWithOnConflictDoUpdateInvalidColumnInExcluded() {
        expectedException.expect(ColumnUnknownException.class);
        expectedException.expectMessage("Column does_not_exist unknown");
        e.analyze(("insert into users (id, name) values (1, 'Arthur') " + "on conflict (id) do update set name = excluded.does_not_exist"));
    }

    @Test
    public void testInsertFromValuesWithOnConflictDoUpdateAndValueUsage() {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("unknown function: values(string)");
        e.analyze(("insert into users (id, name) values (1, 'Arthur') " + "on conflict (id) do update set name = values (name)"));
    }

    @Test
    public void testInsertFromValuesWithOnDupKeyValuesWithNotInsertedColumnRef() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Referenced column 'name' isn't part of the column list of the INSERT statement");
        e.analyze("insert into users (id) values (1) on conflict (id) do update set name = excluded.name");
    }

    @Test
    public void testInsertFromValuesWithOnConflictUpdateExcludedWithNotInsertedColumnRef() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Referenced column 'name' isn't part of the column list of the INSERT statement");
        e.analyze("insert into users (id) values (1) on conflict (id) do update set name = excluded.name");
    }

    @Test
    public void testInsertFromValuesWithInvalidConflictTarget() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Conflict target ([id2]) did not match the primary key columns ([id])");
        e.analyze("insert into users (id) values (1) on conflict (id2) do update set name = excluded.name");
    }

    @Test
    public void testInsertFromValuesWithConflictTargetNotMatchingPK() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Number of conflict targets ([id, id2]) did not match the number of primary key columns ([id])");
        e.analyze("insert into users (id) values (1) on conflict (id, id2) do update set name = excluded.name");
    }

    @Test
    public void testInsertFromValuesWithConflictTargetNotMatchingMultiplePKs() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Number of conflict targets ([a, b]) did not match the number of primary key columns ([a, b, c])");
        e.analyze(("insert into three_pk (a, b, c) values (1, 2, 3) " + "on conflict (a, b) do update set d = 1"));
    }

    @Test
    public void testInsertFromValuesWithInvalidConflictTargetDoNothing() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Conflict target ([id2]) did not match the primary key columns ([id])");
        e.analyze("insert into users (id) values (1) on conflict (id2) DO NOTHING");
    }

    @Test
    public void testInsertFromValuesWithConflictTargetDoNothingNotMatchingPK() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Number of conflict targets ([id, id2]) did not match the number of primary key columns ([id])");
        e.analyze("insert into users (id) values (1) on conflict (id, id2) DO NOTHING");
    }

    @Test
    public void testInsertFromValuesWithConflictTargetDoNothingNotMatchingMultiplePKs() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Number of conflict targets ([a, b]) did not match the number of primary key columns ([a, b, c])");
        e.analyze(("insert into three_pk (a, b, c) values (1, 2, 3) " + "on conflict (a, b) DO NOTHING"));
    }

    @Test
    public void testInsertFromValuesWithMissingConflictTarget() {
        expectedException.expect(ParsingException.class);
        expectedException.expectMessage("line 1:50: mismatched input 'update' expecting 'NOTHING'");
        e.analyze("insert into users (id) values (1) on conflict do update set name = excluded.name");
    }

    @Test
    public void testInsertFromValuesWithOnDupKeyValuesWithReferenceToNull() throws Exception {
        var insert = "insert into users (id, name) values (1, null) on conflict (id) do update set name = excluded.name";
        InsertFromValuesAnalyzedStatement statement = e.analyze(insert);
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(1));
        Symbol[] assignments = statement.onDuplicateKeyAssignments().get(0);
        assertThat(assignments.length, Is.is(1));
        assertThat(assignments[0], SymbolMatchers.isLiteral(null, STRING));
    }

    @Test
    public void testInsertFromValuesWithOnDupKeyValuesWithParams() throws Exception {
        String insert = "insert into users (id, name) values (1, ?) on conflict (id) do update set name = excluded.name";
        InsertFromValuesAnalyzedStatement statement = e.analyze(insert, new Object[]{ "foobar" });
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(1));
        Symbol[] assignments = statement.onDuplicateKeyAssignments().get(0);
        assertThat(assignments.length, Is.is(1));
        assertThat(assignments[0], SymbolMatchers.isLiteral("foobar"));
    }

    @Test
    public void testInsertFromValuesWithOnDuplicateWithTwoRefsAndDifferentTypes() throws Exception {
        InsertFromValuesAnalyzedStatement statement = e.analyze(("insert into users (id, name) values (1, 'foobar') " + "on conflict (id) do update set name = awesome"));
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(1));
        Symbol symbol = statement.onDuplicateKeyAssignments().get(0)[0];
        assertThat(symbol, SymbolMatchers.isFunction("to_string"));
    }

    @Test
    public void testInsertFromMultipleValuesWithOnDuplicateKey() throws Exception {
        var insert = "insert into users (id, name) values (1, 'Arthur'), (2, 'Trillian') " + "on conflict (id) do update set name = substr(excluded.name, 1, 1)";
        InsertFromValuesAnalyzedStatement statement = e.analyze(insert);
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(2));
        Symbol[] assignments = statement.onDuplicateKeyAssignments().get(0);
        assertThat(assignments.length, Is.is(1));
        assertThat(assignments[0], SymbolMatchers.isLiteral("A"));
        assignments = statement.onDuplicateKeyAssignments().get(1);
        assertThat(assignments.length, Is.is(1));
        assertThat(assignments[0], SymbolMatchers.isLiteral("T"));
    }

    @Test
    public void testOnDuplicateKeyUpdateOnObjectColumn() throws Exception {
        var insert = "insert into users (id) values (1) on conflict (id) do update set details['foo'] = 'foobar'";
        InsertFromValuesAnalyzedStatement statement = e.analyze(insert);
        assertThat(statement.onDuplicateKeyAssignments().size(), Is.is(1));
        Symbol[] assignments = statement.onDuplicateKeyAssignments().get(0);
        assertThat(assignments.length, Is.is(1));
        assertThat(assignments[0], SymbolMatchers.isLiteral("foobar"));
    }

    @Test
    public void testInvalidLeftSideExpressionInOnDuplicateKey() throws Exception {
        try {
            e.analyze("insert into users (id, name) values (1, 'Arthur') on conflict (id) do update set [1, 2] = 1");
            fail("Analyze passed without a failure.");
        } catch (IllegalArgumentException e) {
            // this is what we want
        }
    }

    @Test
    public void testUpdateOnPartitionedColumnShouldRaiseAnError() {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage("Validation failed for date: Updating a partitioned-by column is not supported");
        e.analyze("update parted set date = 1");
    }

    @Test
    public void testUpdateOnClusteredByColumnShouldRaiseAnError() throws Exception {
        expectedException.expect(ColumnValidationException.class);
        expectedException.expectMessage("Validation failed for id: Updating a clustered-by column is not supported");
        e.analyze("update users_clustered_by_only set id = 10");
    }

    @Test
    public void testUpdateOnConflictDoNothingProducesEmptyUpdateAssignments() {
        InsertFromValuesAnalyzedStatement statement = e.analyze("insert into users (id, name) values (1, 'Jon') on conflict (id) DO NOTHING");
        assertThat(statement.isIgnoreDuplicateKeys(), Is.is(true));
        assertThat(statement.onDuplicateKeyAssignments(), Is.is(Matchers.empty()));
        // also test without the optional conflict target
        statement = e.analyze("insert into users (id, name) values (1, 'Jon') on conflict DO NOTHING");
        assertThat(statement.isIgnoreDuplicateKeys(), Is.is(true));
        assertThat(statement.onDuplicateKeyAssignments(), Is.is(Matchers.empty()));
    }

    @Test
    public void testInsertWithGeneratedColumn() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into generated_column (ts) values (?)", new Object[]{ "2015-11-18T11:11:00" });
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1447845060000L));
        assertThat(((Long) (analysis.sourceMaps().get(0)[1])), Is.is(1447804800000L));
    }

    @Test
    public void testInsertWithGeneratedColumnReferenceValueNullOrNotGiven() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into generated_column (ts) values (?)", new Object[]{ null });
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        Object[] values = analysis.sourceMaps().get(0);
        assertThat(values.length, Is.is(3));
        assertThat(values[0], Matchers.nullValue());
        // generated column 'day'
        assertThat(values[1], Matchers.nullValue());
        // generated column 'name'
        assertThat(values[2], Is.is("bar"));
    }

    @Test
    public void testInsertWithGeneratedColumnWithValueGiven() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into generated_column (ts, day) values (?, ?)", new Object[]{ "2015-11-18T11:11:00", 1447804800000L });
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(((Long) (analysis.sourceMaps().get(0)[0])), Is.is(1447845060000L));
        assertThat(((Long) (analysis.sourceMaps().get(0)[1])), Is.is(1447804800000L));
    }

    @Test
    public void testInsertWithGeneratedColumnWithInvalidValueGiven() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Given value 1449999900000 for generated column does not match defined generated expression value 1447804800000");
        e.analyze("insert into generated_column (ts, day) values (?, ?)", new Object[]{ "2015-11-18T11:11:00", 1449999900000L });
    }

    @Test
    public void testInsertNullValueWithGeneratedColumn() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("insert into generated_column (day) values (?)", new Object[]{ null });
        assertThat(analysis.sourceMaps().size(), Is.is(1));
        assertThat(analysis.sourceMaps().get(0)[0], Is.is(Matchers.nullValue()));
    }

    @Test
    public void testInsertMultipleValuesWithGeneratedColumn() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("INSERT INTO generated_column (ts, \"user\") values (\'1970-01-01\', {name=\'Johnny\'}), (\'1989-11-09T08:30:00\', {name=\'Egon\'})");
        assertThat(analysis.columns(), Matchers.hasSize(4));
        assertThat(analysis.columns(), Matchers.contains(SymbolMatchers.isReference("ts"), SymbolMatchers.isReference("user"), SymbolMatchers.isReference("day"), SymbolMatchers.isReference("name")));
        assertThat(analysis.sourceMaps(), Matchers.hasSize(2));
        Matcher<Object[]> firstRow = Matchers.arrayContaining(0L, ImmutableMap.<String, Object>of("name", "Johnny"), 0L, "Johnnybar");
        Matcher<Object[]> secondRow = Matchers.arrayContaining(626603400000L, ImmutableMap.<String, Object>of("name", "Egon"), 626572800000L, "Egonbar");
        assertThat(analysis.sourceMaps(), Matchers.contains(firstRow, secondRow));
    }

    @Test
    public void testInsertMultipleValuesWithGeneratedColumnGiven() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("INSERT INTO generated_column (ts, \"user\", day) values (\'1970-01-01\', {name=\'Johnny\'}, \'1970-01-01\'), (\'1989-11-09T08:30:00\', {name=\'Egon\'}, \'1989-11-09\')");
        assertThat(analysis.columns(), Matchers.hasSize(4));
        assertThat(analysis.columns(), Matchers.contains(SymbolMatchers.isReference("ts"), SymbolMatchers.isReference("user"), SymbolMatchers.isReference("day"), SymbolMatchers.isReference("name")));
        assertThat(analysis.sourceMaps(), Matchers.hasSize(2));
        Matcher<Object[]> firstRow = Matchers.arrayContaining(0L, ImmutableMap.<String, Object>of("name", "Johnny"), 0L, "Johnnybar");
        Matcher<Object[]> secondRow = Matchers.arrayContaining(626603400000L, ImmutableMap.<String, Object>of("name", "Egon"), 626572800000L, "Egonbar");
        assertThat(analysis.sourceMaps(), Matchers.contains(firstRow, secondRow));
    }

    @Test
    public void testInsertGeneratedPrimaryKeyColumn() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("INSERT INTO generated_pk_column (serial_no, product_no) values (1, 1)");
        assertThat(analysis.routingValues(), Matchers.contains("AgEyATI="));
        assertThat(analysis.ids().get(0), Is.is(generateId(Arrays.asList(new ColumnIdent("id"), new ColumnIdent("id2")), Arrays.asList("2", "2"), new ColumnIdent("id"))));
    }

    @Test
    public void testInsertGeneratedPrimaryKeyColumnWithMultiValues() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("INSERT INTO generated_pk_column (serial_no, product_no) values (1, 1), (2, 2)");
        assertThat(analysis.routingValues(), Matchers.contains("AgEyATI=", "AgEzATM="));
        assertThat(analysis.ids(), Matchers.contains(Is.is(generateId(Arrays.asList(new ColumnIdent("id"), new ColumnIdent("id2")), Arrays.asList("2", "2"), new ColumnIdent("id"))), Is.is(generateId(Arrays.asList(new ColumnIdent("id"), new ColumnIdent("id2")), Arrays.asList("3", "3"), new ColumnIdent("id")))));
    }

    @Test
    public void testInsertGeneratedPrimaryKeyAndPartedColumn() throws Exception {
        InsertFromValuesAnalyzedStatement analysis = e.analyze("INSERT INTO generated_pk_parted_column (ts, value) VALUES (1508848674000, 1)");
        assertThat(analysis.ids().size(), Is.is(1));
        assertThat(analysis.ids().get(0), Is.is(generateId(Arrays.asList(new ColumnIdent("value"), new ColumnIdent("part_key__generated")), Arrays.asList("1", "1508803200000"), null)));
        assertThat(analysis.generatePartitions().size(), Is.is(1));
    }

    @Test
    public void testInsertMultipleValuesTooManyValues() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("INSERT statement contains a VALUES clause with too many elements (3), expected (2)");
        e.analyze("INSERT INTO users (id, name) values (1, 'Johnny'), (2, 'Egon', 1234)");
    }

    @Test
    public void testInsertMultipleValuesWithGeneratedColumnAndTooFewValuesInSecondValues() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid number of values: Got 2 columns specified but 1 values");
        e.analyze("INSERT INTO generated_column (ts, username) values ('1970-01-01', {name='Johnny'}), ('1989-11-09T08:30:00')");
    }

    @Test
    public void testGeneratedPrimaryKeyMissing() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Primary key is required but is missing from the insert statement");
        e.analyze("INSERT INTO generated_pk_column (color) values ('green')");
    }

    @Test
    public void testGeneratedKeyPrimaryKeyPartMissing() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Primary key value must not be NULL");
        e.analyze("INSERT INTO generated_pk_column (serial_no) values (1)");
    }

    @Test
    public void testGeneratedClusteredByMissing() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Clustered by value is required but is missing from the insert statement");
        e.analyze("INSERT INTO generated_clustered_by_column (color) values ('black')");
    }

    @Test
    public void testNestedGeneratedClusteredByMissing() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Clustered by value is required but is missing from the insert statement");
        e.analyze("INSERT INTO generated_nested_clustered_by (name) values ('kill-o-zap blaster pistol')");
    }

    @Test
    public void testNestedGeneratedClusteredBy() throws Exception {
        InsertFromValuesAnalyzedStatement statement = e.analyze("INSERT INTO generated_nested_clustered_by (o) values ({serial_number=1})");
        assertThat(statement.routingValues(), Matchers.contains(Is.is("2")));
    }

    @Test
    public void testInsertArrayLiteralWithOneNullValue() throws Exception {
        InsertFromValuesAnalyzedStatement stmt = e.analyze("insert into users (id, tags) values (1, ['foo', 'bar', null])");
        assertThat(stmt.sourceMaps().get(0), Is.is(new Object[]{ 1L, new Object[]{ "foo", "bar", null } }));
        stmt = e.analyze("insert into users (id, tags) values (1, [null, 'foo', 'bar'])");
        assertThat(stmt.sourceMaps().get(0), Is.is(new Object[]{ 1L, new Object[]{ null, "foo", "bar" } }));
    }

    @Test
    public void testInsertArrayLiteralWithOnlyNullValues() throws Exception {
        InsertFromValuesAnalyzedStatement stmt = e.analyze("insert into users (id, tags) values (1, [null, null])");
        assertThat(stmt.sourceMaps().get(0), Is.is(new Object[]{ 1L, new Object[]{ null, null } }));
    }

    @Test
    public void testIdGenerationDoesNotDependOnPrimaryKeyInsertOrder() throws Exception {
        InsertFromValuesAnalyzedStatement stmt1 = e.analyze("insert into three_pk (a, b, c) values (1, 2, 3)");
        InsertFromValuesAnalyzedStatement stmt2 = e.analyze("insert into three_pk (c, b, a) values (3, 2, 1)");
        assertThat(stmt1.ids().get(0), Is.is(stmt2.ids().get(0)));
    }
}
