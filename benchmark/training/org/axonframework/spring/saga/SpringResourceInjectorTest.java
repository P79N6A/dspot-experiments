/**
 * Copyright (c) 2010-2018. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.axonframework.spring.saga;


import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.modelling.saga.ResourceInjector;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 *
 * @author Allard Buijze
 */
public class SpringResourceInjectorTest {
    private static ResourceInjector testSubject;

    @Test
    public void testInjectSaga() {
        SpringResourceInjectorTest.InjectableSaga injectableSaga = new SpringResourceInjectorTest.InjectableSaga();
        SpringResourceInjectorTest.testSubject.injectResources(injectableSaga);
        Assert.assertNotNull(injectableSaga.getCommandBus());
        Assert.assertNull(injectableSaga.getNonAnnotatedCommandBus());
        Assert.assertNull(injectableSaga.getEventBus());
    }

    @Test(expected = BeanCreationException.class)
    public void testResourcesNotAvailable() {
        SpringResourceInjectorTest.ProblematicInjectableSaga injectableSaga = new SpringResourceInjectorTest.ProblematicInjectableSaga();
        SpringResourceInjectorTest.testSubject.injectResources(injectableSaga);
    }

    public static class InjectableSaga {
        private static final long serialVersionUID = 6273830321273396327L;

        @Autowired
        private CommandBus commandBus1;

        @SuppressWarnings({ "UnusedDeclaration" })
        private CommandBus commandBus2;

        private EventBus eventBus;

        public CommandBus getCommandBus() {
            return commandBus1;
        }

        public CommandBus getNonAnnotatedCommandBus() {
            return commandBus2;
        }

        public EventBus getEventBus() {
            return eventBus;
        }

        @Autowired(required = false)
        public void setEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
        }
    }

    public static class ProblematicInjectableSaga extends SpringResourceInjectorTest.InjectableSaga {
        private static final long serialVersionUID = 3731262948334502511L;

        @Override
        @Autowired(required = true)
        public void setEventBus(EventBus eventBus) {
            throw new UnsupportedOperationException("Method not implemented");
        }
    }

    @Configuration
    public static class Context {
        @Bean
        public ResourceInjector resourceInjector() {
            return new SpringResourceInjector();
        }

        @Bean
        public CommandBus commandBus() {
            return SimpleCommandBus.builder().build();
        }
    }
}
