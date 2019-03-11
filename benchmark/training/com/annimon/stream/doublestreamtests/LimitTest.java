package com.annimon.stream.doublestreamtests;


import com.annimon.stream.DoubleStream;
import org.hamcrest.Matchers;
import org.junit.Test;


public final class LimitTest {
    @Test
    public void testLimit() {
        DoubleStream.of(0.1, 0.2, 0.3).limit(2).custom(assertElements(Matchers.arrayContaining(0.1, 0.2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitNegative() {
        DoubleStream.of(0.1, 2.345).limit((-2)).count();
    }

    @Test
    public void testLimitZero() {
        DoubleStream.of(0.1, 0.2, 0.3).limit(0).custom(assertIsEmpty());
    }

    @Test
    public void testLimitMoreThanCount() {
        DoubleStream.of(0.1, 0.2, 0.3).limit(5).custom(assertElements(Matchers.arrayContaining(0.1, 0.2, 0.3)));
    }
}
