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
package org.assertj.core.internal.bytes;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldBeBetween;
import org.assertj.core.internal.BytesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for <code>{@link Bytes#assertIsBetween(AssertionInfo, Byte, Byte, Byte)}</code>.
 *
 * @author William Delanoue
 */
public class Bytes_assertIsBetween_Test extends BytesBaseTest {
    private static final Byte ZERO = ((byte) (0));

    private static final Byte ONE = ((byte) (1));

    private static final Byte TWO = ((byte) (2));

    private static final Byte TEN = ((byte) (10));

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> bytes.assertIsBetween(someInfo(), null, ZERO, ONE)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_start_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> bytes.assertIsBetween(someInfo(), ONE, null, ONE));
    }

    @Test
    public void should_fail_if_end_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> bytes.assertIsBetween(someInfo(), ONE, ZERO, null));
    }

    @Test
    public void should_pass_if_actual_is_in_range() {
        bytes.assertIsBetween(TestData.someInfo(), Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.ZERO, Bytes_assertIsBetween_Test.TEN);
    }

    @Test
    public void should_pass_if_actual_is_equal_to_range_start() {
        bytes.assertIsBetween(TestData.someInfo(), Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.TEN);
    }

    @Test
    public void should_pass_if_actual_is_equal_to_range_end() {
        bytes.assertIsBetween(TestData.someInfo(), Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.ZERO, Bytes_assertIsBetween_Test.ONE);
    }

    @Test
    public void should_fail_if_actual_is_not_in_range_start() {
        AssertionInfo info = TestData.someInfo();
        try {
            bytes.assertIsBetween(info, Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.TWO, Bytes_assertIsBetween_Test.TEN);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldBeBetween.shouldBeBetween(Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.TWO, Bytes_assertIsBetween_Test.TEN, true, true));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_is_not_in_range_end() {
        AssertionInfo info = TestData.someInfo();
        try {
            bytes.assertIsBetween(info, Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.ZERO, Bytes_assertIsBetween_Test.ZERO);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldBeBetween.shouldBeBetween(Bytes_assertIsBetween_Test.ONE, Bytes_assertIsBetween_Test.ZERO, Bytes_assertIsBetween_Test.ZERO, true, true));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
