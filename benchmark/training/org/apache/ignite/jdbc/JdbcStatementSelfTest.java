/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.jdbc;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;


/**
 * Statement test.
 */
public class JdbcStatementSelfTest extends GridCommonAbstractTest {
    /**
     * URL.
     */
    private static final String URL = "jdbc:ignite://127.0.0.1/";

    /**
     * SQL query.
     */
    private static final String SQL = "select * from Person where age > 30";

    /**
     * Connection.
     */
    private Connection conn;

    /**
     * Statement.
     */
    private Statement stmt;

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testExecuteQuery() throws Exception {
        ResultSet rs = stmt.executeQuery(JdbcStatementSelfTest.SQL);
        assert rs != null;
        int cnt = 0;
        while (rs.next()) {
            int id = rs.getInt("id");
            if (id == 2) {
                assert "Joe".equals(rs.getString("firstName"));
                assert "Black".equals(rs.getString("lastName"));
                assert (rs.getInt("age")) == 35;
            } else
                if (id == 3) {
                    assert "Mike".equals(rs.getString("firstName"));
                    assert "Green".equals(rs.getString("lastName"));
                    assert (rs.getInt("age")) == 40;
                } else
                    assert false : "Wrong ID: " + id;


            cnt++;
        } 
        assert cnt == 2;
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testExecute() throws Exception {
        assert stmt.execute(JdbcStatementSelfTest.SQL);
        ResultSet rs = stmt.getResultSet();
        assert rs != null;
        assert (stmt.getResultSet()) == null;
        int cnt = 0;
        while (rs.next()) {
            int id = rs.getInt("id");
            if (id == 2) {
                assert "Joe".equals(rs.getString("firstName"));
                assert "Black".equals(rs.getString("lastName"));
                assert (rs.getInt("age")) == 35;
            } else
                if (id == 3) {
                    assert "Mike".equals(rs.getString("firstName"));
                    assert "Green".equals(rs.getString("lastName"));
                    assert (rs.getInt("age")) == 40;
                } else
                    assert false : "Wrong ID: " + id;


            cnt++;
        } 
        assert cnt == 2;
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testMaxRows() throws Exception {
        stmt.setMaxRows(1);
        ResultSet rs = stmt.executeQuery(JdbcStatementSelfTest.SQL);
        assert rs != null;
        int cnt = 0;
        while (rs.next()) {
            int id = rs.getInt("id");
            if (id == 2) {
                assert "Joe".equals(rs.getString("firstName"));
                assert "Black".equals(rs.getString("lastName"));
                assert (rs.getInt("age")) == 35;
            } else
                if (id == 3) {
                    assert "Mike".equals(rs.getString("firstName"));
                    assert "Green".equals(rs.getString("lastName"));
                    assert (rs.getInt("age")) == 40;
                } else
                    assert false : "Wrong ID: " + id;


            cnt++;
        } 
        assert cnt == 1;
        stmt.setMaxRows(0);
        rs = stmt.executeQuery(JdbcStatementSelfTest.SQL);
        assert rs != null;
        cnt = 0;
        while (rs.next()) {
            int id = rs.getInt("id");
            if (id == 2) {
                assert "Joe".equals(rs.getString("firstName"));
                assert "Black".equals(rs.getString("lastName"));
                assert (rs.getInt("age")) == 35;
            } else
                if (id == 3) {
                    assert "Mike".equals(rs.getString("firstName"));
                    assert "Green".equals(rs.getString("lastName"));
                    assert (rs.getInt("age")) == 40;
                } else
                    assert false : "Wrong ID: " + id;


            cnt++;
        } 
        assert cnt == 2;
    }

    /**
     * Person.
     */
    private static class Person implements Serializable {
        /**
         * ID.
         */
        @QuerySqlField
        private final int id;

        /**
         * First name.
         */
        @QuerySqlField(index = false)
        private final String firstName;

        /**
         * Last name.
         */
        @QuerySqlField(index = false)
        private final String lastName;

        /**
         * Age.
         */
        @QuerySqlField
        private final int age;

        /**
         *
         *
         * @param id
         * 		ID.
         * @param firstName
         * 		First name.
         * @param lastName
         * 		Last name.
         * @param age
         * 		Age.
         */
        private Person(int id, String firstName, String lastName, int age) {
            assert !(F.isEmpty(firstName));
            assert !(F.isEmpty(lastName));
            assert age > 0;
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }
    }
}
