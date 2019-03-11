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


import io.crate.exceptions.OperationOnInaccessibleRelationException;
import io.crate.exceptions.RelationUnknown;
import io.crate.exceptions.SchemaUnknownException;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import java.util.Locale;
import org.hamcrest.Matchers;
import org.junit.Test;


public class DropTableAnalyzerTest extends CrateDummyClusterServiceUnitTest {
    private SQLExecutor e;

    @Test
    public void testDropNonExistingTable() throws Exception {
        expectedException.expect(RelationUnknown.class);
        expectedException.expectMessage("Relation 'unknown' unknown");
        e.analyze("drop table unknown");
    }

    @Test
    public void testDropSystemTable() throws Exception {
        expectedException.expect(OperationOnInaccessibleRelationException.class);
        expectedException.expectMessage(("The relation \"sys.cluster\" doesn\'t support or allow DROP " + "operations, as it is read-only."));
        e.analyze("drop table sys.cluster");
    }

    @Test
    public void testDropInformationSchemaTable() throws Exception {
        expectedException.expect(OperationOnInaccessibleRelationException.class);
        expectedException.expectMessage(("The relation \"information_schema.tables\" doesn\'t support or allow " + "DROP operations, as it is read-only."));
        e.analyze("drop table information_schema.tables");
    }

    @Test
    public void testDropUnknownSchema() throws Exception {
        expectedException.expect(SchemaUnknownException.class);
        expectedException.expectMessage("Schema 'unknown_schema' unknown");
        e.analyze("drop table unknown_schema.unknown");
    }

    @Test
    public void testDropTableIfExistsWithUnknownSchema() throws Exception {
        // shouldn't raise SchemaUnknownException / RelationUnknown
        e.analyze("drop table if exists unknown_schema.unknown");
    }

    @Test
    public void testDropExistingTable() throws Exception {
        AnalyzedStatement analyzedStatement = e.analyze(String.format(Locale.ENGLISH, "drop table %s", TableDefinitions.USER_TABLE_IDENT.name()));
        assertThat(analyzedStatement, Matchers.instanceOf(DropTableAnalyzedStatement.class));
        DropTableAnalyzedStatement dropTableAnalysis = ((DropTableAnalyzedStatement) (analyzedStatement));
        assertThat(dropTableAnalysis.dropIfExists(), Matchers.is(false));
        assertThat(dropTableAnalysis.index(), Matchers.is(TableDefinitions.USER_TABLE_IDENT.name()));
    }

    @Test
    public void testDropIfExistExistingTable() throws Exception {
        AnalyzedStatement analyzedStatement = e.analyze(String.format(Locale.ENGLISH, "drop table if exists %s", TableDefinitions.USER_TABLE_IDENT.name()));
        assertThat(analyzedStatement, Matchers.instanceOf(DropTableAnalyzedStatement.class));
        DropTableAnalyzedStatement dropTableAnalysis = ((DropTableAnalyzedStatement) (analyzedStatement));
        assertThat(dropTableAnalysis.dropIfExists(), Matchers.is(true));
        assertThat(dropTableAnalysis.index(), Matchers.is(TableDefinitions.USER_TABLE_IDENT.name()));
    }

    @Test
    public void testNonExistentTableIsRecognizedCorrectly() throws Exception {
        AnalyzedStatement analyzedStatement = e.analyze("drop table if exists unknowntable");
        assertThat(analyzedStatement, Matchers.instanceOf(DropTableAnalyzedStatement.class));
        DropTableAnalyzedStatement dropTableAnalysis = ((DropTableAnalyzedStatement) (analyzedStatement));
        assertThat(dropTableAnalysis.dropIfExists(), Matchers.is(true));
        assertThat(dropTableAnalysis.noop(), Matchers.is(true));
    }
}
