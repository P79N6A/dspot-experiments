/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.query;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import org.apache.druid.query.aggregation.LongSumAggregatorFactory;
import org.apache.druid.query.dimension.DefaultDimensionSpec;
import org.apache.druid.query.groupby.GroupByQuery;
import org.apache.druid.segment.TestHelper;
import org.junit.Assert;
import org.junit.Test;


public class DataSourceTest {
    private static final ObjectMapper jsonMapper = TestHelper.makeJsonMapper();

    @Test
    public void testSerialization() throws IOException {
        DataSource dataSource = new TableDataSource("somedatasource");
        String json = DataSourceTest.jsonMapper.writeValueAsString(dataSource);
        DataSource serdeDataSource = DataSourceTest.jsonMapper.readValue(json, DataSource.class);
        Assert.assertEquals(dataSource, serdeDataSource);
    }

    @Test
    public void testLegacyDataSource() throws IOException {
        DataSource dataSource = DataSourceTest.jsonMapper.readValue("\"somedatasource\"", DataSource.class);
        Assert.assertEquals(new TableDataSource("somedatasource"), dataSource);
    }

    @Test
    public void testTableDataSource() throws IOException {
        DataSource dataSource = DataSourceTest.jsonMapper.readValue("{\"type\":\"table\", \"name\":\"somedatasource\"}", DataSource.class);
        Assert.assertEquals(new TableDataSource("somedatasource"), dataSource);
    }

    @Test
    public void testQueryDataSource() throws IOException {
        GroupByQuery query = GroupByQuery.builder().setDataSource(QueryRunnerTestHelper.dataSource).setQuerySegmentSpec(QueryRunnerTestHelper.firstToThird).setDimensions(new DefaultDimensionSpec("quality", "alias")).setAggregatorSpecs(QueryRunnerTestHelper.rowsCount, new LongSumAggregatorFactory("idx", "index")).setGranularity(QueryRunnerTestHelper.dayGran).build();
        String dataSourceJSON = ("{\"type\":\"query\", \"query\":" + (DataSourceTest.jsonMapper.writeValueAsString(query))) + "}";
        DataSource dataSource = DataSourceTest.jsonMapper.readValue(dataSourceJSON, DataSource.class);
        Assert.assertEquals(new QueryDataSource(query), dataSource);
    }

    @Test
    public void testUnionDataSource() throws Exception {
        DataSource dataSource = DataSourceTest.jsonMapper.readValue("{\"type\":\"union\", \"dataSources\":[\"ds1\", \"ds2\"]}", DataSource.class);
        Assert.assertTrue((dataSource instanceof UnionDataSource));
        Assert.assertEquals(Lists.newArrayList(new TableDataSource("ds1"), new TableDataSource("ds2")), Lists.newArrayList(getDataSources()));
        Assert.assertEquals(Lists.newArrayList("ds1", "ds2"), Lists.newArrayList(dataSource.getNames()));
        final DataSource serde = DataSourceTest.jsonMapper.readValue(DataSourceTest.jsonMapper.writeValueAsString(dataSource), DataSource.class);
        Assert.assertEquals(dataSource, serde);
    }
}
