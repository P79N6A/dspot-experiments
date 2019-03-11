/**
 * SonarQube
 * Copyright (C) 2009-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.sql;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.db.dialect.H2;
import org.sonar.db.dialect.MsSql;
import org.sonar.db.dialect.MySql;
import org.sonar.db.dialect.Oracle;
import org.sonar.db.dialect.PostgreSql;


public class RenameTableBuilderTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void rename_table_on_h2() {
        RenameTableBuilderTest.verifySql(new H2(), "ALTER TABLE foo RENAME TO bar");
    }

    @Test
    public void rename_table_on_mssql() {
        RenameTableBuilderTest.verifySql(new MsSql(), "EXEC sp_rename 'foo', 'bar'");
    }

    @Test
    public void rename_table_on_mysql() {
        RenameTableBuilderTest.verifySql(new MySql(), "ALTER TABLE foo RENAME TO bar");
    }

    @Test
    public void rename_table_on_oracle() {
        RenameTableBuilderTest.verifySql(new Oracle(), "DROP TRIGGER foo_idt", "RENAME foo TO bar", "RENAME foo_seq TO bar_seq", "CREATE OR REPLACE TRIGGER bar_idt BEFORE INSERT ON bar FOR EACH ROW BEGIN IF :new.id IS null THEN SELECT bar_seq.nextval INTO :new.id FROM dual; END IF; END;");
    }

    @Test
    public void rename_table_on_oracle_when_auto_generated_id_is_false() {
        RenameTableBuilderTest.verifySqlWhenAutoGeneratedIdIsFalse(new Oracle(), "RENAME foo TO bar");
    }

    @Test
    public void rename_table_on_postgresql() {
        RenameTableBuilderTest.verifySql(new PostgreSql(), "ALTER TABLE foo RENAME TO bar");
    }

    @Test
    public void throw_IAE_if_name_is_not_valid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Table name must be lower case and contain only alphanumeric chars or '_', got '(not valid)'");
        setName("(not valid)").build();
    }

    @Test
    public void throw_IAE_if_new_name_is_not_valid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Table name must be lower case and contain only alphanumeric chars or '_', got '(not valid)'");
        setName("foo").setNewName("(not valid)").build();
    }
}
