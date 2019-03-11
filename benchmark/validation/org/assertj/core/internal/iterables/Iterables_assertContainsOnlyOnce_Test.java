/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2019 the original author or authors.
 */
package org.assertj.core.internal.iterables;


import java.awt.Rectangle;
import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldContainsOnlyOnce;
import org.assertj.core.internal.ErrorMessages;
import org.assertj.core.internal.IterablesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.FailureMessages;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for
 * <code>{@link Iterables#assertContainsOnlyOnce(org.assertj.core.api.AssertionInfo, Iterable, Object[])}</code>.
 *
 * @author William Delanoue
 */
public class Iterables_assertContainsOnlyOnce_Test extends IterablesBaseTest {
    @Test
    public void should_pass_if_actual_contains_given_values_only_once() {
        iterables.assertContainsOnlyOnce(TestData.someInfo(), actual, Arrays.array("Luke", "Yoda", "Leia"));
    }

    @Test
    public void should_pass_if_actual_contains_given_values_only_once_even_if_actual_type_is_not_comparable() {
        // Rectangle class does not implement Comparable
        Rectangle r1 = new Rectangle(1, 1);
        Rectangle r2 = new Rectangle(2, 2);
        iterables.assertContainsOnlyOnce(TestData.someInfo(), Lists.newArrayList(r1, r2, r2), Arrays.array(r1));
    }

    @Test
    public void should_pass_if_actual_contains_given_values_only_once_with_null_element() {
        actual.add(null);
        iterables.assertContainsOnlyOnce(TestData.someInfo(), actual, Arrays.array("Luke", null, "Yoda", "Leia", null));
    }

    @Test
    public void should_pass_if_actual_contains_given_values_only_once_in_different_order() {
        iterables.assertContainsOnlyOnce(TestData.someInfo(), actual, Arrays.array("Leia", "Yoda", "Luke"));
    }

    @Test
    public void should_fail_if_actual_contains_given_values_more_than_once() {
        AssertionInfo info = TestData.someInfo();
        actual.addAll(Lists.newArrayList("Luke", "Luke", null, null));
        Object[] expected = new Object[]{ "Luke", "Luke", "Yoda", "Han", null };
        try {
            iterables.assertContainsOnlyOnce(TestData.someInfo(), actual, expected);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldContainsOnlyOnce.shouldContainsOnlyOnce(actual, expected, Sets.newLinkedHashSet("Han"), Sets.newLinkedHashSet("Luke", null)));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_does_not_contains_null_value() {
        AssertionInfo info = TestData.someInfo();
        actual.addAll(Lists.newArrayList("Luke", "Luke"));
        Object[] expected = new Object[]{ null };
        try {
            iterables.assertContainsOnlyOnce(TestData.someInfo(), actual, expected);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldContainsOnlyOnce.shouldContainsOnlyOnce(actual, expected, Sets.newLinkedHashSet(Arrays.array(((String) (null)))), Sets.newLinkedHashSet()));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_pass_if_actual_contains_given_values_only_once_even_if_duplicated() {
        iterables.assertContainsOnlyOnce(TestData.someInfo(), actual, Arrays.array("Luke", "Luke", "Luke", "Yoda", "Leia"));
    }

    @Test
    public void should_pass_if_actual_and_given_values_are_empty() {
        actual.clear();
        iterables.assertContainsOnlyOnce(TestData.someInfo(), actual, Arrays.array());
    }

    @Test
    public void should_fail_if_array_of_values_to_look_for_is_empty_and_actual_is_not() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> iterables.assertContainsOnlyOnce(someInfo(), actual, emptyArray()));
    }

    @Test
    public void should_throw_error_if_array_of_values_to_look_for_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> iterables.assertContainsOnlyOnce(someInfo(), emptyList(), null)).withMessage(ErrorMessages.valuesToLookForIsNull());
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> iterables.assertContainsOnlyOnce(someInfo(), null, array("Yoda"))).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_actual_does_not_contain_given_values_only_once() {
        AssertionInfo info = TestData.someInfo();
        Object[] expected = new Object[]{ "Luke", "Yoda", "Han" };
        try {
            iterables.assertContainsOnlyOnce(info, actual, expected);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldContainsOnlyOnce.shouldContainsOnlyOnce(actual, expected, Sets.newLinkedHashSet("Han"), Sets.newLinkedHashSet()));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    // ------------------------------------------------------------------------------------------------------------------
    // tests using a custom comparison strategy
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_pass_if_actual_contains_given_values_only_once_according_to_custom_comparison_strategy() {
        iterablesWithCaseInsensitiveComparisonStrategy.assertContainsOnlyOnce(TestData.someInfo(), actual, Arrays.array("LUKE", "YODA", "Leia"));
    }

    @Test
    public void should_pass_if_actual_contains_given_values_only_once_in_different_order_according_to_custom_comparison_strategy() {
        iterablesWithCaseInsensitiveComparisonStrategy.assertContainsOnlyOnce(TestData.someInfo(), actual, Arrays.array("LEIA", "yoda", "LukE"));
    }

    @Test
    public void should_fail_if_actual_contains_given_values_more_than_once_according_to_custom_comparison_strategy() {
        AssertionInfo info = TestData.someInfo();
        actual.addAll(Lists.newArrayList("Luke", "Luke"));
        Object[] expected = Arrays.array("luke", "YOda", "LeIA");
        try {
            iterablesWithCaseInsensitiveComparisonStrategy.assertContainsOnlyOnce(TestData.someInfo(), actual, expected);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldContainsOnlyOnce.shouldContainsOnlyOnce(actual, expected, Sets.newLinkedHashSet(), Sets.newLinkedHashSet("luke"), comparisonStrategy));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_contains_given_values_more_than_once_even_if_duplicated_according_to_custom_comparison_strategy() {
        AssertionInfo info = TestData.someInfo();
        actual.addAll(Lists.newArrayList("LUKE"));
        Object[] expected = Arrays.array("LUke", "LUke", "lukE", "YOda", "Leia", "Han");
        try {
            iterablesWithCaseInsensitiveComparisonStrategy.assertContainsOnlyOnce(TestData.someInfo(), actual, expected);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldContainsOnlyOnce.shouldContainsOnlyOnce(actual, expected, Sets.newLinkedHashSet("Han"), Sets.newLinkedHashSet("LUke", "lukE"), comparisonStrategy));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_does_not_contain_given_values_only_according_to_custom_comparison_strategy() {
        AssertionInfo info = TestData.someInfo();
        Object[] expected = new Object[]{ "Luke", "Yoda", "Han" };
        try {
            iterablesWithCaseInsensitiveComparisonStrategy.assertContainsOnlyOnce(info, actual, expected);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldContainsOnlyOnce.shouldContainsOnlyOnce(actual, expected, Sets.newLinkedHashSet("Han"), Sets.newLinkedHashSet(), comparisonStrategy));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
