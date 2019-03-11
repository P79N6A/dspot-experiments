/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.tests.e2e.common;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


/**
 * E2E Request Scope Tests.
 *
 * @author Michal Gajdos
 */
@Ignore("Test Supplier Injection -> this test require dispose() method from Factory")
public class RequestScopeTest extends JerseyTest {
    public interface CloseMe {
        String eval();

        void close();
    }

    public static class CloseMeFactory implements DisposableSupplier<RequestScopeTest.CloseMe> {
        private static final CountDownLatch CLOSED_LATCH = new CountDownLatch(1);

        @Override
        public RequestScopeTest.CloseMe get() {
            return new RequestScopeTest.CloseMe() {
                @Override
                public String eval() {
                    return "foo";
                }

                @Override
                public void close() {
                    RequestScopeTest.CloseMeFactory.CLOSED_LATCH.countDown();
                }
            };
        }

        @Override
        public void dispose(final RequestScopeTest.CloseMe instance) {
            instance.close();
        }
    }

    @Path("remove")
    public static class RemoveResource {
        private RequestScopeTest.CloseMe closeMe;

        @Inject
        public RemoveResource(final RequestScopeTest.CloseMe closeMe) {
            this.closeMe = closeMe;
        }

        @GET
        public String get() {
            return closeMe.eval();
        }
    }

    /**
     * Test that Factory.dispose method is called during release of Request Scope.
     */
    @Test
    public void testRemove() throws Exception {
        final Response response = target().path("remove").request().get();
        MatcherAssert.assertThat(response.getStatus(), CoreMatchers.is(200));
        MatcherAssert.assertThat(response.readEntity(String.class), CoreMatchers.is("foo"));
        Assert.assertTrue(RequestScopeTest.CloseMeFactory.CLOSED_LATCH.await(3, TimeUnit.SECONDS));
    }
}
