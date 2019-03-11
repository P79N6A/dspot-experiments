/**
 * The MIT License
 *
 * Copyright 2014 Jesse Glick.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.cli;


import Item.CREATE;
import Jenkins.READ;
import hudson.model.User;
import java.io.ByteArrayInputStream;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.MockFolder;

import static Matcher.failedWith;
import static Matcher.succeededSilently;


public class CreateJobCommandTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Issue("JENKINS-22262")
    @Test
    public void folderPermissions() throws Exception {
        CLICommand cmd = new CreateJobCommand();
        CLICommandInvoker invoker = new CLICommandInvoker(r, cmd);
        final MockFolder d = r.createFolder("d");
        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());
        r.jenkins.setAuthorizationStrategy(// including alice
        new MockAuthorizationStrategy().grant(READ).everywhere().toAuthenticated().grant(Item.READ).onItems(d).toAuthenticated().grant(CREATE).onItems(d).to("bob"));
        cmd.setTransportAuth(User.get("alice").impersonate());
        MatcherAssert.assertThat(invoker.withStdin(new ByteArrayInputStream("<project/>".getBytes("US-ASCII"))).invokeWithArgs("d/p"), failedWith(6));
        cmd.setTransportAuth(User.get("bob").impersonate());
        MatcherAssert.assertThat(invoker.withStdin(new ByteArrayInputStream("<project/>".getBytes("US-ASCII"))).invokeWithArgs("d/p"), succeededSilently());
        Assert.assertNotNull(d.getItem("p"));
    }
}
