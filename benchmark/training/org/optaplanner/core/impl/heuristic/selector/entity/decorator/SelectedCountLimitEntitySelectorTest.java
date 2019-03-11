/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.heuristic.selector.entity.decorator;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;


public class SelectedCountLimitEntitySelectorTest {
    @Test
    public void selectSizeLimitLowerThanSelectorSize() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class, new TestdataEntity("e1"), new TestdataEntity("e2"), new TestdataEntity("e3"), new TestdataEntity("e4"), new TestdataEntity("e5"));
        EntitySelector entitySelector = new SelectedCountLimitEntitySelector(childEntitySelector, true, 3L);
        DefaultSolverScope solverScope = Mockito.mock(DefaultSolverScope.class);
        entitySelector.solvingStarted(solverScope);
        AbstractPhaseScope phaseScopeA = Mockito.mock(AbstractPhaseScope.class);
        Mockito.when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        entitySelector.phaseStarted(phaseScopeA);
        AbstractStepScope stepScopeA1 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        entitySelector.stepStarted(stepScopeA1);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeA1);
        AbstractStepScope stepScopeA2 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        entitySelector.stepStarted(stepScopeA2);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeA2);
        entitySelector.phaseEnded(phaseScopeA);
        AbstractPhaseScope phaseScopeB = Mockito.mock(AbstractPhaseScope.class);
        Mockito.when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        entitySelector.phaseStarted(phaseScopeB);
        AbstractStepScope stepScopeB1 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        entitySelector.stepStarted(stepScopeB1);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeB1);
        AbstractStepScope stepScopeB2 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeB2.getPhaseScope()).thenReturn(phaseScopeB);
        entitySelector.stepStarted(stepScopeB2);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeB2);
        AbstractStepScope stepScopeB3 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeB3.getPhaseScope()).thenReturn(phaseScopeB);
        entitySelector.stepStarted(stepScopeB3);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeB3);
        entitySelector.phaseEnded(phaseScopeB);
        entitySelector.solvingEnded(solverScope);
        PlannerAssert.verifyPhaseLifecycle(childEntitySelector, 1, 2, 5);
        Mockito.verify(childEntitySelector, Mockito.times(5)).iterator();
        Mockito.verify(childEntitySelector, Mockito.times(5)).getSize();
    }

    @Test
    public void selectSizeLimitHigherThanSelectorSize() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class, new TestdataEntity("e1"), new TestdataEntity("e2"), new TestdataEntity("e3"));
        EntitySelector entitySelector = new SelectedCountLimitEntitySelector(childEntitySelector, true, 5L);
        DefaultSolverScope solverScope = Mockito.mock(DefaultSolverScope.class);
        entitySelector.solvingStarted(solverScope);
        AbstractPhaseScope phaseScopeA = Mockito.mock(AbstractPhaseScope.class);
        Mockito.when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        entitySelector.phaseStarted(phaseScopeA);
        AbstractStepScope stepScopeA1 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        entitySelector.stepStarted(stepScopeA1);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeA1);
        AbstractStepScope stepScopeA2 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        entitySelector.stepStarted(stepScopeA2);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeA2);
        entitySelector.phaseEnded(phaseScopeA);
        AbstractPhaseScope phaseScopeB = Mockito.mock(AbstractPhaseScope.class);
        Mockito.when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        entitySelector.phaseStarted(phaseScopeB);
        AbstractStepScope stepScopeB1 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        entitySelector.stepStarted(stepScopeB1);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeB1);
        AbstractStepScope stepScopeB2 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeB2.getPhaseScope()).thenReturn(phaseScopeB);
        entitySelector.stepStarted(stepScopeB2);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeB2);
        AbstractStepScope stepScopeB3 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeB3.getPhaseScope()).thenReturn(phaseScopeB);
        entitySelector.stepStarted(stepScopeB3);
        PlannerAssert.assertAllCodesOfEntitySelector(entitySelector, "e1", "e2", "e3");
        entitySelector.stepEnded(stepScopeB3);
        entitySelector.phaseEnded(phaseScopeB);
        entitySelector.solvingEnded(solverScope);
        PlannerAssert.verifyPhaseLifecycle(childEntitySelector, 1, 2, 5);
        Mockito.verify(childEntitySelector, Mockito.times(5)).iterator();
        Mockito.verify(childEntitySelector, Mockito.times(5)).getSize();
    }

    @Test
    public void isCountable() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class);
        EntitySelector entitySelector = new SelectedCountLimitEntitySelector(childEntitySelector, true, 5L);
        Assert.assertEquals(true, entitySelector.isCountable());
    }

    @Test
    public void isNeverEnding() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class);
        EntitySelector entitySelector = new SelectedCountLimitEntitySelector(childEntitySelector, true, 5L);
        Assert.assertEquals(false, entitySelector.isNeverEnding());
    }

    @Test
    public void getSize() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class);
        Mockito.when(childEntitySelector.getSize()).thenReturn(1L);
        EntitySelector entitySelector = new SelectedCountLimitEntitySelector(childEntitySelector, true, 5L);
        Assert.assertEquals(1, entitySelector.getSize());
        Mockito.when(childEntitySelector.getSize()).thenReturn(5L);
        Assert.assertEquals(5, entitySelector.getSize());
        Mockito.when(childEntitySelector.getSize()).thenReturn(10L);
        Assert.assertEquals(5, entitySelector.getSize());
    }

    @Test
    public void endingIteratorOriginalOrder() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class, new TestdataEntity("e1"), new TestdataEntity("e2"), new TestdataEntity("e3"), new TestdataEntity("e4"));
        EntitySelector entitySelector = new SelectedCountLimitEntitySelector(childEntitySelector, false, 2L);
        PlannerAssert.assertAllCodesOfIterator(entitySelector.endingIterator(), "e1", "e2");
    }

    @Test
    public void endingIteratorRandomOrder() {
        EntitySelector childEntitySelector = SelectorTestUtils.mockEntitySelector(TestdataEntity.class, new TestdataEntity("e1"), new TestdataEntity("e2"), new TestdataEntity("e3"), new TestdataEntity("e4"));
        EntitySelector entitySelector = new SelectedCountLimitEntitySelector(childEntitySelector, true, 2L);
        PlannerAssert.assertAllCodesOfIterator(entitySelector.endingIterator(), "e1", "e2", "e3", "e4");
    }
}
