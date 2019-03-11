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
package org.assertj.core.internal.strings;


import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldNotEndWith;
import org.assertj.core.internal.StringsBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Strings#assertDoesNotEndWith(AssertionInfo, CharSequence, CharSequence)}</code>.
 *
 * @author Michal Kordas
 */
public class Strings_assertDoesNotEndWith_Test extends StringsBaseTest {
    @Test
    public void should_pass_if_actual_does_not_end_with_suffix() {
        strings.assertDoesNotEndWith(TestData.someInfo(), "Yoda", "Luke");
        strings.assertDoesNotEndWith(TestData.someInfo(), "Yoda", "DA");
    }

    @Test
    public void should_fail_if_actual_ends_with_suffix() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> strings.assertDoesNotEndWith(someInfo(), "Yoda", "oda")).withMessage(ShouldNotEndWith.shouldNotEndWith("Yoda", "oda").create());
    }

    @Test
    public void should_throw_error_if_suffix_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> strings.assertDoesNotEndWith(someInfo(), "Yoda", null)).withMessage("The given suffix should not be null");
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> strings.assertDoesNotEndWith(someInfo(), null, "Yoda")).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_pass_if_actual_does_not_end_with_suffix_according_to_custom_comparison_strategy() {
        stringsWithCaseInsensitiveComparisonStrategy.assertDoesNotEndWith(TestData.someInfo(), "Yoda", "Luke");
    }

    @Test
    public void should_fail_if_actual_ends_with_suffix_according_to_custom_comparison_strategy() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> stringsWithCaseInsensitiveComparisonStrategy.assertDoesNotEndWith(someInfo(), "Yoda", "A")).withMessage(ShouldNotEndWith.shouldNotEndWith("Yoda", "A", comparisonStrategy).create());
    }
}
