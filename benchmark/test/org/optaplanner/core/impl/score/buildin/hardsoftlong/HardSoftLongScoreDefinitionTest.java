/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.score.buildin.hardsoftlong;


import InitializingScoreTrendLevel.ONLY_DOWN;
import InitializingScoreTrendLevel.ONLY_UP;
import org.junit.Assert;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;


public class HardSoftLongScoreDefinitionTest {
    @Test
    public void getLevelSize() {
        Assert.assertEquals(2, new HardSoftLongScoreDefinition().getLevelsSize());
    }

    @Test
    public void getLevelLabels() {
        Assert.assertArrayEquals(new String[]{ "hard score", "soft score" }, new HardSoftLongScoreDefinition().getLevelLabels());
    }

    @Test
    public void getFeasibleLevelsSize() {
        Assert.assertEquals(1, new HardSoftLongScoreDefinition().getFeasibleLevelsSize());
    }

    @Test
    public void buildOptimisticBoundOnlyUp() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore optimisticBound = scoreDefinition.buildOptimisticBound(InitializingScoreTrend.buildUniformTrend(ONLY_UP, 2), HardSoftLongScore.of((-1L), (-2L)));
        Assert.assertEquals(0, optimisticBound.getInitScore());
        Assert.assertEquals(Long.MAX_VALUE, optimisticBound.getHardScore());
        Assert.assertEquals(Long.MAX_VALUE, optimisticBound.getSoftScore());
    }

    @Test
    public void buildOptimisticBoundOnlyDown() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore optimisticBound = scoreDefinition.buildOptimisticBound(InitializingScoreTrend.buildUniformTrend(ONLY_DOWN, 2), HardSoftLongScore.of((-1L), (-2L)));
        Assert.assertEquals(0, optimisticBound.getInitScore());
        Assert.assertEquals((-1L), optimisticBound.getHardScore());
        Assert.assertEquals((-2L), optimisticBound.getSoftScore());
    }

    @Test
    public void buildPessimisticBoundOnlyUp() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore pessimisticBound = scoreDefinition.buildPessimisticBound(InitializingScoreTrend.buildUniformTrend(ONLY_UP, 2), HardSoftLongScore.of((-1L), (-2L)));
        Assert.assertEquals(0, pessimisticBound.getInitScore());
        Assert.assertEquals((-1L), pessimisticBound.getHardScore());
        Assert.assertEquals((-2L), pessimisticBound.getSoftScore());
    }

    @Test
    public void buildPessimisticBoundOnlyDown() {
        HardSoftLongScoreDefinition scoreDefinition = new HardSoftLongScoreDefinition();
        HardSoftLongScore pessimisticBound = scoreDefinition.buildPessimisticBound(InitializingScoreTrend.buildUniformTrend(ONLY_DOWN, 2), HardSoftLongScore.of((-1L), (-2L)));
        Assert.assertEquals(0, pessimisticBound.getInitScore());
        Assert.assertEquals(Long.MIN_VALUE, pessimisticBound.getHardScore());
        Assert.assertEquals(Long.MIN_VALUE, pessimisticBound.getSoftScore());
    }
}
