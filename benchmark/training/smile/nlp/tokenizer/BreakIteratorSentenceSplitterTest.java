/**
 * *****************************************************************************
 * Copyright (c) 2010 Haifeng Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package smile.nlp.tokenizer;


import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Haifeng Li
 */
public class BreakIteratorSentenceSplitterTest {
    public BreakIteratorSentenceSplitterTest() {
    }

    /**
     * Test of split method, of class BreakIteratorSentenceSplitter.
     */
    @Test
    public void testSplit() {
        System.out.println("split");
        String text = "THE BIG RIPOFF\n\nMr. John B. Smith bought cheapsite.com for 1.5 million dollars, i.e. he paid far too much for it. Did he mind? Adam Jones Jr. thinks he didn\'t. In any case, this isn\'t true... Well, with a probability of .9 it isn\'t. ";
        String[] expResult = new String[]{ "THE BIG RIPOFF\n\nMr.", "John B.", "Smith bought cheapsite.com for 1.5 million dollars, i.e. he paid far too much for it.", "Did he mind?", "Adam Jones Jr. thinks he didn't.", "In any case, this isn't true...", "Well, with a probability of .9 it isn't." };
        BreakIteratorSentenceSplitter instance = new BreakIteratorSentenceSplitter();
        String[] result = instance.split(text);
        Assert.assertEquals(expResult.length, result.length);
        for (int i = 0; i < (result.length); i++)
            Assert.assertEquals(expResult[i], result[i]);

    }
}
