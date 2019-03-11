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
package org.optaplanner.core.impl.solver.termination;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;


public class ScoreCalculationCountTerminationTest {
    @Test
    public void solveTermination() {
        Termination termination = new ScoreCalculationCountTermination(1000L);
        DefaultSolverScope solverScope = Mockito.mock(DefaultSolverScope.class);
        InnerScoreDirector scoreDirector = Mockito.mock(InnerScoreDirector.class);
        Mockito.when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(0L);
        Assert.assertEquals(false, termination.isSolverTerminated(solverScope));
        Assert.assertEquals(0.0, termination.calculateSolverTimeGradient(solverScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(100L);
        Assert.assertEquals(false, termination.isSolverTerminated(solverScope));
        Assert.assertEquals(0.1, termination.calculateSolverTimeGradient(solverScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(500L);
        Assert.assertEquals(false, termination.isSolverTerminated(solverScope));
        Assert.assertEquals(0.5, termination.calculateSolverTimeGradient(solverScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(700L);
        Assert.assertEquals(false, termination.isSolverTerminated(solverScope));
        Assert.assertEquals(0.7, termination.calculateSolverTimeGradient(solverScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(1000L);
        Assert.assertEquals(true, termination.isSolverTerminated(solverScope));
        Assert.assertEquals(1.0, termination.calculateSolverTimeGradient(solverScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(1200L);
        Assert.assertEquals(true, termination.isSolverTerminated(solverScope));
        Assert.assertEquals(1.0, termination.calculateSolverTimeGradient(solverScope), 0.0);
    }

    @Test
    public void phaseTermination() {
        Termination termination = new ScoreCalculationCountTermination(1000L);
        AbstractPhaseScope phaseScope = Mockito.mock(AbstractPhaseScope.class);
        InnerScoreDirector scoreDirector = Mockito.mock(InnerScoreDirector.class);
        Mockito.when(phaseScope.getScoreDirector()).thenReturn(scoreDirector);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(0L);
        Assert.assertEquals(false, termination.isPhaseTerminated(phaseScope));
        Assert.assertEquals(0.0, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(100L);
        Assert.assertEquals(false, termination.isPhaseTerminated(phaseScope));
        Assert.assertEquals(0.1, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(500L);
        Assert.assertEquals(false, termination.isPhaseTerminated(phaseScope));
        Assert.assertEquals(0.5, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(700L);
        Assert.assertEquals(false, termination.isPhaseTerminated(phaseScope));
        Assert.assertEquals(0.7, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(1000L);
        Assert.assertEquals(true, termination.isPhaseTerminated(phaseScope));
        Assert.assertEquals(1.0, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        Mockito.when(scoreDirector.getCalculationCount()).thenReturn(1200L);
        Assert.assertEquals(true, termination.isPhaseTerminated(phaseScope));
        Assert.assertEquals(1.0, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
    }
}
