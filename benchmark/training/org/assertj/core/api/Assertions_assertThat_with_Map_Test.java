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
package org.assertj.core.api;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Assertions#assertThat(Map)}</code>.
 *
 * @author Alex Ruiz
 * @author Nicolas Fran?ois
 */
public class Assertions_assertThat_with_Map_Test {
    @Test
    public void should_create_Assert() {
        AbstractMapAssert<?, ? extends Map<Object, Object>, Object, Object> assertions = Assertions.assertThat(Collections.emptyMap());
        Assertions.assertThat(assertions).isNotNull();
    }

    @Test
    public void should_pass_actual() {
        Map<Object, Object> actual = new HashMap<>();
        AbstractMapAssert<?, ? extends Map<Object, Object>, Object, Object> assertions = Assertions.assertThat(actual);
        Assertions.assertThat(assertions.actual).isSameAs(actual);
    }
}
