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
package org.optaplanner.core.impl.constructionheuristic.placer.entity;


import java.util.Iterator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.optaplanner.core.impl.constructionheuristic.placer.Placement;
import org.optaplanner.core.impl.constructionheuristic.placer.QueuedValuePlacer;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicRecordingValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;


public class QueuedValuePlacerTest extends AbstractEntityPlacerTest {
    @Test
    public void oneMoveSelector() {
        GenuineVariableDescriptor variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();
        EntitySelector entitySelector = SelectorTestUtils.mockEntitySelector(variableDescriptor.getEntityDescriptor(), new TestdataEntity("a"), new TestdataEntity("b"), new TestdataEntity("c"));
        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(variableDescriptor, new TestdataValue("1"), new TestdataValue("2"));
        MimicRecordingValueSelector recordingValueSelector = new MimicRecordingValueSelector(valueSelector);
        MoveSelector moveSelector = new org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelector(entitySelector, new org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicReplayingValueSelector(recordingValueSelector), false);
        QueuedValuePlacer placer = new QueuedValuePlacer(recordingValueSelector, moveSelector);
        DefaultSolverScope solverScope = Mockito.mock(DefaultSolverScope.class);
        placer.solvingStarted(solverScope);
        AbstractPhaseScope phaseScopeA = Mockito.mock(AbstractPhaseScope.class);
        Mockito.when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        placer.phaseStarted(phaseScopeA);
        Iterator<Placement> placementIterator = placer.iterator();
        Assert.assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeA1 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        placer.stepStarted(stepScopeA1);
        AbstractEntityPlacerTest.assertValuePlacement(placementIterator.next(), "1", "a", "b", "c");
        placer.stepEnded(stepScopeA1);
        Assert.assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeA2 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        placer.stepStarted(stepScopeA2);
        AbstractEntityPlacerTest.assertValuePlacement(placementIterator.next(), "2", "a", "b", "c");
        placer.stepEnded(stepScopeA2);
        Assert.assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeA3 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeA3.getPhaseScope()).thenReturn(phaseScopeA);
        placer.stepStarted(stepScopeA3);
        AbstractEntityPlacerTest.assertValuePlacement(placementIterator.next(), "1", "a", "b", "c");
        placer.stepEnded(stepScopeA3);
        // Requires adding ReinitializeVariableValueSelector complexity to work
        // assertFalse(placementIterator.hasNext());
        placer.phaseEnded(phaseScopeA);
        AbstractPhaseScope phaseScopeB = Mockito.mock(AbstractPhaseScope.class);
        Mockito.when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        placer.phaseStarted(phaseScopeB);
        placementIterator = placer.iterator();
        Assert.assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeB1 = Mockito.mock(AbstractStepScope.class);
        Mockito.when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        placer.stepStarted(stepScopeB1);
        AbstractEntityPlacerTest.assertValuePlacement(placementIterator.next(), "1", "a", "b", "c");
        placer.stepEnded(stepScopeB1);
        placer.phaseEnded(phaseScopeB);
        placer.solvingEnded(solverScope);
        PlannerAssert.verifyPhaseLifecycle(entitySelector, 1, 2, 4);
        PlannerAssert.verifyPhaseLifecycle(valueSelector, 1, 2, 4);
    }
}
