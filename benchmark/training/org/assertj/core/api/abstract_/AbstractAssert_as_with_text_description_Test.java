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
package org.assertj.core.api.abstract_;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.ConcreteAssert;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link AbstractAssert#as(String)}</code>.
 *
 * @author Alex Ruiz
 */
public class AbstractAssert_as_with_text_description_Test {
    private ConcreteAssert assertions;

    private String description;

    @Test
    public void should_set_description() {
        as(description);
        Assertions.assertThat(descriptionText()).isEqualTo(description);
    }

    @Test
    public void should_return_this() {
        Assertions.assertThat(as(description)).isSameAs(assertions);
    }

    @Test
    public void should_set_empty_description_if_description_is_null() {
        String description = null;
        as(description);
        Assertions.assertThat(descriptionText()).isEmpty();
    }
}
