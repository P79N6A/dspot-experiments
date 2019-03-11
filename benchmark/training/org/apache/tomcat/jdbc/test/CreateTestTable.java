/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.jdbc.test;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;
import org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer;
import org.junit.Test;


public class CreateTestTable extends DefaultTestCase {
    public static volatile boolean recreate = Boolean.getBoolean("recreate");

    @Test
    public void testCreateTestTable() throws Exception {
        Connection con = datasource.getConnection();
        Statement st = con.createStatement();
        try {
            st.execute("create table test(id int not null, val1 varchar(255), val2 varchar(255), val3 varchar(255), val4 varchar(255))");
        } catch (Exception ignore) {
            // Ignore
        }
        st.close();
        con.close();
    }

    @Test
    public void testPopulateData() throws Exception {
        int count = 100000;
        int actual = testCheckData();
        if (actual >= count) {
            System.out.println((("Test tables has " + actual) + " rows of data. No need to populate."));
            return;
        }
        datasource.setJdbcInterceptors(ResetAbandonedTimer.class.getName());
        String insert = "insert into test values (?,?,?,?,?)";
        this.datasource.setRemoveAbandoned(false);
        Connection con = datasource.getConnection();
        boolean commit = con.getAutoCommit();
        con.setAutoCommit(false);
        if (CreateTestTable.recreate) {
            Statement st = con.createStatement();
            try {
                st.execute("drop table test");
            } catch (Exception ignore) {
                // Ignore
            }
            st.execute("create table test(id int not null, val1 varchar(255), val2 varchar(255), val3 varchar(255), val4 varchar(255))");
            st.close();
        }
        PreparedStatement ps = con.prepareStatement(insert);
        ps.setQueryTimeout(0);
        for (int i = actual; i < count; i++) {
            ps.setInt(1, i);
            String s = CreateTestTable.getRandom();
            ps.setString(2, s);
            ps.setString(3, s);
            ps.setString(4, s);
            ps.setString(5, s);
            ps.addBatch();
            ps.clearParameters();
            if (((i + 1) % 1000) == 0) {
                System.out.print(".");
            }
            if (((i + 1) % 10000) == 0) {
                System.out.print(("\n" + (i + 1)));
                ps.executeBatch();
                ps.close();
                con.commit();
                ps = con.prepareStatement(insert);
                ps.setQueryTimeout(0);
            }
        }
        ps.close();
        con.setAutoCommit(commit);
        con.close();
    }

    public static final Random random = new Random(System.currentTimeMillis());
}
