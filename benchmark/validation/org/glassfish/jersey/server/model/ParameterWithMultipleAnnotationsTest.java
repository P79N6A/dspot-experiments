/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.server.model;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.PathParam;
import org.junit.Test;


/**
 * Checks that Parameters work fine with multiple annotations.
 */
public class ParameterWithMultipleAnnotationsTest {
    @Test
    public void testParametersWithMultiple() throws Exception {
        checkMyResourceMethod("processTrailingUnknown");
        checkMyResourceMethod("processLeadingUnknown");
        checkMyResourceMethod("processLeadingAndTrailingUnknown");
        checkMyResourceMethod("processSingleUnknown");
        checkMyResourceMethod("processDoubleUnknown");
    }

    @Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LeadAnnotation {
        String value() default "lead";
    }

    @Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TrailAnnotation {
        String value() default "trail";
    }

    private static class MyResource {
        public void processTrailingUnknown(@PathParam("correct")
        @ParameterWithMultipleAnnotationsTest.TrailAnnotation
        String id) {
        }

        public void processLeadingUnknown(@ParameterWithMultipleAnnotationsTest.LeadAnnotation
        @PathParam("correct")
        String id) {
        }

        public void processLeadingAndTrailingUnknown(@ParameterWithMultipleAnnotationsTest.LeadAnnotation
        @PathParam("correct")
        @ParameterWithMultipleAnnotationsTest.TrailAnnotation
        String id) {
        }

        public void processSingleUnknown(@ParameterWithMultipleAnnotationsTest.LeadAnnotation("correct")
        String id) {
        }

        public void processDoubleUnknown(@ParameterWithMultipleAnnotationsTest.LeadAnnotation
        @ParameterWithMultipleAnnotationsTest.TrailAnnotation("correct")
        String id) {
        }
    }
}
