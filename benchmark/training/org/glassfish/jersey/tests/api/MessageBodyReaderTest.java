/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.tests.api;


import HttpHeaders.CONTENT_TYPE;
import MediaType.APPLICATION_OCTET_STREAM_TYPE;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Jan Supol (jan.supol at oracle.com)
 * @author Michal Gajdos
 */
public class MessageBodyReaderTest extends JerseyTest {
    @Path("resource")
    public static class Resource {
        @Context
        private HttpHeaders headers;

        @POST
        @Path("plain")
        public String plain(final MessageBodyReaderTest.EntityForReader entity) {
            return ((entity.getValue()) + ";") + (headers.getHeaderString(CONTENT_TYPE));
        }
    }

    @Provider
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public static class AppOctetReader implements MessageBodyReader<MessageBodyReaderTest.EntityForReader> {
        @Override
        public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
            return APPLICATION_OCTET_STREAM_TYPE.equals(mediaType);
        }

        @Override
        public MessageBodyReaderTest.EntityForReader readFrom(final Class<MessageBodyReaderTest.EntityForReader> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream) throws IOException, WebApplicationException {
            // Underlying stream should not be closed and Jersey is preventing from closing it.
            entityStream.close();
            return new MessageBodyReaderTest.EntityForReader(ReaderWriter.readFromAsString(entityStream, mediaType));
        }
    }

    public static class EntityForReader {
        private String value;

        public EntityForReader(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Test whether the default {@link MediaType} ({@value MediaType#APPLICATION_OCTET_STREAM}) is passed to a reader if no
     * {@value HttpHeaders#CONTENT_TYPE} value is provided in a request.
     */
    @Test
    public void testDefaultContentTypeForReader() throws Exception {
        final HttpPost httpPost = new HttpPost(UriBuilder.fromUri(getBaseUri()).path("resource/plain").build());
        httpPost.setEntity(new ByteArrayEntity("value".getBytes()));
        httpPost.removeHeaders("Content-Type");
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpResponse response = httpClient.execute(httpPost);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertEquals("value;null", ReaderWriter.readFromAsString(response.getEntity().getContent(), null));
    }
}
