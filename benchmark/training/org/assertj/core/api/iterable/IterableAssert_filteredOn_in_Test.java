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


import org.assertj.core.api.Assertions;
import org.assertj.core.data.TolkienCharacter;
import org.assertj.core.data.TolkienCharacterAssert;
import org.assertj.core.data.TolkienCharacterAssertFactory;
import org.assertj.core.util.introspection.IntrospectionError;
import org.junit.jupiter.api.Test;


public class IterableAssert_filteredOn_in_Test extends IterableAssert_filtered_baseTest {
    @Test
    public void should_apply_in_filter() {
        Assertions.assertThat(employees).filteredOn("age", Assertions.in(800, 26)).containsOnly(yoda, obiwan, luke);
        Assertions.assertThat(employees).filteredOn("age", Assertions.in(800)).containsOnly(yoda, obiwan);
    }

    @Test
    public void should_filter_iterable_under_test_on_property_not_backed_by_a_field_values() {
        Assertions.assertThat(employees).filteredOn("adult", Assertions.in(false)).containsOnly(noname);
        Assertions.assertThat(employees).filteredOn("adult", Assertions.in(true)).containsOnly(yoda, obiwan, luke);
    }

    @Test
    public void should_filter_iterable_under_test_on_public_field_values() {
        Assertions.assertThat(employees).filteredOn("id", 1L).containsOnly(yoda);
    }

    @Test
    public void should_filter_iterable_under_test_on_private_field_values() {
        Assertions.assertThat(employees).filteredOn("city", Assertions.in("New York")).containsOnly(yoda, obiwan, luke, noname);
        Assertions.assertThat(employees).filteredOn("city", Assertions.in("Paris")).isEmpty();
    }

    @Test
    public void should_fail_if_filter_is_on_private_field_and_reading_private_field_is_disabled() {
        Assertions.setAllowExtractingPrivateFields(false);
        try {
            Assertions.assertThatExceptionOfType(IntrospectionError.class).isThrownBy(() -> {
                assertThat(employees).filteredOn("city", in("New York")).isEmpty();
            });
        } finally {
            Assertions.setAllowExtractingPrivateFields(true);
        }
    }

    @Test
    public void should_filter_stream_under_test_on_property_values() {
        Assertions.assertThat(employees.stream()).filteredOn("age", Assertions.in(800)).containsOnly(yoda, obiwan);
    }

    @Test
    public void should_filter_iterable_under_test_on_nested_property_values() {
        Assertions.assertThat(employees).filteredOn("name.first", Assertions.in("Luke")).containsOnly(luke);
    }

    @Test
    public void should_filter_iterable_under_test_on_nested_mixed_property_and_field_values() {
        Assertions.assertThat(employees).filteredOn("name.last", Assertions.in("Vader")).isEmpty();
        Assertions.assertThat(employees).filteredOn("name.last", Assertions.in("Skywalker")).containsOnly(luke);
    }

    @Test
    public void should_fail_if_given_property_or_field_name_is_null() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> assertThat(employees).filteredOn(null, in(800))).withMessage("The property/field name to filter on should not be null or empty");
    }

    @Test
    public void should_fail_if_given_property_or_field_name_is_empty() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> assertThat(employees).filteredOn("", in(800))).withMessage("The property/field name to filter on should not be null or empty");
    }

    @Test
    public void should_fail_if_on_of_the_iterable_element_does_not_have_given_property_or_field() {
        Assertions.assertThatExceptionOfType(IntrospectionError.class).isThrownBy(() -> assertThat(employees).filteredOn("secret", in("???"))).withMessageContaining("Can't find any field or property with name 'secret'");
    }

    // these tests validate any FilterOperator with strongly typed navigation assertions
    // no need to write tests for all FilterOperators
    @Test
    public void shoul_honor_AssertFactory_strongly_typed_navigation_assertions() {
        // GIVEN
        Iterable<TolkienCharacter> hobbits = IterableAssert_filtered_baseTest.hobbits();
        TolkienCharacterAssertFactory tolkienCharacterAssertFactory = new TolkienCharacterAssertFactory();
        // THEN
        Assertions.assertThat(hobbits, tolkienCharacterAssertFactory).filteredOn("name", Assertions.in("Frodo")).first().hasAge(33);
        Assertions.assertThat(hobbits, tolkienCharacterAssertFactory).filteredOn("name", Assertions.in("Frodo")).last().hasAge(33);
        Assertions.assertThat(hobbits, tolkienCharacterAssertFactory).filteredOn("name", Assertions.in("Frodo")).element(0).hasAge(33);
    }

    @Test
    public void shoul_honor_ClassBased_strongly_typed_navigation_assertions() {
        // GIVEN
        Iterable<TolkienCharacter> hobbits = IterableAssert_filtered_baseTest.hobbits();
        // THEN
        Assertions.assertThat(hobbits, TolkienCharacterAssert.class).filteredOn("name", Assertions.in("Frodo")).first().hasAge(33);
        Assertions.assertThat(hobbits, TolkienCharacterAssert.class).filteredOn("name", Assertions.in("Frodo")).last().hasAge(33);
        Assertions.assertThat(hobbits, TolkienCharacterAssert.class).filteredOn("name", Assertions.in("Frodo")).element(0).hasAge(33);
    }
}
