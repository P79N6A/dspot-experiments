/**
 * Copyright 2016 ThoughtWorks, Inc.
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
 */
package com.thoughtworks.go.server;


import java.io.IOException;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;
import org.mockito.Mockito;


public class AssetsContextHandlerInitializerTest {
    @Test
    public void shouldInitializeHandlerOnWebappContextLifeCycleStarted() throws IOException {
        AssetsContextHandler handler = Mockito.mock(AssetsContextHandler.class);
        WebAppContext webAppContext = Mockito.mock(WebAppContext.class);
        AssetsContextHandlerInitializer initializer = new AssetsContextHandlerInitializer(handler, webAppContext);
        initializer.lifeCycleStarted(null);
        Mockito.verify(handler, Mockito.times(1)).init(webAppContext);
    }

    @Test
    public void shouldNotInitializeHandlerOnOtherWebappContextLifeCycleEvents() throws IOException {
        AssetsContextHandler handler = Mockito.mock(AssetsContextHandler.class);
        WebAppContext webAppContext = Mockito.mock(WebAppContext.class);
        AssetsContextHandlerInitializer initializer = new AssetsContextHandlerInitializer(handler, webAppContext);
        initializer.lifeCycleStarting(null);
        Mockito.verify(handler, Mockito.never()).init(webAppContext);
    }
}
