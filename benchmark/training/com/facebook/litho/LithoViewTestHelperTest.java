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


import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.litho.widget.Text;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static MeasureSpec.makeMeasureSpec;


@RunWith(ComponentsTestRunner.class)
public class LithoViewTestHelperTest {
    @Test
    public void testBasicViewToString() {
        final Component component = new InlineLayoutSpec() {
            @Override
            protected Component onCreateLayout(ComponentContext c) {
                return TestDrawableComponent.create(c).widthPx(100).heightPx(100).build();
            }
        };
        final LithoView lithoView = new LithoView(RuntimeEnvironment.application);
        lithoView.setComponent(component);
        lithoView.measure(makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        final String string = LithoViewTestHelper.viewToString(lithoView);
        assertThat(string).containsPattern(("litho.InlineLayout\\{\\w+ V.E..... .. 0,0-100,100\\}\n" + "  litho.TestDrawableComponent\\{\\w+ V.E..... .. 0,0-100,100\\}"));
    }

    @Test
    public void testViewToStringWithText() {
        final Component component = new InlineLayoutSpec() {
            @Override
            protected Component onCreateLayout(ComponentContext c) {
                return Column.create(c).child(TestDrawableComponent.create(c).testKey("test-drawable").widthPx(100).heightPx(100)).child(Text.create(c).widthPx(100).heightPx(100).text("Hello, World")).build();
            }
        };
        final LithoView lithoView = new LithoView(RuntimeEnvironment.application);
        lithoView.setComponent(component);
        lithoView.measure(makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        lithoView.layout(0, 0, lithoView.getMeasuredWidth(), lithoView.getMeasuredHeight());
        final String string = LithoViewTestHelper.viewToString(lithoView);
        assertThat(string).containsPattern(("litho.InlineLayout\\{\\w+ V.E..... .. 0,0-100,200\\}\n" + ("  litho.TestDrawableComponent\\{\\w+ V.E..... .. 0,0-100,100 litho:id/test-drawable\\}\n" + "  litho.Text\\{\\w+ V.E..... .. 0,100-100,200 text=\"Hello, World\"\\}")));
    }
}
