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


import PredicateDescription.GIVEN;
import org.assertj.core.api.Assertions;
import org.assertj.core.description.TextDescription;
import org.assertj.core.presentation.PredicateDescription;
import org.assertj.core.presentation.StandardRepresentation;
import org.junit.jupiter.api.Test;


public class ShouldNotAccept_create_Test {
    @Test
    public void should_create_error_message_with_default_predicate_description() {
        ErrorMessageFactory factory = ShouldNotAccept.shouldNotAccept(( color) -> color.equals("red"), "Yoda", GIVEN);
        String message = factory.create(new TextDescription("Test"), new StandardRepresentation());
        Assertions.assertThat(message).isEqualTo(String.format("[Test] %nExpecting:%n  <given predicate>%nnot to accept <\"Yoda\"> but it did."));
    }

    @Test
    public void should_create_error_message_with_predicate_description() {
        ErrorMessageFactory factory = ShouldNotAccept.shouldNotAccept((String color) -> color.equals("red"), "Yoda", new PredicateDescription("red light saber"));
        String message = factory.create(new TextDescription("Test"), new StandardRepresentation());
        Assertions.assertThat(message).isEqualTo(String.format("[Test] %nExpecting:%n  <\'red light saber\' predicate>%nnot to accept <\"Yoda\"> but it did."));
    }

    @Test
    public void should_fail_if_predicate_description_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> shouldNotAccept(( color) -> color.equals("red"), "Yoda", null)).withMessage("The predicate description must not be null");
    }
}
