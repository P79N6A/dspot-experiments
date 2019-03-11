/**
 * Copyright 2013 the original author or authors.
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
package org.springframework.batch.core.jsr;


import javax.batch.api.chunk.listener.ItemProcessListener;
import javax.batch.operations.BatchRuntimeException;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


public class ItemProcessListenerAdapterTests {
    private ItemProcessListenerAdapter<String, String> adapter;

    @Mock
    private ItemProcessListener delegate;

    @Test(expected = IllegalArgumentException.class)
    public void testNullCreation() {
        adapter = new ItemProcessListenerAdapter(null);
    }

    @Test
    public void testBeforeProcess() throws Exception {
        String item = "This is my item";
        adapter.beforeProcess(item);
        Mockito.verify(delegate).beforeProcess(item);
    }

    @Test(expected = BatchRuntimeException.class)
    public void testBeforeProcessException() throws Exception {
        Exception exception = new Exception("This should occur");
        String item = "This is the bad item";
        Mockito.doThrow(exception).when(delegate).beforeProcess(item);
        adapter.beforeProcess(item);
    }

    @Test
    public void testAfterProcess() throws Exception {
        String item = "This is the input";
        String result = "This is the output";
        adapter.afterProcess(item, result);
        Mockito.verify(delegate).afterProcess(item, result);
    }

    @Test(expected = BatchRuntimeException.class)
    public void testAfterProcessException() throws Exception {
        String item = "This is the input";
        String result = "This is the output";
        Exception exception = new Exception("This is expected");
        Mockito.doThrow(exception).when(delegate).afterProcess(item, result);
        adapter.afterProcess(item, result);
    }

    @Test
    public void testOnProcessError() throws Exception {
        String item = "This is the input";
        Exception cause = new Exception("This was the cause");
        adapter.onProcessError(item, cause);
        Mockito.verify(delegate).onProcessError(item, cause);
    }

    @Test(expected = BatchRuntimeException.class)
    public void testOnProcessErrorException() throws Exception {
        String item = "This is the input";
        Exception cause = new Exception("This was the cause");
        Exception exception = new Exception("This is expected");
        Mockito.doThrow(exception).when(delegate).onProcessError(item, cause);
        adapter.onProcessError(item, cause);
    }
}
