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
package org.assertj.core.internal.maps;


import java.util.List;
import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldHaveSameSizeAs;
import org.assertj.core.internal.MapsBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.util.FailureMessages;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Maps#assertHasSameSizeAs(org.assertj.core.api.AssertionInfo, java.util.Map, Iterable)}</code>.
 *
 * @author Nicolas Fran?ois
 */
public class Maps_assertHasSameSizeAs_with_Iterable_Test extends MapsBaseTest {
    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> maps.assertHasSameSizeAs(someInfo(), null, newArrayList("Solo", "Leia"))).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_size_of_actual_is_not_equal_to_expected_size() {
        AssertionInfo info = TestData.someInfo();
        List<String> other = Lists.newArrayList("Solo", "Leia", "Yoda");
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> maps.assertHasSameSizeAs(info, actual, other)).withMessage(ShouldHaveSameSizeAs.shouldHaveSameSizeAs(actual, actual.size(), other.size()).create(null, info.representation()));
    }

    @Test
    public void should_pass_if_size_of_actual_is_equal_to_expected_size() {
        maps.assertHasSameSizeAs(TestData.someInfo(), actual, Lists.newArrayList("Solo", "Leia"));
    }
}
