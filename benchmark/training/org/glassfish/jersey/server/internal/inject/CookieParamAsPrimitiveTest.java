/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.server.internal.inject;


import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class CookieParamAsPrimitiveTest extends AbstractTest {
    public CookieParamAsPrimitiveTest() {
        initiateWebApplication(CookieParamAsPrimitiveTest.ResourceHeaderPrimitives.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitivesDefaultNull.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitivesDefault.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitivesDefaultOverride.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveWrappers.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveWrappersDefaultNull.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveWrappersDefault.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveWrappersDefaultOverride.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveList.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveListDefaultNull.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveListDefault.class, CookieParamAsPrimitiveTest.ResourceHeaderPrimitiveListDefaultOverride.class);
    }

    @Path("/")
    public static class ResourceHeaderPrimitives {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        boolean v) {
            Assert.assertEquals(true, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        byte v) {
            Assert.assertEquals(127, v);
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        short v) {
            Assert.assertEquals(32767, v);
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        int v) {
            Assert.assertEquals(2147483647, v);
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        long v) {
            Assert.assertEquals(9223372036854775807L, v);
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        float v) {
            Assert.assertEquals(3.1415927F, v, 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        double v) {
            Assert.assertEquals(3.14159265358979, v, 0);
            return "content";
        }
    }

    @Path("/default/null")
    public static class ResourceHeaderPrimitivesDefaultNull {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        boolean v) {
            Assert.assertEquals(false, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        byte v) {
            Assert.assertEquals(0, v);
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        short v) {
            Assert.assertEquals(0, v);
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        int v) {
            Assert.assertEquals(0, v);
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        long v) {
            Assert.assertEquals(0L, v);
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        float v) {
            Assert.assertEquals(0.0F, v, 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        double v) {
            Assert.assertEquals(0.0, v, 0);
            return "content";
        }
    }

    @Path("/default")
    public static class ResourceHeaderPrimitivesDefault {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        @DefaultValue("true")
        boolean v) {
            Assert.assertEquals(true, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        @DefaultValue("127")
        byte v) {
            Assert.assertEquals(127, v);
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        @DefaultValue("32767")
        short v) {
            Assert.assertEquals(32767, v);
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        @DefaultValue("2147483647")
        int v) {
            Assert.assertEquals(2147483647, v);
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        @DefaultValue("9223372036854775807")
        long v) {
            Assert.assertEquals(9223372036854775807L, v);
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        @DefaultValue("3.14159265")
        float v) {
            Assert.assertEquals(3.1415927F, v, 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        @DefaultValue("3.14159265358979")
        double v) {
            Assert.assertEquals(3.14159265358979, v, 0);
            return "content";
        }
    }

    @Path("/default/override")
    public static class ResourceHeaderPrimitivesDefaultOverride {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        @DefaultValue("false")
        boolean v) {
            Assert.assertEquals(true, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        @DefaultValue("1")
        byte v) {
            Assert.assertEquals(127, v);
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        @DefaultValue("1")
        short v) {
            Assert.assertEquals(32767, v);
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        @DefaultValue("1")
        int v) {
            Assert.assertEquals(2147483647, v);
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        @DefaultValue("1")
        long v) {
            Assert.assertEquals(9223372036854775807L, v);
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        @DefaultValue("0.0")
        float v) {
            Assert.assertEquals(3.1415927F, v, 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        @DefaultValue("0.0")
        double v) {
            Assert.assertEquals(3.14159265358979, v, 0);
            return "content";
        }
    }

    @Path("/wrappers")
    public static class ResourceHeaderPrimitiveWrappers {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        Boolean v) {
            Assert.assertEquals(true, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        Byte v) {
            Assert.assertEquals(127, v.byteValue());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        Short v) {
            Assert.assertEquals(32767, v.shortValue());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        Integer v) {
            Assert.assertEquals(2147483647, v.intValue());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        Long v) {
            Assert.assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        Float v) {
            Assert.assertEquals(3.1415927F, v, 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        Double v) {
            Assert.assertEquals(3.14159265358979, v, 0);
            return "content";
        }
    }

    @Path("/wrappers/default/null")
    public static class ResourceHeaderPrimitiveWrappersDefaultNull {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        Boolean v) {
            Assert.assertEquals(null, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        Byte v) {
            Assert.assertEquals(null, v);
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        Short v) {
            Assert.assertEquals(null, v);
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        Integer v) {
            Assert.assertEquals(null, v);
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        Long v) {
            Assert.assertEquals(null, v);
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        Float v) {
            Assert.assertEquals(null, v);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        Double v) {
            Assert.assertEquals(null, v);
            return "content";
        }
    }

    @Path("/wrappers/default")
    public static class ResourceHeaderPrimitiveWrappersDefault {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        @DefaultValue("true")
        Boolean v) {
            Assert.assertEquals(true, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        @DefaultValue("127")
        Byte v) {
            Assert.assertEquals(127, v.byteValue());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        @DefaultValue("32767")
        Short v) {
            Assert.assertEquals(32767, v.shortValue());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        @DefaultValue("2147483647")
        Integer v) {
            Assert.assertEquals(2147483647, v.intValue());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        @DefaultValue("9223372036854775807")
        Long v) {
            Assert.assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        @DefaultValue("3.14159265")
        Float v) {
            Assert.assertEquals(3.1415927F, v, 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        @DefaultValue("3.14159265358979")
        Double v) {
            Assert.assertEquals(3.14159265358979, v, 0);
            return "content";
        }
    }

    @Path("/wrappers/default/override")
    public static class ResourceHeaderPrimitiveWrappersDefaultOverride {
        @GET
        @Produces("application/boolean")
        public String doGet(@CookieParam("boolean")
        @DefaultValue("false")
        Boolean v) {
            Assert.assertEquals(true, v);
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGet(@CookieParam("byte")
        @DefaultValue("1")
        Byte v) {
            Assert.assertEquals(127, v.byteValue());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGet(@CookieParam("short")
        @DefaultValue("1")
        Short v) {
            Assert.assertEquals(32767, v.shortValue());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGet(@CookieParam("int")
        @DefaultValue("1")
        Integer v) {
            Assert.assertEquals(2147483647, v.intValue());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGet(@CookieParam("long")
        @DefaultValue("1")
        Long v) {
            Assert.assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGet(@CookieParam("float")
        @DefaultValue("0.0")
        Float v) {
            Assert.assertEquals(3.1415927F, v, 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGet(@CookieParam("double")
        @DefaultValue("0.0")
        Double v) {
            Assert.assertEquals(3.14159265358979, v, 0);
            return "content";
        }
    }

    @Path("/list")
    public static class ResourceHeaderPrimitiveList {
        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@CookieParam("boolean")
        List<Boolean> v) {
            Assert.assertEquals(true, v.get(0));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@CookieParam("byte")
        List<Byte> v) {
            Assert.assertEquals(127, v.get(0).byteValue());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@CookieParam("short")
        List<Short> v) {
            Assert.assertEquals(32767, v.get(0).shortValue());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@CookieParam("int")
        List<Integer> v) {
            Assert.assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@CookieParam("long")
        List<Long> v) {
            Assert.assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@CookieParam("float")
        List<Float> v) {
            Assert.assertEquals(3.1415927F, v.get(0), 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@CookieParam("double")
        List<Double> v) {
            Assert.assertEquals(3.14159265358979, v.get(0), 0);
            return "content";
        }
    }

    @Path("/list/default/null")
    public static class ResourceHeaderPrimitiveListDefaultNull {
        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@CookieParam("boolean")
        List<Boolean> v) {
            Assert.assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@CookieParam("byte")
        List<Byte> v) {
            Assert.assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@CookieParam("short")
        List<Short> v) {
            Assert.assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@CookieParam("int")
        List<Integer> v) {
            Assert.assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@CookieParam("long")
        List<Long> v) {
            Assert.assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@CookieParam("float")
        List<Float> v) {
            Assert.assertEquals(0, v.size());
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@CookieParam("double")
        List<Double> v) {
            Assert.assertEquals(0, v.size());
            return "content";
        }
    }

    @Path("/list/default")
    public static class ResourceHeaderPrimitiveListDefault {
        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@CookieParam("boolean")
        @DefaultValue("true")
        List<Boolean> v) {
            Assert.assertEquals(true, v.get(0));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@CookieParam("byte")
        @DefaultValue("127")
        List<Byte> v) {
            Assert.assertEquals(127, v.get(0).byteValue());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@CookieParam("short")
        @DefaultValue("32767")
        List<Short> v) {
            Assert.assertEquals(32767, v.get(0).shortValue());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@CookieParam("int")
        @DefaultValue("2147483647")
        List<Integer> v) {
            Assert.assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@CookieParam("long")
        @DefaultValue("9223372036854775807")
        List<Long> v) {
            Assert.assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@CookieParam("float")
        @DefaultValue("3.14159265")
        List<Float> v) {
            Assert.assertEquals(3.1415927F, v.get(0), 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@CookieParam("double")
        @DefaultValue("3.14159265358979")
        List<Double> v) {
            Assert.assertEquals(3.14159265358979, v.get(0), 0);
            return "content";
        }
    }

    @Path("/list/default/override")
    public static class ResourceHeaderPrimitiveListDefaultOverride {
        @GET
        @Produces("application/boolean")
        public String doGetBoolean(@CookieParam("boolean")
        @DefaultValue("false")
        List<Boolean> v) {
            Assert.assertEquals(true, v.get(0));
            return "content";
        }

        @GET
        @Produces("application/byte")
        public String doGetByte(@CookieParam("byte")
        @DefaultValue("0")
        List<Byte> v) {
            Assert.assertEquals(127, v.get(0).byteValue());
            return "content";
        }

        @GET
        @Produces("application/short")
        public String doGetShort(@CookieParam("short")
        @DefaultValue("0")
        List<Short> v) {
            Assert.assertEquals(32767, v.get(0).shortValue());
            return "content";
        }

        @GET
        @Produces("application/int")
        public String doGetInteger(@CookieParam("int")
        @DefaultValue("0")
        List<Integer> v) {
            Assert.assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }

        @GET
        @Produces("application/long")
        public String doGetLong(@CookieParam("long")
        @DefaultValue("0")
        List<Long> v) {
            Assert.assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }

        @GET
        @Produces("application/float")
        public String doGetFloat(@CookieParam("float")
        @DefaultValue("0.0")
        List<Float> v) {
            Assert.assertEquals(3.1415927F, v.get(0), 0);
            return "content";
        }

        @GET
        @Produces("application/double")
        public String doGetDouble(@CookieParam("double")
        @DefaultValue("0.0")
        List<Double> v) {
            Assert.assertEquals(3.14159265358979, v.get(0), 0);
            return "content";
        }
    }

    @Test
    public void testGetBoolean() throws InterruptedException, ExecutionException {
        _test("boolean", "true");
    }

    @Test
    public void testGetBooleanPrimitivesDefault() throws InterruptedException, ExecutionException {
        _testDefault("boolean", "true");
    }

    @Test
    public void testGetBooleanPrimitiveWrapperDefault() throws InterruptedException, ExecutionException {
        _testWrappersDefault("boolean", "true");
    }

    @Test
    public void testGetBooleanPrimitiveListDefault() throws InterruptedException, ExecutionException {
        _testListDefault("boolean", "true");
    }

    @Test
    public void testGetByte() throws InterruptedException, ExecutionException {
        _test("byte", "127");
    }

    @Test
    public void testGetBytePrimitivesDefault() throws InterruptedException, ExecutionException {
        _testDefault("byte", "127");
    }

    @Test
    public void testGetBytePrimitiveWrappersDefault() throws InterruptedException, ExecutionException {
        _testWrappersDefault("byte", "127");
    }

    @Test
    public void testGetBytePrimitiveListDefault() throws InterruptedException, ExecutionException {
        _testListDefault("byte", "127");
    }

    @Test
    public void testGetShort() throws InterruptedException, ExecutionException {
        _test("short", "32767");
    }

    @Test
    public void testGetShortPrimtivesDefault() throws InterruptedException, ExecutionException {
        _testDefault("short", "32767");
    }

    @Test
    public void testGetShortPrimtiveWrappersDefault() throws InterruptedException, ExecutionException {
        _testWrappersDefault("short", "32767");
    }

    @Test
    public void testGetShortPrimtiveListDefault() throws InterruptedException, ExecutionException {
        _testListDefault("short", "32767");
    }

    @Test
    public void testGetInt() throws InterruptedException, ExecutionException {
        _test("int", "2147483647");
    }

    @Test
    public void testGetIntPrimitivesDefault() throws InterruptedException, ExecutionException {
        _testDefault("int", "2147483647");
    }

    @Test
    public void testGetIntPrimitiveWrappersDefault() throws InterruptedException, ExecutionException {
        _testWrappersDefault("int", "2147483647");
    }

    @Test
    public void testGetIntPrimitiveListDefault() throws InterruptedException, ExecutionException {
        _testListDefault("int", "2147483647");
    }

    @Test
    public void testGetLong() throws InterruptedException, ExecutionException {
        _test("long", "9223372036854775807");
    }

    @Test
    public void testGetLongPrimitivesDefault() throws InterruptedException, ExecutionException {
        _testDefault("long", "9223372036854775807");
    }

    @Test
    public void testGetLongPrimitiveWrappersDefault() throws InterruptedException, ExecutionException {
        _testWrappersDefault("long", "9223372036854775807");
    }

    @Test
    public void testGetLongPrimitiveListDefault() throws InterruptedException, ExecutionException {
        _testListDefault("long", "9223372036854775807");
    }

    @Test
    public void testGetFloat() throws InterruptedException, ExecutionException {
        _test("float", "3.14159265");
    }

    @Test
    public void testGetFloatPrimitivesDefault() throws InterruptedException, ExecutionException {
        _testDefault("float", "3.14159265");
    }

    @Test
    public void testGetFloatPrimitiveWrappersDefault() throws InterruptedException, ExecutionException {
        _testWrappersDefault("float", "3.14159265");
    }

    @Test
    public void testGetFloatPrimitiveListDefault() throws InterruptedException, ExecutionException {
        _testListDefault("float", "3.14159265");
    }

    @Test
    public void testGetDouble() throws InterruptedException, ExecutionException {
        _test("double", "3.14159265358979");
    }

    @Test
    public void testGetDoublePrimitivesDefault() throws InterruptedException, ExecutionException {
        _testDefault("double", "3.14159265358979");
    }

    @Test
    public void testGetDoublePrimitiveWrappersDefault() throws InterruptedException, ExecutionException {
        _testWrappersDefault("double", "3.14159265358979");
    }

    @Test
    public void testGetDoublePrimitiveListDefault() throws InterruptedException, ExecutionException {
        _testListDefault("double", "3.14159265358979");
    }

    @Test
    public void testBadPrimitiveValue() throws InterruptedException, ExecutionException {
        final ContainerResponse responseContext = apply(RequestContextBuilder.from("/", "GET").accept("application/int").cookie(new Cookie("int", "abcdef")).build());
        Assert.assertEquals(400, responseContext.getStatus());
    }

    @Test
    public void testBadPrimitiveWrapperValue() throws InterruptedException, ExecutionException {
        final ContainerResponse responseContext = apply(RequestContextBuilder.from("/wrappers", "GET").accept("application/int").cookie(new Cookie("int", "abcdef")).build());
        Assert.assertEquals(400, responseContext.getStatus());
    }

    @Test
    public void testBadPrimitiveListValue() throws InterruptedException, ExecutionException {
        final ContainerResponse responseContext = apply(RequestContextBuilder.from("/wrappers", "GET").accept("application/int").cookie(new Cookie("int", "abcdef")).build());
        Assert.assertEquals(400, responseContext.getStatus());
    }
}
