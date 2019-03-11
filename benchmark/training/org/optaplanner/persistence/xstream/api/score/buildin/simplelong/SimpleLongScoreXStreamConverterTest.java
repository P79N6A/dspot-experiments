/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.persistence.xstream.api.score.buildin.simplelong;


import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.persistence.xstream.api.score.AbstractScoreXStreamConverterTest;


public class SimpleLongScoreXStreamConverterTest extends AbstractScoreXStreamConverterTest {
    @Test
    public void simpleLongScore() {
        assertSerializeAndDeserialize(null, new SimpleLongScoreXStreamConverterTest.TestSimpleLongScoreWrapper(null));
        SimpleLongScore score = SimpleLongScore.of(1234L);
        assertSerializeAndDeserialize(score, new SimpleLongScoreXStreamConverterTest.TestSimpleLongScoreWrapper(score));
        score = SimpleLongScore.ofUninitialized((-7), 1234L);
        assertSerializeAndDeserialize(score, new SimpleLongScoreXStreamConverterTest.TestSimpleLongScoreWrapper(score));
    }

    public static class TestSimpleLongScoreWrapper extends AbstractScoreXStreamConverterTest.TestScoreWrapper<SimpleLongScore> {
        @XStreamConverter(SimpleLongScoreXStreamConverter.class)
        private SimpleLongScore score;

        public TestSimpleLongScoreWrapper(SimpleLongScore score) {
            this.score = score;
        }

        @Override
        public SimpleLongScore getScore() {
            return score;
        }
    }
}
