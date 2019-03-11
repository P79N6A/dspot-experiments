/**
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.litho;


import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(ComponentsTestRunner.class)
public class InternalNodeTouchExpansionTest {
    private InternalNode mInternalNode;

    @Test
    public void testTouchExpansionLeftWithoutTouchHandling() {
        mInternalNode.getOrCreateNodeInfo().setTouchHandler(null);
        mInternalNode.touchExpansionPx(LEFT, 10);
        assertThat(mInternalNode.getTouchExpansionLeft()).isEqualTo(0);
    }

    @Test
    public void testTouchExpansionTopWithoutTouchHandling() {
        mInternalNode.getOrCreateNodeInfo().setTouchHandler(null);
        mInternalNode.touchExpansionPx(TOP, 10);
        assertThat(mInternalNode.getTouchExpansionTop()).isEqualTo(0);
    }

    @Test
    public void testTouchExpansionRightWithoutTouchHandling() {
        mInternalNode.getOrCreateNodeInfo().setTouchHandler(null);
        mInternalNode.touchExpansionPx(RIGHT, 10);
        assertThat(mInternalNode.getTouchExpansionRight()).isEqualTo(0);
    }

    @Test
    public void testTouchExpansionBottomWithoutTouchHandling() {
        mInternalNode.getOrCreateNodeInfo().setTouchHandler(null);
        mInternalNode.touchExpansionPx(BOTTOM, 10);
        assertThat(mInternalNode.getTouchExpansionBottom()).isEqualTo(0);
    }

    @Test
    public void testTouchExpansionLeftWithUndefinedStartEnd() {
        mInternalNode.touchExpansionPx(LEFT, 10);
        assertThat(mInternalNode.getTouchExpansionLeft()).isEqualTo(10);
    }

    @Test
    public void testTouchExpansionLeftWithDefinedStart() {
        mInternalNode.touchExpansionPx(START, 5);
        mInternalNode.touchExpansionPx(LEFT, 10);
        assertThat(mInternalNode.getTouchExpansionLeft()).isEqualTo(5);
    }

    @Test
    public void testTouchExpansionLeftWithDefinedEnd() {
        mInternalNode.touchExpansionPx(END, 5);
        mInternalNode.touchExpansionPx(LEFT, 10);
        assertThat(mInternalNode.getTouchExpansionLeft()).isEqualTo(10);
    }

    @Test
    public void testTouchExpansionLeftWithDefinedStartInRtl() {
        InternalNodeTouchExpansionTest.setDirection(mInternalNode, RTL);
        mInternalNode.touchExpansionPx(START, 5);
        mInternalNode.touchExpansionPx(LEFT, 10);
        assertThat(mInternalNode.getTouchExpansionLeft()).isEqualTo(10);
    }

    @Test
    public void testTouchExpansionLeftWithDefinedEndInRtl() {
        InternalNodeTouchExpansionTest.setDirection(mInternalNode, RTL);
        mInternalNode.touchExpansionPx(END, 5);
        mInternalNode.touchExpansionPx(LEFT, 10);
        assertThat(mInternalNode.getTouchExpansionLeft()).isEqualTo(5);
    }

    @Test
    public void testTouchExpansionRightWithUndefinedStartEnd() {
        mInternalNode.touchExpansionPx(RIGHT, 10);
        assertThat(mInternalNode.getTouchExpansionRight()).isEqualTo(10);
    }

    @Test
    public void testTouchExpansionRightWithDefinedStart() {
        mInternalNode.touchExpansionPx(START, 5);
        mInternalNode.touchExpansionPx(RIGHT, 10);
        assertThat(mInternalNode.getTouchExpansionRight()).isEqualTo(10);
    }

    @Test
    public void testTouchExpansionRightWithDefinedEnd() {
        mInternalNode.touchExpansionPx(END, 5);
        mInternalNode.touchExpansionPx(RIGHT, 10);
        assertThat(mInternalNode.getTouchExpansionRight()).isEqualTo(5);
    }

    @Test
    public void testTouchExpansionRightWithDefinedStartInRtl() {
        InternalNodeTouchExpansionTest.setDirection(mInternalNode, RTL);
        mInternalNode.touchExpansionPx(START, 5);
        mInternalNode.touchExpansionPx(RIGHT, 10);
        assertThat(mInternalNode.getTouchExpansionRight()).isEqualTo(5);
    }

    @Test
    public void testTouchExpansionRightWithDefinedEndInRtl() {
        InternalNodeTouchExpansionTest.setDirection(mInternalNode, RTL);
        mInternalNode.touchExpansionPx(END, 5);
        mInternalNode.touchExpansionPx(RIGHT, 10);
        assertThat(mInternalNode.getTouchExpansionRight()).isEqualTo(10);
    }
}
