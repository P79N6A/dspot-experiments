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
package smile.nlp.keyword;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.junit.Assert;
import org.junit.Test;
import smile.data.parser.IOUtils;
import smile.nlp.NGram;


/**
 *
 *
 * @author Haifeng Li
 */
public class CooccurrenceKeywordExtractorTest {
    public CooccurrenceKeywordExtractorTest() {
    }

    /**
     * Test of extract method, of class KeywordExtractorTest.
     */
    @Test
    public void testExtract() throws FileNotFoundException {
        System.out.println("extract");
        Scanner scanner = new Scanner(IOUtils.getTestDataReader("text/turing.txt"));
        String text = scanner.useDelimiter("\\Z").next();
        scanner.close();
        CooccurrenceKeywordExtractor instance = new CooccurrenceKeywordExtractor();
        ArrayList<NGram> result = instance.extract(text);
        Assert.assertEquals(10, result.size());
        for (NGram ngram : result) {
            System.out.println(ngram);
        }
    }
}
