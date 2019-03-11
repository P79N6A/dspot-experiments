package org.robolectric.shadows;


import android.webkit.JsPromptResult;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ShadowJsPromptResultTest {
    @Test
    public void shouldConstruct() throws Exception {
        JsPromptResult result = ShadowJsPromptResult.newInstance();
        Assert.assertNotNull(result);
    }
}
