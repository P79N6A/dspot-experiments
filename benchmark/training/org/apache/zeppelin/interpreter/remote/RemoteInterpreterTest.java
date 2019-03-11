/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.interpreter.remote;


import Code.ERROR;
import Code.SUCCESS;
import Interpreter.FormType.NATIVE;
import InterpreterOption.ISOLATED;
import InterpreterOption.SCOPED;
import InterpreterOption.SHARED;
import OptionInput.ParamOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.thrift.transport.TTransportException;
import org.apache.zeppelin.display.AngularObjectRegistry;
import org.apache.zeppelin.display.GUI;
import org.apache.zeppelin.display.Input;
import org.apache.zeppelin.display.ui.OptionInput;
import org.apache.zeppelin.interpreter.AbstractInterpreterTest;
import org.junit.Assert;
import org.junit.Test;


public class RemoteInterpreterTest extends AbstractInterpreterTest {
    private InterpreterSetting interpreterSetting;

    @Test
    public void testSharedMode() throws IOException, InterpreterException {
        interpreterSetting.getOption().setPerUser(SHARED);
        Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
        Interpreter interpreter2 = interpreterSetting.getDefaultInterpreter("user2", "note1");
        Assert.assertTrue((interpreter1 instanceof RemoteInterpreter));
        RemoteInterpreter remoteInterpreter1 = ((RemoteInterpreter) (interpreter1));
        Assert.assertTrue((interpreter2 instanceof RemoteInterpreter));
        RemoteInterpreter remoteInterpreter2 = ((RemoteInterpreter) (interpreter2));
        Assert.assertEquals(remoteInterpreter1.getScheduler(), remoteInterpreter2.getScheduler());
        InterpreterContext context1 = createDummyInterpreterContext();
        Assert.assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
        Assert.assertEquals(NATIVE, interpreter1.getFormType());
        Assert.assertEquals(0, remoteInterpreter1.getProgress(context1));
        Assert.assertNotNull(remoteInterpreter1.getOrCreateInterpreterProcess());
        Assert.assertTrue(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());
        Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
        Assert.assertEquals(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess(), remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
        // Call InterpreterGroup.close instead of Interpreter.close, otherwise we will have the
        // RemoteInterpreterProcess leakage.
        remoteInterpreter1.getInterpreterGroup().close(remoteInterpreter1.getSessionId());
        Assert.assertNull(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess());
        try {
            Assert.assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
            Assert.fail("Should not be able to call interpret after interpreter is closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
            Assert.fail("Should not be able to call getProgress after RemoterInterpreterProcess is stoped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testScopedMode() throws IOException, InterpreterException {
        interpreterSetting.getOption().setPerUser(SCOPED);
        Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
        Interpreter interpreter2 = interpreterSetting.getDefaultInterpreter("user2", "note1");
        Assert.assertTrue((interpreter1 instanceof RemoteInterpreter));
        RemoteInterpreter remoteInterpreter1 = ((RemoteInterpreter) (interpreter1));
        Assert.assertTrue((interpreter2 instanceof RemoteInterpreter));
        RemoteInterpreter remoteInterpreter2 = ((RemoteInterpreter) (interpreter2));
        Assert.assertNotEquals(interpreter1.getScheduler(), interpreter2.getScheduler());
        InterpreterContext context1 = createDummyInterpreterContext();
        Assert.assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
        Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
        Assert.assertEquals(NATIVE, interpreter1.getFormType());
        Assert.assertEquals(0, remoteInterpreter1.getProgress(context1));
        Assert.assertNotNull(remoteInterpreter1.getOrCreateInterpreterProcess());
        Assert.assertTrue(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());
        Assert.assertEquals(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess(), remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
        // Call InterpreterGroup.close instead of Interpreter.close, otherwise we will have the
        // RemoteInterpreterProcess leakage.
        remoteInterpreter1.getInterpreterGroup().close(remoteInterpreter1.getSessionId());
        try {
            Assert.assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
            Assert.fail("Should not be able to call interpret after interpreter is closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());
        Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
        remoteInterpreter2.getInterpreterGroup().close(remoteInterpreter2.getSessionId());
        try {
            Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1));
            Assert.fail("Should not be able to call interpret after interpreter is closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNull(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
    }

    @Test
    public void testIsolatedMode() throws IOException, InterpreterException {
        interpreterSetting.getOption().setPerUser(ISOLATED);
        Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
        Interpreter interpreter2 = interpreterSetting.getDefaultInterpreter("user2", "note1");
        Assert.assertTrue((interpreter1 instanceof RemoteInterpreter));
        RemoteInterpreter remoteInterpreter1 = ((RemoteInterpreter) (interpreter1));
        Assert.assertTrue((interpreter2 instanceof RemoteInterpreter));
        RemoteInterpreter remoteInterpreter2 = ((RemoteInterpreter) (interpreter2));
        Assert.assertNotEquals(interpreter1.getScheduler(), interpreter2.getScheduler());
        InterpreterContext context1 = createDummyInterpreterContext();
        Assert.assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
        Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
        Assert.assertEquals(NATIVE, interpreter1.getFormType());
        Assert.assertEquals(0, remoteInterpreter1.getProgress(context1));
        Assert.assertNotNull(remoteInterpreter1.getOrCreateInterpreterProcess());
        Assert.assertTrue(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());
        Assert.assertNotEquals(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess(), remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
        // Call InterpreterGroup.close instead of Interpreter.close, otherwise we will have the
        // RemoteInterpreterProcess leakage.
        remoteInterpreter1.getInterpreterGroup().close(remoteInterpreter1.getSessionId());
        Assert.assertNull(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess());
        Assert.assertTrue(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());
        try {
            remoteInterpreter1.interpret("hello", context1);
            Assert.fail("Should not be able to call getProgress after interpreter is closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
        remoteInterpreter2.getInterpreterGroup().close(remoteInterpreter2.getSessionId());
        try {
            Assert.assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
            Assert.fail("Should not be able to call interpret after interpreter is closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNull(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
    }

    @Test
    public void testExecuteIncorrectPrecode() throws IOException, TTransportException, InterpreterException {
        interpreterSetting.getOption().setPerUser(SHARED);
        interpreterSetting.setProperty("zeppelin.SleepInterpreter.precode", "fail test");
        Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
        InterpreterContext context1 = createDummyInterpreterContext();
        Assert.assertEquals(ERROR, interpreter1.interpret("10", context1).code());
    }

    @Test
    public void testExecuteCorrectPrecode() throws IOException, TTransportException, InterpreterException {
        interpreterSetting.getOption().setPerUser(SHARED);
        interpreterSetting.setProperty("zeppelin.SleepInterpreter.precode", "1");
        Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
        InterpreterContext context1 = createDummyInterpreterContext();
        Assert.assertEquals(SUCCESS, interpreter1.interpret("10", context1).code());
    }

    @Test
    public void testRemoteInterperterErrorStatus() throws IOException, TTransportException, InterpreterException {
        interpreterSetting.setProperty("zeppelin.interpreter.echo.fail", "true");
        interpreterSetting.getOption().setPerUser(SHARED);
        Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
        Assert.assertTrue((interpreter1 instanceof RemoteInterpreter));
        RemoteInterpreter remoteInterpreter1 = ((RemoteInterpreter) (interpreter1));
        InterpreterContext context1 = createDummyInterpreterContext();
        Assert.assertEquals(ERROR, remoteInterpreter1.interpret("hello", context1).code());
    }

    @Test
    public void testFIFOScheduler() throws InterruptedException, InterpreterException {
        interpreterSetting.getOption().setPerUser(SHARED);
        // by default SleepInterpreter would use FIFOScheduler
        final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
        final InterpreterContext context1 = createDummyInterpreterContext();
        // run this dummy interpret method first to launch the RemoteInterpreterProcess to avoid the
        // time overhead of launching the process.
        interpreter1.interpret("1", context1);
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                try {
                    Assert.assertEquals(SUCCESS, interpreter1.interpret("100", context1).code());
                } catch (InterpreterException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            }
        };
        Thread thread2 = new Thread() {
            @Override
            public void run() {
                try {
                    Assert.assertEquals(SUCCESS, interpreter1.interpret("100", context1).code());
                } catch (InterpreterException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            }
        };
        long start = System.currentTimeMillis();
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        long end = System.currentTimeMillis();
        Assert.assertTrue(((end - start) >= 200));
    }

    @Test
    public void testParallelScheduler() throws InterruptedException, InterpreterException {
        interpreterSetting.getOption().setPerUser(SHARED);
        interpreterSetting.setProperty("zeppelin.SleepInterpreter.parallel", "true");
        final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
        final InterpreterContext context1 = createDummyInterpreterContext();
        // run this dummy interpret method first to launch the RemoteInterpreterProcess to avoid the
        // time overhead of launching the process.
        interpreter1.interpret("1", context1);
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                try {
                    Assert.assertEquals(SUCCESS, interpreter1.interpret("100", context1).code());
                } catch (InterpreterException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            }
        };
        Thread thread2 = new Thread() {
            @Override
            public void run() {
                try {
                    Assert.assertEquals(SUCCESS, interpreter1.interpret("100", context1).code());
                } catch (InterpreterException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            }
        };
        long start = System.currentTimeMillis();
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        long end = System.currentTimeMillis();
        Assert.assertTrue(((end - start) <= 200));
    }

    @Test
    public void testRemoteInterpreterSharesTheSameSchedulerInstanceInTheSameGroup() {
        interpreterSetting.getOption().setPerUser(SHARED);
        Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
        Interpreter interpreter2 = interpreterSetting.getInterpreter("user1", "note1", "echo");
        Assert.assertEquals(interpreter1.getInterpreterGroup(), interpreter2.getInterpreterGroup());
        Assert.assertEquals(interpreter1.getScheduler(), interpreter2.getScheduler());
    }

    @Test
    public void testMultiInterpreterSession() {
        interpreterSetting.getOption().setPerUser(SCOPED);
        Interpreter interpreter1_user1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
        Interpreter interpreter2_user1 = interpreterSetting.getInterpreter("user1", "note1", "echo");
        Assert.assertEquals(interpreter1_user1.getInterpreterGroup(), interpreter2_user1.getInterpreterGroup());
        Assert.assertEquals(interpreter1_user1.getScheduler(), interpreter2_user1.getScheduler());
        Interpreter interpreter1_user2 = interpreterSetting.getInterpreter("user2", "note1", "sleep");
        Interpreter interpreter2_user2 = interpreterSetting.getInterpreter("user2", "note1", "echo");
        Assert.assertEquals(interpreter1_user2.getInterpreterGroup(), interpreter2_user2.getInterpreterGroup());
        Assert.assertEquals(interpreter1_user2.getScheduler(), interpreter2_user2.getScheduler());
        // scheduler is shared in session but not across session
        Assert.assertNotEquals(interpreter1_user1.getScheduler(), interpreter1_user2.getScheduler());
    }

    @Test
    public void should_push_local_angular_repo_to_remote() throws Exception {
        final AngularObjectRegistry registry = new AngularObjectRegistry("spark", null);
        registry.add("name_1", "value_1", "note_1", "paragraphId_1");
        registry.add("name_2", "value_2", "node_2", "paragraphId_2");
        Interpreter interpreter = interpreterSetting.getInterpreter("user1", "note1", "angular_obj");
        interpreter.getInterpreterGroup().setAngularObjectRegistry(registry);
        final InterpreterContext context = createDummyInterpreterContext();
        InterpreterResult result = interpreter.interpret("dummy", context);
        Assert.assertEquals(SUCCESS, result.code());
        Assert.assertEquals("2", result.message().get(0).getData());
    }

    @Test
    public void testEnvStringPattern() {
        Assert.assertFalse(RemoteInterpreterUtils.isEnvString(null));
        Assert.assertFalse(RemoteInterpreterUtils.isEnvString(""));
        Assert.assertFalse(RemoteInterpreterUtils.isEnvString("abcDEF"));
        Assert.assertFalse(RemoteInterpreterUtils.isEnvString("ABC-DEF"));
        Assert.assertTrue(RemoteInterpreterUtils.isEnvString("ABCDEF"));
        Assert.assertTrue(RemoteInterpreterUtils.isEnvString("ABC_DEF"));
        Assert.assertTrue(RemoteInterpreterUtils.isEnvString("ABC_DEF123"));
    }

    @Test
    public void testEnvironmentAndProperty() throws InterpreterException {
        interpreterSetting.getOption().setPerUser(SHARED);
        interpreterSetting.setProperty("ENV_1", "VALUE_1");
        interpreterSetting.setProperty("property_1", "value_1");
        final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "get");
        final InterpreterContext context1 = createDummyInterpreterContext();
        Assert.assertEquals("VALUE_1", interpreter1.interpret("getEnv ENV_1", context1).message().get(0).getData());
        Assert.assertEquals("null", interpreter1.interpret("getEnv ENV_2", context1).message().get(0).getData());
        Assert.assertEquals("value_1", interpreter1.interpret("getProperty property_1", context1).message().get(0).getData());
        Assert.assertEquals("null", interpreter1.interpret("getProperty not_existed_property", context1).message().get(0).getData());
    }

    @Test
    public void testConvertDynamicForms() throws InterpreterException {
        GUI gui = new GUI();
        OptionInput[] paramOptions = new ParamOption[]{ new OptionInput.ParamOption("value1", "param1"), new OptionInput.ParamOption("value2", "param2") };
        List<Object> defaultValues = new ArrayList();
        defaultValues.add("default1");
        defaultValues.add("default2");
        gui.checkbox("checkbox_id", defaultValues, paramOptions);
        gui.select("select_id", "default", paramOptions);
        gui.textbox("textbox_id");
        Map<String, Input> expected = new java.util.LinkedHashMap(gui.getForms());
        Interpreter interpreter = interpreterSetting.getDefaultInterpreter("user1", "note1");
        InterpreterContext context = createDummyInterpreterContext();
        interpreter.interpret("text", context);
        Assert.assertArrayEquals(expected.values().toArray(), gui.getForms().values().toArray());
    }
}
