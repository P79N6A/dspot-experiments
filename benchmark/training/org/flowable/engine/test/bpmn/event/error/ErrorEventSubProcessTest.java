/**
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
package org.flowable.engine.test.bpmn.event.error;


import java.util.HashMap;
import java.util.Map;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;


/**
 *
 *
 * @author Tijs Rademakers
 */
public class ErrorEventSubProcessTest extends PluggableFlowableTestCase {
    private static final String STANDALONE_SUBPROCESS_FLAG_VARIABLE_NAME = "standalone";

    private static final String LOCAL_ERROR_FLAG_VARIABLE_NAME = "localError";

    private static final String PROCESS_KEY_UNDER_TEST = "helloWorldWithBothSubProcessTypes";

    // an event subprocesses takes precedence over a boundary event
    @Test
    @Deployment
    public void testEventSubprocessTakesPrecedence() {
        String procId = runtimeService.startProcessInstanceByKey("CatchErrorInEmbeddedSubProcess").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    // an event subprocess with errorCode takes precedence over a catch-all handler
    @Test
    @Deployment
    public void testErrorCodeTakesPrecedence() {
        String procId = runtimeService.startProcessInstanceByKey("CatchErrorInEmbeddedSubProcess").getId();
        // The process will throw an error event, which is caught and escalated by a User org.flowable.task.service.Task
        assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("taskAfterErrorCatch2").count());
        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("Escalated Task", task.getName());
        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }

    @Test
    @Deployment
    public void testCatchErrorInEmbeddedSubProcess() {
        String procId = runtimeService.startProcessInstanceByKey("CatchErrorInEmbeddedSubProcess").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    @Test
    @Deployment
    public void testCatchErrorThrownByScriptTaskInEmbeddedSubProcess() {
        String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInEmbeddedSubProcess").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    @Test
    @Deployment
    public void testCatchErrorThrownByScriptTaskInEmbeddedSubProcessWithErrorCode() {
        String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInEmbeddedSubProcessWithErrorCode").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    @Test
    @Deployment
    public void testCatchErrorThrownByScriptTaskInTopLevelProcess() {
        String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInTopLevelProcess").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    @Test
    @Deployment
    public void testCatchErrorThrownByScriptTaskInsideSubProcessInTopLevelProcess() {
        String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInsideSubProcessInTopLevelProcess").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    @Test
    @Deployment(resources = { "org/flowable/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testThrowErrorInScriptTaskInsideCallActivitiCatchInTopLevelProcess.bpmn20.xml", "org/flowable/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml" })
    public void testThrowErrorInScriptTaskInsideCallActivitiCatchInTopLevelProcess() {
        String procId = runtimeService.startProcessInstanceByKey("testThrowErrorInScriptTaskInsideCallActivitiCatchInTopLevelProcess").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    @Test
    @Deployment(resources = { "org/flowable/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchMultipleRethrowParent.bpmn", "org/flowable/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchMultipleRethrowSubProcess.bpmn" })
    public void testMultipleRethrowEvents() {
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put(ErrorEventSubProcessTest.LOCAL_ERROR_FLAG_VARIABLE_NAME, true);
        variableMap.put(ErrorEventSubProcessTest.STANDALONE_SUBPROCESS_FLAG_VARIABLE_NAME, true);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ErrorEventSubProcessTest.PROCESS_KEY_UNDER_TEST, variableMap);
        assertNotNull(processInstance.getId());
    }
}
