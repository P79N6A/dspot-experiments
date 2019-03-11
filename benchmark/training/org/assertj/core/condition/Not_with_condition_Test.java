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
package org.assertj.core.condition;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.TestCondition;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Not#not(Condition)}</code>.
 *
 * @author Nicolas Fran?ois
 */
public class Not_with_condition_Test {
    @Test
    public void should_create_new_notOf_with_passed_Conditions() {
        TestCondition<Object> condition = new TestCondition<>();
        Condition<Object> created = Not.not(condition);
        Assertions.assertThat(created.getClass()).isEqualTo(Not.class);
        Not<Object> not = ((Not<Object>) (created));
        Assertions.assertThat(not.condition).isEqualTo(condition);
    }
}
