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
package org.assertj.core.error;


import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.assertj.core.internal.TestDescription;
import org.assertj.core.presentation.StandardRepresentation;
import org.junit.jupiter.api.Test;


public class ShouldNotBeEqualWithinOffset_create_Test {
    @Test
    public void should_create_error_message() {
        // GIVEN
        ErrorMessageFactory factory = ShouldNotBeEqualWithinOffset.shouldNotBeEqual(8.0F, 6.0F, Offset.offset(5.0F), 2.0F);
        // WHEN
        String message = factory.create(new TestDescription("Test"), StandardRepresentation.STANDARD_REPRESENTATION);
        // THEN
        Assertions.assertThat(message).isEqualTo(String.format(("[Test] %n" + ((((("Expecting:%n" + "  <8.0f>%n") + "not to be close to:%n") + "  <6.0f>%n") + "by less than <5.0f> but difference was <2.0f>.%n") + "(a difference of exactly <5.0f> being considered invalid)"))));
    }

    @Test
    public void should_create_error_message_for_strict_offset() {
        // GIVEN
        ErrorMessageFactory factory = ShouldNotBeEqualWithinOffset.shouldNotBeEqual(8.0F, 6.0F, Assertions.byLessThan(5.0F), 2.0F);
        // WHEN
        String message = factory.create(new TestDescription("Test"), StandardRepresentation.STANDARD_REPRESENTATION);
        // THEN
        Assertions.assertThat(message).isEqualTo(String.format(("[Test] %n" + ((((("Expecting:%n" + "  <8.0f>%n") + "not to be close to:%n") + "  <6.0f>%n") + "by less than <5.0f> but difference was <2.0f>.%n") + "(a difference of exactly <5.0f> being considered valid)"))));
    }
}
