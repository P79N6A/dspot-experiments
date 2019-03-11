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


import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.assertj.core.description.TextDescription;
import org.assertj.core.presentation.StandardRepresentation;
import org.assertj.core.test.Maps;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;


/**
 * Tests for
 * <code>{@link ShouldContainOnlyKeys#create(org.assertj.core.description.Description, org.assertj.core.presentation.Representation)}</code>
 * .
 *
 * @author Joel Costigliola.
 */
public class ShouldContainOnlyKeys_create_Test {
    @Test
    public void should_create_error_message() {
        ErrorMessageFactory factory = ShouldContainOnlyKeys.shouldContainOnlyKeys(Maps.mapOf(MapEntry.entry("name", "Yoda"), MapEntry.entry("color", "green")), Lists.newArrayList("jedi", "color"), Sets.newLinkedHashSet("jedi"), Sets.newLinkedHashSet("name"));
        String message = factory.create(new TextDescription("Test"), new StandardRepresentation());
        Assertions.assertThat(message).isEqualTo(String.format(("[Test] %n" + ((((((("Expecting:%n" + "  <{\"color\"=\"green\", \"name\"=\"Yoda\"}>%n") + "to contain only following keys:%n") + "  <[\"jedi\", \"color\"]>%n") + "keys not found:%n") + "  <[\"jedi\"]>%n") + "and keys not expected:%n") + "  <[\"name\"]>%n"))));
    }

    @Test
    public void should_not_display_unexpected_elements_when_there_are_none() {
        ErrorMessageFactory factory = ShouldContainOnlyKeys.shouldContainOnlyKeys(Maps.mapOf(MapEntry.entry("color", "green")), Lists.newArrayList("jedi", "color"), Sets.newLinkedHashSet("jedi"), Collections.emptySet());
        String message = factory.create(new TextDescription("Test"), new StandardRepresentation());
        Assertions.assertThat(message).isEqualTo(String.format(("[Test] %n" + ((((("Expecting:%n" + "  <{\"color\"=\"green\"}>%n") + "to contain only following keys:%n") + "  <[\"jedi\", \"color\"]>%n") + "but could not find the following keys:%n") + "  <[\"jedi\"]>%n"))));
    }
}
