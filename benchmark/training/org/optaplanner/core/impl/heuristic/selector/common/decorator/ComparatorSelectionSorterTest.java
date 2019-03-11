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
package org.optaplanner.core.impl.heuristic.selector.common.decorator;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.optaplanner.core.config.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;


public class ComparatorSelectionSorterTest {
    @Test
    public void sort() {
        Integer[] baseArray = new Integer[]{ 3, 4, 3, 5, 1 };
        List<Integer> arrayToSort = new ArrayList<>();
        Collections.addAll(arrayToSort, baseArray);
        ComparatorSelectionSorter<TestdataSolution, Integer> selectionSorter = new ComparatorSelectionSorter(new ComparatorSelectionSorterTest.TestComparator(), SelectionSorterOrder.ASCENDING);
        selectionSorter.sort(null, arrayToSort);
        Assert.assertTrue(ascendingSort(arrayToSort));
        arrayToSort = new ArrayList<>();
        Collections.addAll(arrayToSort, baseArray);
        selectionSorter = new ComparatorSelectionSorter(new ComparatorSelectionSorterTest.TestComparator(), SelectionSorterOrder.DESCENDING);
        selectionSorter.sort(null, arrayToSort);
        Assert.assertTrue(descendingSort(arrayToSort));
    }

    private class TestComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    }
}
