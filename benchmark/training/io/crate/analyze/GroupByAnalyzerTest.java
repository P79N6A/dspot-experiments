/**
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */
package io.crate.analyze;


import io.crate.analyze.relations.QueriedRelation;
import io.crate.exceptions.ColumnUnknownException;
import io.crate.expression.symbol.Function;
import io.crate.expression.symbol.Symbol;
import io.crate.metadata.ReferenceIdent;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import io.crate.testing.SymbolMatchers;
import io.crate.testing.TestingHelpers;
import java.util.List;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Test;


@SuppressWarnings("ConstantConditions")
public class GroupByAnalyzerTest extends CrateDummyClusterServiceUnitTest {
    private SQLExecutor sqlExecutor;

    @Test
    public void testGroupBySubscriptMissingOutput() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'load['5']' must appear in the GROUP BY clause");
        analyze("select load['5'] from sys.nodes group by load['1']");
    }

    @Test
    public void testGroupByOnAlias() throws Exception {
        QueriedRelation relation = analyze("select count(*), name as n from sys.nodes group by n");
        assertThat(relation.querySpec().groupBy().size(), Is.is(1));
        assertThat(relation.fields().get(0).path().outputName(), Is.is("count(*)"));
        assertThat(relation.fields().get(1).path().outputName(), Is.is("n"));
        assertEquals(relation.querySpec().groupBy().get(0), relation.querySpec().outputs().get(1));
    }

    @Test
    public void testGroupByOnOrdinal() throws Exception {
        // just like in postgres access by ordinal starts with 1
        QueriedRelation relation = analyze("select count(*), name as n from sys.nodes group by 2");
        assertThat(relation.querySpec().groupBy().size(), Is.is(1));
        assertEquals(relation.querySpec().groupBy().get(0), relation.querySpec().outputs().get(1));
    }

    @Test
    public void testGroupByOnOrdinalAggregation() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Aggregate functions are not allowed in GROUP BY");
        analyze("select count(*), name as n from sys.nodes group by 1");
    }

    @Test
    public void testGroupByWithDistinctAggregation() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Aggregate functions are not allowed in GROUP BY");
        analyze("select count(DISTINCT name) from sys.nodes group by 1");
    }

    @Test
    public void testGroupByScalarAliasedWithRealColumnNameFailsIfScalarColumnIsNotGrouped() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'(1 / height)' must appear in the GROUP BY clause");
        analyze("select 1/height as age from foo.users group by age");
    }

    @Test
    public void testGroupByScalarAliasAndValueInScalar() {
        QueriedRelation relation = analyze("select 1/age as age from foo.users group by age order by age");
        assertThat(relation.querySpec().groupBy().isEmpty(), Is.is(false));
        List<Symbol> groupBySymbols = relation.querySpec().groupBy();
        assertThat(column().fqn(), Is.is("age"));
    }

    @Test
    public void testGroupByScalarAlias() {
        // grouping by what's under the alias, the 1/age values
        QueriedRelation relation = analyze("select 1/age as theAlias from foo.users group by theAlias");
        assertThat(relation.querySpec().groupBy().isEmpty(), Is.is(false));
        List<Symbol> groupBySymbols = relation.querySpec().groupBy();
        Symbol groupBy = groupBySymbols.get(0);
        assertThat(groupBy, Matchers.instanceOf(Function.class));
        Function groupByFunction = ((Function) (groupBy));
        assertThat(column().fqn(), Is.is("age"));
    }

    @Test
    public void testGroupByColumnInScalar() {
        // grouping by height values
        QueriedRelation relation = analyze("select 1/age as height from foo.users group by age");
        assertThat(relation.querySpec().groupBy().isEmpty(), Is.is(false));
        List<Symbol> groupBySymbols = relation.querySpec().groupBy();
        assertThat(column().fqn(), Is.is("age"));
    }

    @Test
    public void testGroupByScalar() {
        QueriedRelation relation = analyze("select 1/age from foo.users group by 1/age;");
        assertThat(relation.querySpec().groupBy().isEmpty(), Is.is(false));
        List<Symbol> groupBySymbols = relation.querySpec().groupBy();
        Symbol groupBy = groupBySymbols.get(0);
        assertThat(groupBy, Matchers.instanceOf(Function.class));
    }

    @Test
    public void testGroupByAliasedLiteral() {
        QueriedRelation relation = analyze("select 58 as fiftyEight from foo.users group by fiftyEight;");
        assertThat(relation.querySpec().groupBy().isEmpty(), Is.is(false));
        List<Symbol> groupBySymbols = relation.querySpec().groupBy();
        assertThat(groupBySymbols.get(0).symbolType().isValueSymbol(), Is.is(true));
    }

    @Test
    public void testGroupByLiteralAliasedWithRealColumnNameGroupsByColumnValue() {
        QueriedRelation relation = analyze("select 58 as age from foo.users group by age;");
        assertThat(relation.querySpec().groupBy().isEmpty(), Is.is(false));
        List<Symbol> groupBySymbols = relation.querySpec().groupBy();
        ReferenceIdent groupByIdent = ident();
        assertThat(groupByIdent.columnIdent().fqn(), Is.is("age"));
        assertThat(groupByIdent.tableIdent().fqn(), Is.is("foo.users"));
    }

    @Test
    public void testNegateAliasRealColumnGroupByAlias() {
        QueriedRelation relation = analyze("select age age, - age age from foo.users group by age;");
        assertThat(relation.querySpec().groupBy().isEmpty(), Is.is(false));
        List<Symbol> groupBySymbols = relation.querySpec().groupBy();
        ReferenceIdent groupByIdent = ident();
        assertThat(groupByIdent.columnIdent().fqn(), Is.is("age"));
        assertThat(groupByIdent.tableIdent().fqn(), Is.is("foo.users"));
    }

    @Test
    public void testGroupBySubscript() throws Exception {
        QueriedRelation relation = analyze("select load['1'], count(*) from sys.nodes group by load['1']");
        assertThat(relation.querySpec().limit(), Matchers.nullValue());
        assertThat(relation.querySpec().groupBy(), Matchers.notNullValue());
        assertThat(relation.querySpec().outputs().size(), Is.is(2));
        assertThat(relation.querySpec().groupBy().size(), Is.is(1));
        assertThat(relation.querySpec().groupBy().get(0), SymbolMatchers.isReference("load['1']"));
    }

    @Test
    public void testSelectGroupByOrderByWithColumnMissingFromSelect() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage(("ORDER BY expression 'id' must appear in the select clause " + "when grouping or global aggregation is used"));
        analyze("select name, count(id) from users group by name order by id");
    }

    @Test
    public void testSelectGroupByOrderByWithAggregateFunctionInOrderByClause() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage(("ORDER BY function 'max(count(upper(name)))' is not allowed. " + "Only scalar functions can be used"));
        analyze("select name, count(id) from users group by name order by max(count(upper(name)))");
    }

    @Test
    public void testSelectAggregationMissingGroupBy() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'name' must appear in the GROUP BY clause");
        analyze("select name, count(id) from users");
    }

    @Test
    public void testSelectGlobalDistinctAggregationMissingGroupBy() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'name' must appear in the GROUP BY clause");
        analyze("select distinct name, count(id) from users");
    }

    @Test
    public void testSelectDistinctWithGroupBy() {
        QueriedRelation relation = analyze("select distinct max(id) from users group by name order by 1");
        assertThat(relation.isDistinct(), Is.is(true));
        assertThat(relation.querySpec(), TestingHelpers.isSQL("SELECT max(doc.users.id) GROUP BY doc.users.name ORDER BY max(doc.users.id)"));
    }

    @Test
    public void testSelectDistinctWithGroupByLimitAndOffset() {
        QueriedRelation relation = analyze("select distinct max(id) from users group by name order by 1 limit 5 offset 10");
        assertThat(relation.isDistinct(), Is.is(true));
        assertThat(relation.querySpec(), TestingHelpers.isSQL(("SELECT max(doc.users.id) GROUP BY doc.users.name " + "ORDER BY max(doc.users.id) LIMIT 5 OFFSET 10")));
    }

    @Test
    public void testSelectDistinctWithGroupByOnJoin() {
        QueriedRelation relation = analyze(("select DISTINCT max(users.id) from users " + ("  inner join users_multi_pk on users.id = users_multi_pk.id " + "group by users.name order by 1")));
        assertThat(relation, Matchers.instanceOf(MultiSourceSelect.class));
        assertThat(relation.isDistinct(), Is.is(true));
        assertThat(relation.querySpec(), TestingHelpers.isSQL("SELECT max(doc.users.id) GROUP BY doc.users.name ORDER BY max(doc.users.id)"));
    }

    @Test
    public void testSelectDistinctWithGroupByOnSubSelectOuter() {
        QueriedRelation relation = analyze(("select distinct max(id) from (" + ("  select * from users order by name limit 10" + ") t group by name order by 1")));
        assertThat(relation.isDistinct(), Is.is(true));
        assertThat(relation.querySpec(), TestingHelpers.isSQL(("SELECT max(t.id) " + ("GROUP BY t.name " + "ORDER BY max(t.id)"))));
    }

    @Test
    public void testSelectDistinctWithGroupByOnSubSelectInner() {
        QueriedRelation relation = analyze(("select * from (" + ("  select distinct id from users group by id, name order by 1" + ") t order by 1 desc")));
        assertThat(relation, Matchers.instanceOf(QueriedSelectRelation.class));
        QueriedSelectRelation outerRelation = ((QueriedSelectRelation) (relation));
        assertThat(outerRelation.outputs(), Matchers.contains(SymbolMatchers.isField("id")));
        assertThat(outerRelation.groupBy(), Matchers.empty());
        assertThat(outerRelation.orderBy().orderBySymbols(), Matchers.contains(SymbolMatchers.isField("id")));
        QueriedRelation innerRelation = outerRelation.subRelation();
        assertThat(innerRelation.isDistinct(), Is.is(true));
        assertThat(innerRelation.groupBy(), Matchers.contains(SymbolMatchers.isReference("id"), SymbolMatchers.isReference("name")));
    }

    @Test
    public void testGroupByOnLiteral() throws Exception {
        QueriedRelation relation = analyze("select [1,2,3], count(*) from users u group by 1");
        assertThat(relation.querySpec().outputs(), TestingHelpers.isSQL("[1, 2, 3], count()"));
        assertThat(relation.querySpec().groupBy(), TestingHelpers.isSQL("[1, 2, 3]"));
    }

    @Test
    public void testGroupByOnNullLiteral() throws Exception {
        QueriedRelation relation = analyze("select null, count(*) from users u group by 1");
        assertThat(relation.querySpec().outputs(), TestingHelpers.isSQL("NULL, count()"));
        assertThat(relation.querySpec().groupBy(), TestingHelpers.isSQL("NULL"));
    }

    @Test
    public void testGroupWithInvalidOrdinal() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("GROUP BY position 2 is not in select list");
        analyze("select name from users u group by 2");
    }

    @Test
    public void testGroupWithInvalidLiteral() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot use 'abc' in GROUP BY clause");
        analyze("select max(id) from users u group by 'abc'");
    }

    @Test
    public void testGroupByOnInvalidNegateLiteral() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("GROUP BY position -4 is not in select list");
        analyze("select count(*), name from sys.nodes group by -4");
    }

    @Test
    public void testGroupWithInvalidNullLiteral() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot use NULL in GROUP BY clause");
        analyze("select max(id) from users u group by NULL");
    }

    @Test
    public void testGroupByHaving() throws Exception {
        QueriedRelation relation = analyze("select sum(floats) from users group by name having name like 'Slartibart%'");
        assertThat(relation.querySpec().having().query(), SymbolMatchers.isFunction("op_like"));
        Function havingFunction = ((Function) (relation.querySpec().having().query()));
        assertThat(havingFunction.arguments().size(), Is.is(2));
        assertThat(havingFunction.arguments().get(0), SymbolMatchers.isReference("name"));
        assertThat(havingFunction.arguments().get(1), SymbolMatchers.isLiteral("Slartibart%"));
    }

    @Test
    public void testGroupByHavingAliasForRealColumn() {
        QueriedRelation relation = analyze("select id as name from users group by id, name having name != null;");
        HavingClause havingClause = relation.querySpec().having();
        assertThat(havingClause.query(), Matchers.nullValue());
    }

    @Test
    public void testGroupByHavingNormalize() throws Exception {
        QueriedRelation rel = analyze("select sum(floats) from users group by name having 1 > 4");
        HavingClause having = rel.having();
        assertThat(having.noMatch(), Is.is(true));
        assertNull(having.query());
    }

    @Test
    public void testGroupByHavingOtherColumnInAggregate() throws Exception {
        QueriedRelation relation = analyze("select sum(floats), name from users group by name having max(bytes) = 4");
        assertThat(relation.querySpec().having().query(), SymbolMatchers.isFunction("op_="));
        Function havingFunction = ((Function) (relation.querySpec().having().query()));
        assertThat(havingFunction.arguments().size(), Is.is(2));
        assertThat(havingFunction.arguments().get(0), SymbolMatchers.isFunction("max"));
        Function maxFunction = ((Function) (havingFunction.arguments().get(0)));
        assertThat(maxFunction.arguments().get(0), SymbolMatchers.isReference("bytes"));
        assertThat(havingFunction.arguments().get(1), SymbolMatchers.isLiteral(((byte) (4))));
    }

    @Test
    public void testGroupByHavingOtherColumnOutsideAggregate() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot use column bytes outside of an Aggregation in HAVING clause");
        analyze("select sum(floats) from users group by name having bytes = 4");
    }

    @Test
    public void testGroupByHavingOtherColumnOutsideAggregateInFunction() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot use column bytes outside of an Aggregation in HAVING clause");
        analyze("select sum(floats), name from users group by name having (bytes + 1)  = 4");
    }

    @Test
    public void testGroupByHavingByGroupKey() throws Exception {
        QueriedRelation relation = analyze("select sum(floats), name from users group by name having name like 'Slartibart%'");
        assertThat(relation.querySpec().having().query(), SymbolMatchers.isFunction("op_like"));
        Function havingFunction = ((Function) (relation.querySpec().having().query()));
        assertThat(havingFunction.arguments().size(), Is.is(2));
        assertThat(havingFunction.arguments().get(0), SymbolMatchers.isReference("name"));
        assertThat(havingFunction.arguments().get(1), SymbolMatchers.isLiteral("Slartibart%"));
    }

    @Test
    public void testGroupByHavingComplex() throws Exception {
        QueriedRelation relation = analyze(("select sum(floats), name from users " + "group by name having 1=0 or sum(bytes) in (42, 43, 44) and  name not like 'Slartibart%'"));
        assertThat(relation.querySpec().having().hasQuery(), Is.is(true));
        Function andFunction = ((Function) (relation.querySpec().having().query()));
        assertThat(andFunction, Is.is(Matchers.notNullValue()));
        assertThat(andFunction.info().ident().name(), Is.is("op_and"));
        assertThat(andFunction.arguments().size(), Is.is(2));
        assertThat(andFunction.arguments().get(0), SymbolMatchers.isFunction("any_="));
        assertThat(andFunction.arguments().get(1), SymbolMatchers.isFunction("op_not"));
    }

    @Test
    public void testGroupByHavingRecursiveFunction() throws Exception {
        QueriedRelation relation = analyze(("select sum(floats), name from users " + "group by name having sum(power(power(id::double, id::double), id::double)) > 0"));
        assertThat(relation.querySpec().having().query(), TestingHelpers.isSQL("(sum(power(power(to_double(doc.users.id), to_double(doc.users.id)), to_double(doc.users.id))) > 0.0)"));
    }

    @Test
    public void testGroupByHiddenColumn() throws Exception {
        expectedException.expect(ColumnUnknownException.class);
        expectedException.expectMessage("Column _docid unknown");
        analyze("select count(*) from users group by _docid");
    }
}
