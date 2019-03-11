/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.examples.conferencescheduling.app;


import EnvironmentMode.FAST_ASSERT;
import java.io.File;
import org.junit.Test;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;


public class ConferenceSchedulingPerformanceTest extends SolverPerformanceTest<ConferenceSolution> {
    public ConferenceSchedulingPerformanceTest(String moveThreadCount) {
        super(moveThreadCount);
    }

    // ************************************************************************
    // Tests
    // ************************************************************************
    @Test(timeout = 600000)
    public void solveModel() {
        File unsolvedDataFile = new File("data/conferencescheduling/unsolved/72talks-12timeslots-10rooms.xlsx");
        runSpeedTest(unsolvedDataFile, "-2115hard/0medium/-1350915soft");
    }

    @Test(timeout = 600000)
    public void solveModelFastAssert() {
        File unsolvedDataFile = new File("data/conferencescheduling/unsolved/72talks-12timeslots-10rooms.xlsx");
        runSpeedTest(unsolvedDataFile, "-2700hard/0medium/-1379805soft", FAST_ASSERT);
    }
}
