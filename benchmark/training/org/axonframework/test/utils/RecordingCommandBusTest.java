/**
 * Copyright (c) 2010-2012. Axon Framework
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
package org.axonframework.test.utils;


import java.util.List;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.messaging.MessageHandler;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Allard Buijze
 */
public class RecordingCommandBusTest {
    private RecordingCommandBus testSubject;

    @Test
    public void testPublishCommand() {
        testSubject.dispatch(GenericCommandMessage.asCommandMessage("First"));
        testSubject.dispatch(GenericCommandMessage.asCommandMessage("Second"), ( commandMessage, commandResultMessage) -> {
            if (commandResultMessage.isExceptional()) {
                fail("Didn't expect handling to fail");
            }
            assertNull("Expected default callback behavior to invoke onResult(null)", commandResultMessage.getPayload());
        });
        // noinspection AssertEqualsBetweenInconvertibleTypes
        List<CommandMessage<?>> actual = testSubject.getDispatchedCommands();
        Assert.assertEquals(2, actual.size());
        Assert.assertEquals("First", actual.get(0).getPayload());
        Assert.assertEquals("Second", actual.get(1).getPayload());
    }

    @Test
    public void testPublishCommandWithCallbackBehavior() {
        testSubject.setCallbackBehavior(( commandPayload, commandMetaData) -> "callbackResult");
        testSubject.dispatch(GenericCommandMessage.asCommandMessage("First"));
        testSubject.dispatch(GenericCommandMessage.asCommandMessage("Second"), ( commandMessage, commandResultMessage) -> {
            if (commandResultMessage.isExceptional()) {
                fail("Didn't expect handling to fail");
            }
            assertEquals("callbackResult", commandResultMessage.getPayload());
        });
        // noinspection AssertEqualsBetweenInconvertibleTypes
        List<CommandMessage<?>> actual = testSubject.getDispatchedCommands();
        Assert.assertEquals(2, actual.size());
        Assert.assertEquals("First", actual.get(0).getPayload());
        Assert.assertEquals("Second", actual.get(1).getPayload());
    }

    @Test
    public void testRegisterHandler() {
        MessageHandler<? super CommandMessage<?>> handler = ( command) -> {
            fail("Did not expect handler to be invoked");
            return null;
        };
        testSubject.subscribe(String.class.getName(), handler);
        Assert.assertTrue(testSubject.isSubscribed(handler));
        Assert.assertTrue(testSubject.isSubscribed(String.class.getName(), handler));
    }
}
