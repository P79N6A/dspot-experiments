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
package org.assertj.core.internal.bytearrays;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldNotContain;
import org.assertj.core.internal.ByteArraysBaseTest;
import org.assertj.core.internal.ErrorMessages;
import org.assertj.core.test.ByteArrays;
import org.assertj.core.test.IntArrays;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for <code>{@link ByteArrays#assertDoesNotContain(AssertionInfo, byte[], byte[])}</code>.
 */
public class ByteArrays_assertDoesNotContain_with_Integer_Arguments_Test extends ByteArraysBaseTest {
    @Test
    public void should_pass_if_actual_does_not_contain_given_values() {
        arrays.assertDoesNotContain(TestData.someInfo(), actual, IntArrays.arrayOf(12));
    }

    @Test
    public void should_pass_if_actual_does_not_contain_given_values_even_if_duplicated() {
        arrays.assertDoesNotContain(TestData.someInfo(), actual, IntArrays.arrayOf(12, 12, 20));
    }

    @Test
    public void should_throw_error_if_array_of_values_to_look_for_is_empty() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> arrays.assertDoesNotContain(someInfo(), actual, IntArrays.emptyArray())).withMessage(ErrorMessages.valuesToLookForIsEmpty());
    }

    @Test
    public void should_throw_error_if_array_of_values_to_look_for_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> arrays.assertDoesNotContain(someInfo(), actual, ((int[]) (null)))).withMessage(ErrorMessages.valuesToLookForIsNull());
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> arrays.assertDoesNotContain(someInfo(), null, IntArrays.arrayOf(8))).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_actual_contains_given_values() {
        AssertionInfo info = TestData.someInfo();
        try {
            arrays.assertDoesNotContain(info, actual, IntArrays.arrayOf(6, 8, 20));
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldNotContain.shouldNotContain(actual, ByteArrays.arrayOf(6, 8, 20), Sets.newLinkedHashSet(((byte) (6)), ((byte) (8)))));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_pass_if_actual_does_not_contain_given_values_according_to_custom_comparison_strategy() {
        arraysWithCustomComparisonStrategy.assertDoesNotContain(TestData.someInfo(), actual, IntArrays.arrayOf(12));
    }

    @Test
    public void should_pass_if_actual_does_not_contain_given_values_even_if_duplicated_according_to_custom_comparison_strategy() {
        arraysWithCustomComparisonStrategy.assertDoesNotContain(TestData.someInfo(), actual, IntArrays.arrayOf(12, 12, 20));
    }

    @Test
    public void should_throw_error_if_array_of_values_to_look_for_is_empty_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> arraysWithCustomComparisonStrategy.assertDoesNotContain(someInfo(), actual, IntArrays.emptyArray())).withMessage(ErrorMessages.valuesToLookForIsEmpty());
    }

    @Test
    public void should_throw_error_if_array_of_values_to_look_for_is_null_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> arraysWithCustomComparisonStrategy.assertDoesNotContain(someInfo(), actual, ((int[]) (null)))).withMessage(ErrorMessages.valuesToLookForIsNull());
    }

    @Test
    public void should_fail_if_actual_is_null_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> arraysWithCustomComparisonStrategy.assertDoesNotContain(someInfo(), null, IntArrays.arrayOf((-8)))).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_actual_contains_given_values_according_to_custom_comparison_strategy() {
        AssertionInfo info = TestData.someInfo();
        try {
            arraysWithCustomComparisonStrategy.assertDoesNotContain(info, actual, IntArrays.arrayOf(6, (-8), 20));
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldNotContain.shouldNotContain(actual, ByteArrays.arrayOf(6, (-8), 20), Sets.newLinkedHashSet(((byte) (6)), ((byte) (-8))), absValueComparisonStrategy));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
