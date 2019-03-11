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
package org.assertj.core.util.diff;


import Delta.TYPE;
import Delta.TYPE.DELETE;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


public class DeleteDeltaTest {
    private static List<String> EMPTY_LIST = Collections.emptyList();

    @Test
    public void testGetType() {
        // given
        Chunk<String> chunk = new Chunk(1, DeleteDeltaTest.EMPTY_LIST);
        Delta<String> delta = new DeleteDelta(chunk, chunk);
        // when
        Delta.TYPE type = delta.getType();
        // then
        Assertions.assertThat(type).isEqualTo(DELETE);
    }

    @Test
    public void testToString() {
        // given
        Chunk<String> chunk1 = new Chunk(0, Arrays.asList("line1", "line2"));
        Chunk<String> chunk2 = new Chunk(1, DeleteDeltaTest.EMPTY_LIST);
        Delta<String> delta = new DeleteDelta(chunk1, chunk2);
        // when
        String desc = delta.toString();
        // then
        Assertions.assertThat(desc).isEqualTo(String.format(("Missing content at line 1:%n" + ("  [\"line1\",%n" + "   \"line2\"]%n"))));
    }
}
