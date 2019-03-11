/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2019 the original author or authors.
 */
package org.assertj.core.util.introspection;


import org.assertj.core.api.Assertions;
import org.assertj.core.test.Person;
import org.assertj.core.util.introspection.beans.SuperHero;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link MethodSupport#methodResultFor(Object, String)}</code>.
 *
 * @author Micha? Piotrkowski
 */
public class MethodSupport_methodResultFor_Test {
    private Person bruceWayne;

    private Person joker;

    private SuperHero batman;

    @Test
    public void should_invoke_methods_without_arguments() {
        Object result = MethodSupport.methodResultFor(batman, "archenemy");
        Assertions.assertThat(result).isEqualTo(joker);
    }

    @Test
    public void should_invoke_methods_from_superclass() {
        Object result = MethodSupport.methodResultFor(batman, "getName");
        Assertions.assertThat(result).isEqualTo("Batman");
    }

    @Test
    public void should_fail_meaningfully_if_object_instance_not_provided() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> MethodSupport.methodResultFor(null, "methodName")).withMessage("Object instance can not be null!");
    }

    @Test
    public void should_fail_meaningfully_if_method_name_not_provided() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> MethodSupport.methodResultFor(batman, null)).withMessage("Method name can not be empty!");
    }

    @Test
    public void should_fail_meaningfully_if_method_name_is_empty() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> MethodSupport.methodResultFor(batman, "")).withMessage("Method name can not be empty!");
    }

    @Test
    public void should_fail_meaningfully_if_method_not_found() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> MethodSupport.methodResultFor(batman, "commitCrime")).withMessage(("Can't find method 'commitCrime' in class SuperHero.class. Make sure public" + " method exists and accepts no arguments!"));
    }

    @Test
    public void should_fail_meaningfully_if_method_does_not_return_value() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> MethodSupport.methodResultFor(batman, "saveTheDay")).withMessage("Method 'saveTheDay' in class SuperHero.class has to return a value!");
    }

    @Test
    public void should_fail_meaningfully_if_method_is_not_public() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> MethodSupport.methodResultFor(batman, "trueIdentity")).withMessage(("Can't find method 'trueIdentity' in class SuperHero.class. Make sure " + "public method exists and accepts no arguments!"));
    }
}
