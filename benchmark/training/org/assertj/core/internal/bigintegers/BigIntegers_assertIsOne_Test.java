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
package org.assertj.core.internal.bigintegers;


import java.math.BigInteger;
import org.assertj.core.api.Assertions;
import org.assertj.core.internal.BigIntegersBaseTest;
import org.assertj.core.internal.NumbersBaseTest;
import org.assertj.core.test.TestData;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link BigIntegers#assertIsOne(AssertionInfo, BigInteger)}</code>.
 */
public class BigIntegers_assertIsOne_Test extends BigIntegersBaseTest {
    @Test
    public void should_succeed_since_actual_is_one() {
        numbers.assertIsOne(TestData.someInfo(), BigInteger.ONE);
    }

    @Test
    public void should_fail_since_actual_is_not_one() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> numbers.assertIsOne(someInfo(), BigInteger.ZERO)).withMessage(String.format("%nExpecting:%n <0>%nto be equal to:%n <1>%nbut was not."));
    }

    @Test
    public void should_succeed_since_actual_is_one_whatever_custom_comparison_strategy_is() {
        numbersWithComparatorComparisonStrategy.assertIsOne(TestData.someInfo(), BigInteger.ONE);
    }

    @Test
    public void should_fail_since_actual_is_not_one_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> numbersWithComparatorComparisonStrategy.assertIsOne(someInfo(), BigInteger.ZERO)).withMessage(String.format("%nExpecting:%n <0>%nto be equal to:%n <1>%nbut was not."));
    }
}
