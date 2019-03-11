/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.tests.java.lang;


import junit.framework.TestCase;


public class StrictMathTest extends TestCase {
    private static final double HYP = StrictMath.sqrt(2.0);

    private static final double OPP = 1.0;

    private static final double ADJ = 1.0;

    /* Required to make previous preprocessor flags work - do not remove */
    int unused = 0;

    /**
     * java.lang.StrictMath#abs(double)
     */
    public void test_absD() {
        // Test for method double java.lang.StrictMath.abs(double)
        TestCase.assertTrue("Incorrect double abs value", ((StrictMath.abs((-1908.8976))) == 1908.8976));
        TestCase.assertTrue("Incorrect double abs value", ((StrictMath.abs(1908.8976)) == 1908.8976));
    }

    /**
     * java.lang.StrictMath#abs(float)
     */
    public void test_absF() {
        // Test for method float java.lang.StrictMath.abs(float)
        TestCase.assertTrue("Incorrect float abs value", ((StrictMath.abs((-1908.8976F))) == 1908.8976F));
        TestCase.assertTrue("Incorrect float abs value", ((StrictMath.abs(1908.8976F)) == 1908.8976F));
    }

    /**
     * java.lang.StrictMath#abs(int)
     */
    public void test_absI() {
        // Test for method int java.lang.StrictMath.abs(int)
        TestCase.assertTrue("Incorrect int abs value", ((StrictMath.abs((-1908897))) == 1908897));
        TestCase.assertTrue("Incorrect int abs value", ((StrictMath.abs(1908897)) == 1908897));
    }

    /**
     * java.lang.StrictMath#abs(long)
     */
    public void test_absJ() {
        // Test for method long java.lang.StrictMath.abs(long)
        TestCase.assertTrue("Incorrect long abs value", ((StrictMath.abs((-19088976000089L))) == 19088976000089L));
        TestCase.assertTrue("Incorrect long abs value", ((StrictMath.abs(19088976000089L)) == 19088976000089L));
    }

    /**
     * java.lang.StrictMath#acos(double)
     */
    public void test_acosD() {
        // Test for method double java.lang.StrictMath.acos(double)
        TestCase.assertTrue("Returned incorrect arc cosine", ((StrictMath.cos(StrictMath.acos(((StrictMathTest.ADJ) / (StrictMathTest.HYP))))) == ((StrictMathTest.ADJ) / (StrictMathTest.HYP))));
    }

    /**
     * java.lang.StrictMath#asin(double)
     */
    public void test_asinD() {
        // Test for method double java.lang.StrictMath.asin(double)
        TestCase.assertTrue("Returned incorrect arc sine", ((StrictMath.sin(StrictMath.asin(((StrictMathTest.OPP) / (StrictMathTest.HYP))))) == ((StrictMathTest.OPP) / (StrictMathTest.HYP))));
    }

    /**
     * java.lang.StrictMath#atan(double)
     */
    public void test_atanD() {
        // Test for method double java.lang.StrictMath.atan(double)
        double answer = StrictMath.tan(StrictMath.atan(1.0));
        TestCase.assertTrue(("Returned incorrect arc tangent: " + answer), ((answer <= 1.0) && (answer >= 0.9999999999999998)));
    }

    /**
     * java.lang.StrictMath#atan2(double, double)
     */
    public void test_atan2DD() {
        // Test for method double java.lang.StrictMath.atan2(double, double)
        double answer = StrictMath.atan(StrictMath.tan(1.0));
        TestCase.assertTrue(("Returned incorrect arc tangent: " + answer), ((answer <= 1.0) && (answer >= 0.9999999999999998)));
    }

    /**
     * java.lang.StrictMath#ceil(double)
     */
    public void test_ceilD() {
        // Test for method double java.lang.StrictMath.ceil(double)
        TestCase.assertEquals("Incorrect ceiling for double", 79, StrictMath.ceil(78.89), 0.0);
        TestCase.assertEquals("Incorrect ceiling for double", (-78), StrictMath.ceil((-78.89)), 0.0);
    }

    /**
     * java.lang.StrictMath#cos(double)
     */
    public void test_cosD() {
        // Test for method double java.lang.StrictMath.cos(double)
        TestCase.assertTrue("Returned incorrect cosine", ((StrictMath.cos(StrictMath.acos(((StrictMathTest.ADJ) / (StrictMathTest.HYP))))) == ((StrictMathTest.ADJ) / (StrictMathTest.HYP))));
    }

    /**
     * java.lang.StrictMath#exp(double)
     */
    public void test_expD() {
        // Test for method double java.lang.StrictMath.exp(double)
        TestCase.assertTrue("Incorrect answer returned for simple power", ((StrictMath.abs(((StrictMath.exp(4.0)) - ((((StrictMath.E) * (StrictMath.E)) * (StrictMath.E)) * (StrictMath.E))))) < 0.1));
        TestCase.assertTrue("Incorrect answer returned for larger power", ((StrictMath.log(((StrictMath.abs(StrictMath.exp(5.5))) - 5.5))) < 10.0));
    }

    /**
     * java.lang.StrictMath#floor(double)
     */
    public void test_floorD() {
        // Test for method double java.lang.StrictMath.floor(double)
        TestCase.assertEquals("Incorrect floor for double", 78, StrictMath.floor(78.89), 0.0);
        TestCase.assertEquals("Incorrect floor for double", (-79), StrictMath.floor((-78.89)), 0.0);
    }

    /**
     * java.lang.StrictMath#IEEEremainder(double, double)
     */
    public void test_IEEEremainderDD() {
        // Test for method double java.lang.StrictMath.IEEEremainder(double,
        // double)
        TestCase.assertEquals("Incorrect remainder returned", 0.0, StrictMath.IEEEremainder(1.0, 1.0), 0.0);
        TestCase.assertTrue("Incorrect remainder returned", (((StrictMath.IEEEremainder(1.32, 89.765)) >= 0.014705063220631647) || ((StrictMath.IEEEremainder(1.32, 89.765)) >= 0.01470506322063165)));
    }

    /**
     * java.lang.StrictMath#log(double)
     */
    public void test_logD() {
        // Test for method double java.lang.StrictMath.log(double)
        for (double d = 10; d >= (-10); d -= 0.5) {
            double answer = StrictMath.log(StrictMath.exp(d));
            TestCase.assertTrue(((("Answer does not equal expected answer for d = " + d) + " answer = ") + answer), ((StrictMath.abs((answer - d))) <= (StrictMath.abs((d * 1.0E-8)))));
        }
    }

    /**
     * java.lang.StrictMath#max(double, double)
     */
    public void test_maxDD() {
        // Test for method double java.lang.StrictMath.max(double, double)
        TestCase.assertEquals("Incorrect double max value", 1908897.6000089, StrictMath.max((-1908897.6000089), 1908897.6000089), 0.0);
        TestCase.assertEquals("Incorrect double max value", 1908897.6000089, StrictMath.max(2.0, 1908897.6000089), 0.0);
        TestCase.assertEquals("Incorrect double max value", (-2.0), StrictMath.max((-2.0), (-1908897.6000089)), 0.0);
    }

    /**
     * java.lang.StrictMath#max(float, float)
     */
    public void test_maxFF() {
        // Test for method float java.lang.StrictMath.max(float, float)
        TestCase.assertTrue("Incorrect float max value", ((StrictMath.max((-1908897.6F), 1908897.6F)) == 1908897.6F));
        TestCase.assertTrue("Incorrect float max value", ((StrictMath.max(2.0F, 1908897.6F)) == 1908897.6F));
        TestCase.assertTrue("Incorrect float max value", ((StrictMath.max((-2.0F), (-1908897.6F))) == (-2.0F)));
    }

    /**
     * java.lang.StrictMath#max(int, int)
     */
    public void test_maxII() {
        // Test for method int java.lang.StrictMath.max(int, int)
        TestCase.assertEquals("Incorrect int max value", 19088976, StrictMath.max((-19088976), 19088976));
        TestCase.assertEquals("Incorrect int max value", 19088976, StrictMath.max(20, 19088976));
        TestCase.assertEquals("Incorrect int max value", (-20), StrictMath.max((-20), (-19088976)));
    }

    /**
     * java.lang.StrictMath#max(long, long)
     */
    public void test_maxJJ() {
        // Test for method long java.lang.StrictMath.max(long, long)
        TestCase.assertEquals("Incorrect long max value", 19088976000089L, StrictMath.max((-19088976000089L), 19088976000089L));
        TestCase.assertEquals("Incorrect long max value", 19088976000089L, StrictMath.max(20, 19088976000089L));
        TestCase.assertEquals("Incorrect long max value", (-20), StrictMath.max((-20), (-19088976000089L)));
    }

    /**
     * java.lang.StrictMath#min(double, double)
     */
    public void test_minDD() {
        // Test for method double java.lang.StrictMath.min(double, double)
        TestCase.assertEquals("Incorrect double min value", (-1908897.6000089), StrictMath.min((-1908897.6000089), 1908897.6000089), 0.0);
        TestCase.assertEquals("Incorrect double min value", 2.0, StrictMath.min(2.0, 1908897.6000089), 0.0);
        TestCase.assertEquals("Incorrect double min value", (-1908897.6000089), StrictMath.min((-2.0), (-1908897.6000089)), 0.0);
    }

    /**
     * java.lang.StrictMath#min(float, float)
     */
    public void test_minFF() {
        // Test for method float java.lang.StrictMath.min(float, float)
        TestCase.assertTrue("Incorrect float min value", ((StrictMath.min((-1908897.6F), 1908897.6F)) == (-1908897.6F)));
        TestCase.assertTrue("Incorrect float min value", ((StrictMath.min(2.0F, 1908897.6F)) == 2.0F));
        TestCase.assertTrue("Incorrect float min value", ((StrictMath.min((-2.0F), (-1908897.6F))) == (-1908897.6F)));
    }

    /**
     * java.lang.StrictMath#min(int, int)
     */
    public void test_minII() {
        // Test for method int java.lang.StrictMath.min(int, int)
        TestCase.assertEquals("Incorrect int min value", (-19088976), StrictMath.min((-19088976), 19088976));
        TestCase.assertEquals("Incorrect int min value", 20, StrictMath.min(20, 19088976));
        TestCase.assertEquals("Incorrect int min value", (-19088976), StrictMath.min((-20), (-19088976)));
    }

    /**
     * java.lang.StrictMath#min(long, long)
     */
    public void test_minJJ() {
        // Test for method long java.lang.StrictMath.min(long, long)
        TestCase.assertEquals("Incorrect long min value", (-19088976000089L), StrictMath.min((-19088976000089L), 19088976000089L));
        TestCase.assertEquals("Incorrect long min value", 20, StrictMath.min(20, 19088976000089L));
        TestCase.assertEquals("Incorrect long min value", (-19088976000089L), StrictMath.min((-20), (-19088976000089L)));
    }

    /**
     * java.lang.StrictMath#pow(double, double)
     */
    public void test_powDD() {
        // Test for method double java.lang.StrictMath.pow(double, double)
        TestCase.assertTrue("pow returned incorrect value", (((long) (StrictMath.pow(2, 8))) == 256L));
        TestCase.assertTrue("pow returned incorrect value", ((StrictMath.pow(2, (-8))) == 0.00390625));
    }

    /**
     * java.lang.StrictMath#rint(double)
     */
    public void test_rintD() {
        // Test for method double java.lang.StrictMath.rint(double)
        TestCase.assertEquals("Failed to round properly - up to odd", 3.0, StrictMath.rint(2.9), 0.0);
        TestCase.assertTrue("Failed to round properly - NaN", Double.isNaN(StrictMath.rint(Double.NaN)));
        TestCase.assertEquals("Failed to round properly down  to even", 2.0, StrictMath.rint(2.1), 0.0);
        TestCase.assertTrue((("Failed to round properly " + 2.5) + " to even"), ((StrictMath.rint(2.5)) == 2.0));
    }

    /**
     * java.lang.StrictMath#round(double)
     */
    public void test_roundD() {
        // Test for method long java.lang.StrictMath.round(double)
        TestCase.assertEquals("Incorrect rounding of a float", (-91), StrictMath.round((-90.89)));
    }

    /**
     * java.lang.StrictMath#round(float)
     */
    public void test_roundF() {
        // Test for method int java.lang.StrictMath.round(float)
        TestCase.assertEquals("Incorrect rounding of a float", (-91), StrictMath.round((-90.89F)));
    }

    /**
     * java.lang.StrictMath#signum(double)
     */
    public void test_signum_D() {
        TestCase.assertTrue(Double.isNaN(StrictMath.signum(Double.NaN)));
        TestCase.assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(StrictMath.signum(0.0)));
        TestCase.assertEquals(Double.doubleToLongBits((+0.0)), Double.doubleToLongBits(StrictMath.signum((+0.0))));
        TestCase.assertEquals(Double.doubleToLongBits((-0.0)), Double.doubleToLongBits(StrictMath.signum((-0.0))));
        TestCase.assertEquals(1.0, StrictMath.signum(253681.2187962), 0.0);
        TestCase.assertEquals((-1.0), StrictMath.signum((-1.2587469356E8)), 0.0);
        if (!(System.getProperty("os.arch").equals("armv7"))) {
            TestCase.assertEquals(1.0, StrictMath.signum(1.2587E-308), 0.0);
            TestCase.assertEquals((-1.0), StrictMath.signum((-1.2587E-308)), 0.0);
        }
        TestCase.assertEquals(1.0, StrictMath.signum(Double.MAX_VALUE), 0.0);
        if (!(System.getProperty("os.arch").equals("armv7"))) {
            TestCase.assertEquals(1.0, StrictMath.signum(Double.MIN_VALUE), 0.0);
        }
        TestCase.assertEquals((-1.0), StrictMath.signum((-(Double.MAX_VALUE))), 0.0);
        if (!(System.getProperty("os.arch").equals("armv7"))) {
            TestCase.assertEquals((-1.0), StrictMath.signum((-(Double.MIN_VALUE))), 0.0);
        }
        TestCase.assertEquals(1.0, StrictMath.signum(Double.POSITIVE_INFINITY), 0.0);
        TestCase.assertEquals((-1.0), StrictMath.signum(Double.NEGATIVE_INFINITY), 0.0);
    }

    /**
     * java.lang.StrictMath#signum(float)
     */
    public void test_signum_F() {
        TestCase.assertTrue(Float.isNaN(StrictMath.signum(Float.NaN)));
        TestCase.assertEquals(Float.floatToIntBits(0.0F), Float.floatToIntBits(StrictMath.signum(0.0F)));
        TestCase.assertEquals(Float.floatToIntBits((+0.0F)), Float.floatToIntBits(StrictMath.signum((+0.0F))));
        TestCase.assertEquals(Float.floatToIntBits((-0.0F)), Float.floatToIntBits(StrictMath.signum((-0.0F))));
        TestCase.assertEquals(1.0F, StrictMath.signum(253681.22F), 0.0F);
        TestCase.assertEquals((-1.0F), StrictMath.signum((-1.25874696E8F)), 0.0F);
        TestCase.assertEquals(1.0F, StrictMath.signum(1.2587E-11F), 0.0F);
        TestCase.assertEquals((-1.0F), StrictMath.signum((-1.2587E-11F)), 0.0F);
        TestCase.assertEquals(1.0F, StrictMath.signum(Float.MAX_VALUE), 0.0F);
        if (!(System.getProperty("os.arch").equals("armv7"))) {
            TestCase.assertEquals(1.0F, StrictMath.signum(Float.MIN_VALUE), 0.0F);
        }
        TestCase.assertEquals((-1.0F), StrictMath.signum((-(Float.MAX_VALUE))), 0.0F);
        if (!(System.getProperty("os.arch").equals("armv7"))) {
            TestCase.assertEquals((-1.0F), StrictMath.signum((-(Float.MIN_VALUE))), 0.0F);
        }
        TestCase.assertEquals(1.0F, StrictMath.signum(Float.POSITIVE_INFINITY), 0.0F);
        TestCase.assertEquals((-1.0F), StrictMath.signum(Float.NEGATIVE_INFINITY), 0.0F);
    }

    /**
     * java.lang.StrictMath#sin(double)
     */
    public void test_sinD() {
        // Test for method double java.lang.StrictMath.sin(double)
        TestCase.assertTrue("Returned incorrect sine", ((StrictMath.sin(StrictMath.asin(((StrictMathTest.OPP) / (StrictMathTest.HYP))))) == ((StrictMathTest.OPP) / (StrictMathTest.HYP))));
    }

    /**
     * java.lang.StrictMath#sinh(double)
     */
    // TODO(tball): reenable after OpenJDK migration completes.
    // public void test_sinh_D() {
    // // Test for special situations
    // assertTrue(Double.isNaN(StrictMath.sinh(Double.NaN)));
    // assertEquals("Should return POSITIVE_INFINITY",
    // Double.POSITIVE_INFINITY, StrictMath
    // .sinh(Double.POSITIVE_INFINITY), 0D);
    // assertEquals("Should return NEGATIVE_INFINITY",
    // Double.NEGATIVE_INFINITY, StrictMath
    // .sinh(Double.NEGATIVE_INFINITY), 0D);
    // assertEquals(Double.doubleToLongBits(0.0), Double
    // .doubleToLongBits(StrictMath.sinh(0.0)));
    // assertEquals(Double.doubleToLongBits(+0.0), Double
    // .doubleToLongBits(StrictMath.sinh(+0.0)));
    // assertEquals(Double.doubleToLongBits(-0.0), Double
    // .doubleToLongBits(StrictMath.sinh(-0.0)));
    // 
    // assertEquals("Should return POSITIVE_INFINITY",
    // Double.POSITIVE_INFINITY, StrictMath.sinh(1234.56), 0D);
    // assertEquals("Should return NEGATIVE_INFINITY",
    // Double.NEGATIVE_INFINITY, StrictMath.sinh(-1234.56), 0D);
    // assertEquals("Should return 1.0000000000001666E-6",
    // 1.0000000000001666E-6, StrictMath.sinh(0.000001), 0D);
    // assertEquals("Should return -1.0000000000001666E-6",
    // -1.0000000000001666E-6, StrictMath.sinh(-0.000001), 0D);
    // assertEquals("Should return 5.115386441963859", 5.115386441963859,
    // StrictMath.sinh(2.33482), 0D);
    // assertEquals("Should return POSITIVE_INFINITY",
    // Double.POSITIVE_INFINITY, StrictMath.sinh(Double.MAX_VALUE), 0D);
    // assertEquals("Should return 4.9E-324", 4.9E-324, StrictMath
    // .sinh(Double.MIN_VALUE), 0D);
    // }
    /**
     * java.lang.StrictMath#sqrt(double)
     */
    public void test_sqrtD() {
        // Test for method double java.lang.StrictMath.sqrt(double)
        TestCase.assertEquals("Incorrect root returned1", 2, StrictMath.sqrt(StrictMath.pow(StrictMath.sqrt(2), 4)), 0.0);
        TestCase.assertEquals("Incorrect root returned2", 7, StrictMath.sqrt(49), 0.0);
    }

    /**
     * java.lang.StrictMath#tan(double)
     */
    public void test_tanD() {
        // Test for method double java.lang.StrictMath.tan(double)
        TestCase.assertTrue("Returned incorrect tangent: ", (((StrictMath.tan(StrictMath.atan(1.0))) <= 1.0) || ((StrictMath.tan(StrictMath.atan(1.0))) >= 0.9999999999999998)));
    }

    /**
     * java.lang.StrictMath#tanh(double)
     */
    public void test_tanh_D() {
        // Test for special situations
        TestCase.assertTrue(Double.isNaN(StrictMath.tanh(Double.NaN)));
        TestCase.assertEquals("Should return +1.0", (+1.0), StrictMath.tanh(Double.POSITIVE_INFINITY), 0.0);
        TestCase.assertEquals("Should return -1.0", (-1.0), StrictMath.tanh(Double.NEGATIVE_INFINITY), 0.0);
        TestCase.assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(StrictMath.tanh(0.0)));
        TestCase.assertEquals(Double.doubleToLongBits((+0.0)), Double.doubleToLongBits(StrictMath.tanh((+0.0))));
        TestCase.assertEquals(Double.doubleToLongBits((-0.0)), Double.doubleToLongBits(StrictMath.tanh((-0.0))));
        TestCase.assertEquals("Should return 1.0", 1.0, StrictMath.tanh(1234.56), 0.0);
        TestCase.assertEquals("Should return -1.0", (-1.0), StrictMath.tanh((-1234.56)), 0.0);
        TestCase.assertEquals("Should return 9.999999999996666E-7", 9.999999999996666E-7, StrictMath.tanh(1.0E-6), 0.0);
        TestCase.assertEquals("Should return 0.981422884124941", 0.981422884124941, StrictMath.tanh(2.33482), 0.0);
        TestCase.assertEquals("Should return 1.0", 1.0, StrictMath.tanh(Double.MAX_VALUE), 0.0);
        TestCase.assertEquals("Should return 4.9E-324", 4.9E-324, StrictMath.tanh(Double.MIN_VALUE), 0.0);
    }

    /**
     * java.lang.StrictMath#random()
     */
    public void test_random() {
        // There isn't a place for these tests so just stick them here
        TestCase.assertEquals("Wrong value E", 4613303445314885481L, Double.doubleToLongBits(StrictMath.E));
        TestCase.assertEquals("Wrong value PI", 4614256656552045848L, Double.doubleToLongBits(StrictMath.PI));
        for (int i = 500; i >= 0; i--) {
            double d = StrictMath.random();
            TestCase.assertTrue(("Generated number is out of range: " + d), ((d >= 0.0) && (d < 1.0)));
        }
    }

    /**
     * java.lang.StrictMath#toRadians(double)
     */
    public void test_toRadiansD() {
        for (double d = 500; d >= 0; d -= 1.0) {
            double converted = StrictMath.toDegrees(StrictMath.toRadians(d));
            TestCase.assertTrue(("Converted number not equal to original. d = " + d), ((converted >= (d * 0.99999999)) && (converted <= (d * 1.00000001))));
        }
    }

    /**
     * java.lang.StrictMath#toDegrees(double)
     */
    public void test_toDegreesD() {
        for (double d = 500; d >= 0; d -= 1.0) {
            double converted = StrictMath.toRadians(StrictMath.toDegrees(d));
            TestCase.assertTrue(("Converted number not equal to original. d = " + d), ((converted >= (d * 0.99999999)) && (converted <= (d * 1.00000001))));
        }
    }
}
