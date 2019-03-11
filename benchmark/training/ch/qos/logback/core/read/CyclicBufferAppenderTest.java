/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.read;


import org.junit.Assert;
import org.junit.Test;


public class CyclicBufferAppenderTest {
    private CyclicBufferAppender<String> cyclicBufferAppender;

    @Test
    public void reset() {
        cyclicBufferAppender.append("foobar");
        Assert.assertEquals(1, cyclicBufferAppender.getLength());
        cyclicBufferAppender.reset();
        Assert.assertEquals(0, cyclicBufferAppender.getLength());
    }

    @Test
    public void genericGet() {
        cyclicBufferAppender.append("Some string");
        // get() now has type information, assigning to String should work without cast.
        String foo = cyclicBufferAppender.get(0);
        Assert.assertEquals("Some string", foo);
    }
}
