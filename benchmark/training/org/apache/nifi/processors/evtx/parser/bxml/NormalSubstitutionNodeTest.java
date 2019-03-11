/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.evtx.parser.bxml;


import BxmlNode.NORMAL_SUBSTITUTION_TOKEN;
import java.io.IOException;
import org.apache.nifi.processors.evtx.parser.BxmlNodeVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class NormalSubstitutionNodeTest extends BxmlNodeWithTokenTestBase {
    private int index = 30;

    private byte type = 20;

    private NormalSubstitutionNode normalSubstitutionNode;

    @Test
    public void testInit() {
        Assert.assertEquals(NORMAL_SUBSTITUTION_TOKEN, normalSubstitutionNode.getToken());
        Assert.assertEquals(index, normalSubstitutionNode.getIndex());
    }

    @Test
    public void testVisitor() throws IOException {
        BxmlNodeVisitor mock = Mockito.mock(BxmlNodeVisitor.class);
        normalSubstitutionNode.accept(mock);
        Mockito.verify(mock).visit(normalSubstitutionNode);
        Mockito.verifyNoMoreInteractions(mock);
    }
}
