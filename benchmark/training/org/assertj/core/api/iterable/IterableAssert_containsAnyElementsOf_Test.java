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
package org.assertj.core.api.iterable;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssertBaseTest;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link AbstractIterableAssert#containsAnyElementsOf(Iterable)}}</code>.
 *
 * @author Marko Bekhta
 */
public class IterableAssert_containsAnyElementsOf_Test extends IterableAssertBaseTest {
    private final List<Object> iterable = Arrays.asList(new Object(), "bar");

    @Test
    public void should_compile_as_containsAnyElementsOf_declares_bounded_wildcard_parameter() {
        // GIVEN
        Map<String, String> map = Maps.newHashMap("some_key", "some_value");
        // THEN
        Assertions.assertThat(map).extracting("some_key").containsAnyElementsOf(Lists.list("some_value", "some_other_value"));
    }
}
