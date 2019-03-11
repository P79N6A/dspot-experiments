/**
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.litho;


import android.content.Context;
import com.facebook.litho.reference.Reference;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(ComponentsTestRunner.class)
public class ComponentUtilsTest {
    private ComponentUtilsTest.ComponentTest mC1;

    private ComponentUtilsTest.ComponentTest mC2;

    @Test
    public void hasEquivalentFieldsArrayIntPropTest() {
        mC1.propArrayInt = new int[]{ 2, 5, 6 };
        mC2.propArrayInt = new int[]{ 2, 5, 6 };
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propArrayInt = new int[]{ 2, 3 };
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsArrayCharPropTest() {
        mC1.propArrayChar = new char[]{ 'a', 'c', '5' };
        mC2.propArrayChar = new char[]{ 'a', 'c', '5' };
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propArrayChar = new char[]{ 'a', 'c' };
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsStateContainersTest() {
        mC1.stateContainer = new ComponentUtilsTest.StateTest(true, 3.0F);
        mC2.stateContainer = new ComponentUtilsTest.StateTest(true, 3.0F);
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.stateContainer = new ComponentUtilsTest.StateTest(true, 2.0F);
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsDoublePropTest() {
        mC1.propDouble = 2.0;
        mC2.propDouble = 2.0;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propDouble = 3.0;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsFloatPropTest() {
        mC1.propFloat = 2.0F;
        mC2.propFloat = 2.0F;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propFloat = 3.0F;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsCharPropTest() {
        mC1.propChar = 'c';
        mC2.propChar = 'c';
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propChar = 'z';
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsBytePropTest() {
        mC1.propByte = 1;
        mC2.propByte = 1;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propByte = 2;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsShortPropTest() {
        mC1.propShort = 3;
        mC2.propShort = 3;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propShort = 2;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsIntPropTest() {
        mC1.propInt = 3;
        mC2.propInt = 3;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propInt = 2;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsLongPropTest() {
        mC1.propLong = 3;
        mC2.propLong = 3;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propLong = 2;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsBooleanPropTest() {
        mC1.propBoolean = true;
        mC2.propBoolean = true;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propBoolean = false;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsIntBoxedPropTest() {
        mC1.propIntBoxed = new Integer(3);
        mC2.propIntBoxed = new Integer(3);
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propIntBoxed = new Integer(2);
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
        mC2.propIntBoxed = null;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsStringPropTest() {
        mC1.propString = "string";
        mC2.propString = "string";
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propString = "bla";
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
        mC2.propString = null;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsReferencePropTest() {
        mC1.propReference = new ComponentUtilsTest.TestReference("aa");
        mC2.propReference = new ComponentUtilsTest.TestReference("aa");
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propReference = new ComponentUtilsTest.TestReference("ab");
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsCollectionPropTest() {
        mC1.propCollection = Arrays.asList("1", "2", "3");
        mC2.propCollection = Arrays.asList("1", "2", "3");
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propCollection = Arrays.asList("2", "3");
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsCollectionWithComponentsPropTest() {
        ComponentUtilsTest.ComponentTest innerComponent11 = new ComponentUtilsTest.ComponentTest();
        innerComponent11.propDouble = 2.0;
        ComponentUtilsTest.ComponentTest innerComponent12 = new ComponentUtilsTest.ComponentTest();
        innerComponent12.propDouble = 2.0;
        ComponentUtilsTest.ComponentTest innerComponent21 = new ComponentUtilsTest.ComponentTest();
        innerComponent21.propDouble = 2.0;
        ComponentUtilsTest.ComponentTest innerComponent22 = new ComponentUtilsTest.ComponentTest();
        innerComponent22.propDouble = 2.0;
        mC1.propCollectionWithComponents = Arrays.asList(innerComponent11, innerComponent12);
        mC2.propCollectionWithComponents = Arrays.asList(innerComponent21, innerComponent22);
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        innerComponent22.propDouble = 3.0;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsComponentPropTest() {
        ComponentUtilsTest.ComponentTest innerComponent1 = new ComponentUtilsTest.ComponentTest();
        innerComponent1.propDouble = 2.0;
        ComponentUtilsTest.ComponentTest innerComponent2 = new ComponentUtilsTest.ComponentTest();
        innerComponent2.propDouble = 2.0;
        mC1.propComponent = innerComponent1;
        mC2.propComponent = innerComponent2;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        innerComponent2.propDouble = 3.0;
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsEventHandlerPropTest() {
        // The first item of the params is skipped as explained in the EventHandler class.
        mC1.propEventHandler = new EventHandler(null, 3, new Object[]{ "", "1" });
        mC2.propEventHandler = new EventHandler(null, 3, new Object[]{ "", "1" });
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.propEventHandler = new EventHandler(null, 3, new Object[]{ "", "2" });
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    @Test
    public void hasEquivalentFieldsTreePropTest() {
        mC1.treePropObject = new String("1");
        mC2.treePropObject = new String("1");
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isTrue();
        mC2.treePropObject = new String("2");
        assertThat(ComponentUtils.hasEquivalentFields(mC1, mC2)).isFalse();
    }

    private static class ComponentTest extends Component {
        @Comparable(type = Comparable.ARRAY)
        int[] propArrayInt;

        @Comparable(type = Comparable.ARRAY)
        char[] propArrayChar;

        @Comparable(type = Comparable.DOUBLE)
        double propDouble;

        @Comparable(type = Comparable.FLOAT)
        float propFloat;

        @Comparable(type = Comparable.PRIMITIVE)
        char propChar;

        @Comparable(type = Comparable.PRIMITIVE)
        byte propByte;

        @Comparable(type = Comparable.PRIMITIVE)
        short propShort;

        @Comparable(type = Comparable.PRIMITIVE)
        int propInt;

        @Comparable(type = Comparable.PRIMITIVE)
        long propLong;

        @Comparable(type = Comparable.PRIMITIVE)
        boolean propBoolean;

        @Comparable(type = Comparable.OTHER)
        Integer propIntBoxed;

        @Comparable(type = Comparable.OTHER)
        String propString;

        @Comparable(type = Comparable.REFERENCE)
        Reference propReference;

        @Comparable(type = Comparable.COLLECTION_COMPLEVEL_0)
        Collection<String> propCollection;

        @Comparable(type = Comparable.COLLECTION_COMPLEVEL_1)
        Collection<Component> propCollectionWithComponents;

        @Comparable(type = Comparable.COMPONENT)
        Component propComponent;

        @Comparable(type = Comparable.EVENT_HANDLER)
        EventHandler propEventHandler;

        @Comparable(type = Comparable.OTHER)
        Object treePropObject;

        @Comparable(type = Comparable.STATE_CONTAINER)
        ComponentUtilsTest.StateTest stateContainer = new ComponentUtilsTest.StateTest();

        protected ComponentTest() {
            super("test");
        }
    }

    private static class StateTest implements StateContainer {
        @Comparable(type = Comparable.PRIMITIVE)
        boolean state1;

        @Comparable(type = Comparable.FLOAT)
        float state2;

        StateTest(boolean state1, float state2) {
            this.state1 = state1;
            this.state2 = state2;
        }

        StateTest() {
        }
    }

    private static class TestReference extends Reference {
        private String mVal;

        TestReference(String val) {
            super(new com.facebook.litho.reference.ReferenceLifecycle<String>() {
                @Override
                protected String onAcquire(Context context, Reference<String> reference) {
                    return "";
                }
            });
            mVal = val;
        }

        @Override
        public boolean equals(Object o) {
            ComponentUtilsTest.TestReference other = ((ComponentUtilsTest.TestReference) (o));
            return ((this) == o) || (mVal.equals(other.mVal));
        }

        @Override
        public String getSimpleName() {
            return "TestReference";
        }
    }
}
