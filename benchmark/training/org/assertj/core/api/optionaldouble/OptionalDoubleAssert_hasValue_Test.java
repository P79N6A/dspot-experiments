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
package org.assertj.core.api.optionaldouble;


import java.util.OptionalDouble;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BaseTest;
import org.assertj.core.error.OptionalShouldContain;
import org.assertj.core.util.AssertionsUtil;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;


public class OptionalDoubleAssert_hasValue_Test extends BaseTest {
    @Test
    public void should_fail_when_optionalDouble_is_null() {
        // GIVEN
        OptionalDouble nullActual = null;
        // THEN
        AssertionsUtil.assertThatAssertionErrorIsThrownBy(() -> assertThat(nullActual).hasValue(10.0)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_pass_if_optionalDouble_has_expected_value() {
        Assertions.assertThat(OptionalDouble.of(10.0)).hasValue(10.0);
    }

    @Test
    public void should_fail_if_optionalDouble_does_not_have_expected_value() {
        // GIVEN
        OptionalDouble actual = OptionalDouble.of(5.0);
        double expectedValue = 10.0;
        // WHEN
        AssertionFailedError error = Assertions.catchThrowableOfType(() -> assertThat(actual).hasValue(expectedValue), AssertionFailedError.class);
        // THEN
        Assertions.assertThat(error).hasMessage(OptionalShouldContain.shouldContain(actual, expectedValue).create());
        Assertions.assertThat(error.getActual().getStringRepresentation()).isEqualTo(String.valueOf(actual.getAsDouble()));
        Assertions.assertThat(error.getExpected().getStringRepresentation()).isEqualTo(String.valueOf(expectedValue));
    }

    @Test
    public void should_fail_if_optionalDouble_is_empty() {
        // GIVEN
        double expectedValue = 10.0;
        // WHEN
        Throwable error = Assertions.catchThrowable(() -> assertThat(OptionalDouble.empty()).hasValue(expectedValue));
        // THEN
        Assertions.assertThat(error).hasMessage(OptionalShouldContain.shouldContain(expectedValue).create());
    }
}
