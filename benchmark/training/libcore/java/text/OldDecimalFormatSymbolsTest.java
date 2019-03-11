/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.java.text;


import java.io.ObjectInputStream;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import junit.framework.TestCase;


public class OldDecimalFormatSymbolsTest extends TestCase {
    DecimalFormatSymbols dfs;

    public void test_RIHarmony_compatible() throws Exception {
        ObjectInputStream i = null;
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
            i = new ObjectInputStream(getClass().getClassLoader().getResourceAsStream("serialization/java/text/DecimalFormatSymbols.ser"));
            DecimalFormatSymbols riSymbols = ((DecimalFormatSymbols) (i.readObject()));
            // RI's default NaN is U+FFFD, Harmony's is based on ICU
            // This suggests an RI bug, assuming that non-UTF8 bytes are UTF8 and
            // getting a conversion failure.
            riSymbols.setNaN("NaN");
            TestCase.assertEquals(symbols, riSymbols);
        } catch (NullPointerException e) {
            TestCase.assertNotNull(("Failed to load /serialization/java/text/" + "DecimalFormatSymbols.ser"), i);
        } finally {
            try {
                if (i != null) {
                    i.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public void test_Constructor() {
        new DecimalFormatSymbols();
    }

    /**
     * java.text.DecimalFormatSymbols#DecimalFormatSymbols(java.util.Locale)
     */
    public void test_ConstructorLjava_util_Locale() {
        try {
            new DecimalFormatSymbols(null);
            TestCase.fail("NullPointerException was not thrown.");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    public void test_getMonetaryDecimalSeparator() {
        dfs.setMonetaryDecimalSeparator(',');
        TestCase.assertEquals("Returned incorrect MonetaryDecimalSeparator symbol", ',', dfs.getMonetaryDecimalSeparator());
    }

    public void test_hashCode() {
        DecimalFormatSymbols dfs1 = new DecimalFormatSymbols();
        DecimalFormatSymbols dfs2 = ((DecimalFormatSymbols) (dfs1.clone()));
        TestCase.assertTrue("Hash codes of equal object are equal", ((dfs2.hashCode()) == (dfs1.hashCode())));
        dfs1.setInfinity("infinity_infinity");
        TestCase.assertTrue("Hash codes of non-equal objects are equal", ((dfs2.hashCode()) != (dfs1.hashCode())));
    }

    public void test_clone() {
        // case 1: Compare of internal variables of cloned objects
        DecimalFormatSymbols fs = new DecimalFormatSymbols(Locale.US);
        DecimalFormatSymbols fsc = ((DecimalFormatSymbols) (fs.clone()));
        TestCase.assertEquals(fs.getCurrency(), fsc.getCurrency());
        // case 2: Compare of clones
        fs = new DecimalFormatSymbols();
        DecimalFormatSymbols fsc2 = ((DecimalFormatSymbols) (fs.clone()));
        // make sure the objects are equal
        TestCase.assertTrue("Object's clone isn't equal!", fs.equals(fsc2));
        // case 3:
        // change the content of the clone and make sure it's not equal
        // anymore
        // verifies that it's data is now distinct from the original
        fs.setNaN("not-a-number");
        TestCase.assertTrue("Object's changed clone should not be equal!", (!(fs.equals(fsc2))));
    }

    public void test_setCurrencySymbolLjava_lang_String() {
        dfs.setCurrencySymbol("$");
        TestCase.assertEquals("Returned incorrect CurrencySymbol symbol", "$", dfs.getCurrencySymbol());
    }

    public void test_setMonetaryDecimalSeparatorC() {
        dfs.setMonetaryDecimalSeparator('#');
        TestCase.assertEquals("Returned incorrect MonetaryDecimalSeparator symbol", '#', dfs.getMonetaryDecimalSeparator());
    }

    public void test_DecimalFormatSymbols_France() {
        /* currency = [EUR]
        currencySymbol = [U+20ac] // EURO SIGN
        decimalSeparator = [,][U+002c]
        digit = [#][U+0023]
        groupingSeparator = [U+00a0] // NON-BREAKING SPACE
        infinity = [U+221e] // INFINITY
        internationalCurrencySymbol = [EUR]
        minusSign = [-][U+002d]
        monetaryDecimalSeparator = [,][U+002c]
        naN = "NaN"
        patternSeparator = [;][U+003b]
        perMill = [U+2030] // PER MILLE
        percent = [%][U+0025]
        zeroDigit = [0][U+0030]
         */
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.FRANCE);
        TestCase.assertEquals("EUR", dfs.getCurrency().getCurrencyCode());
        TestCase.assertEquals("\u20ac", dfs.getCurrencySymbol());
        TestCase.assertEquals(',', dfs.getDecimalSeparator());
        TestCase.assertEquals('#', dfs.getDigit());
        TestCase.assertEquals('\u00a0', dfs.getGroupingSeparator());
        TestCase.assertEquals("\u221e", dfs.getInfinity());
        TestCase.assertEquals("EUR", dfs.getInternationalCurrencySymbol());
        TestCase.assertEquals('-', dfs.getMinusSign());
        TestCase.assertEquals(',', dfs.getMonetaryDecimalSeparator());
        // RI's default NaN is U+FFFD, Harmony's is based on ICU
        // This suggests an RI bug, assuming that non-UTF8 bytes are UTF8 and
        // getting a conversion failure.
        TestCase.assertEquals("NaN", dfs.getNaN());
        TestCase.assertEquals(';', dfs.getPatternSeparator());
        TestCase.assertEquals('\u2030', dfs.getPerMill());
        TestCase.assertEquals('%', dfs.getPercent());
        TestCase.assertEquals('0', dfs.getZeroDigit());
    }
}
