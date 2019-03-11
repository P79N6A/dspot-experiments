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
package io.crate.expression.scalar.geo;


import DataTypes.GEO_SHAPE;
import DataTypes.STRING;
import io.crate.exceptions.ConversionException;
import io.crate.expression.scalar.AbstractScalarFunctionsTest;
import io.crate.expression.symbol.Literal;
import io.crate.geo.GeoJSONUtils;
import io.crate.testing.SymbolMatchers;
import io.crate.testing.TestingHelpers;
import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.Test;


public class IntersectsFunctionTest extends AbstractScalarFunctionsTest {
    @Test
    public void testNormalizeFromStringLiterals() throws Exception {
        assertNormalize("intersects('LINESTRING (0 0, 10 10)', 'LINESTRING (0 2, 0 -2)')", SymbolMatchers.isLiteral(Boolean.TRUE));
    }

    @Test
    public void testNormalizeFromGeoShapeLiterals() throws Exception {
        assertNormalize("intersects('POLYGON ((0 0, 10 10, 10 0, 0 0), (5 1, 7 1, 7 2, 5 2, 5 1))', 'LINESTRING (0 2, 0 -2)')", SymbolMatchers.isLiteral(Boolean.TRUE));
    }

    @Test
    public void testNormalizeFromMixedLiterals() throws Exception {
        assertNormalize("intersects({type='LineString', coordinates=[[0, 0], [10, 10]]}, 'LINESTRING (0 2, 0 -2)')", SymbolMatchers.isLiteral(Boolean.TRUE));
    }

    @Test
    public void testNormalizeFromInvalidLiteral() throws Exception {
        expectedException.expect(ConversionException.class);
        expectedException.expectMessage(Matchers.stringContainsInOrder(Arrays.asList("Cannot cast", "to type geo_shape")));
        assertNormalize("intersects({type='LineString', coordinates=[0, 0]}, 'LINESTRING (0 2, 0 -2)')", null);
    }

    @Test
    public void testEvaluateFromString() throws Exception {
        assertEvaluate("intersects(geostring, geostring)", true, Literal.of(STRING, "POINT (0 0)"), Literal.of(STRING, "POLYGON ((1 1, 1 -1, -1 -1, -1 1, 1 1))"));
    }

    @Test
    public void testEvaluateMixed() throws Exception {
        assertEvaluate("intersects(geostring, geoshape)", false, Literal.of(STRING, "POINT (100 0)"), Literal.of(GEO_SHAPE, GeoJSONUtils.wkt2Map("POLYGON ((1 1, 1 -1, -1 -1, -1 1, 1 1))")));
    }

    @Test
    public void testEvaluateLowercaseGeoJSON() throws Exception {
        assertEvaluate("intersects(geostring, geoshape)", false, Literal.of(STRING, "POINT (100 0)"), Literal.of(GEO_SHAPE, TestingHelpers.jsonMap("{\"type\":\"linestring\", \"coordinates\":[[0, 0], [10, 10]]}")));
    }

    @Test
    public void testExactIntersect() throws Exception {
        // validate how exact the intersection detection is
        assertEvaluate("intersects(geostring, geoshape)", true, Literal.of(STRING, "POINT (100.00000000000001 0.0)"), Literal.of(GEO_SHAPE, TestingHelpers.jsonMap("{\"type\":\"linestring\", \"coordinates\":[[100.00000000000001, 0.0], [10, 10]]}")));
        assertEvaluate("intersects(geostring, geoshape)", false, Literal.of(STRING, "POINT (100.00000000000001 0.0)"), Literal.of(GEO_SHAPE, TestingHelpers.jsonMap("{\"type\":\"linestring\", \"coordinates\":[[100.00000000000003, 0.0], [10, 10]]}")));
    }
}
