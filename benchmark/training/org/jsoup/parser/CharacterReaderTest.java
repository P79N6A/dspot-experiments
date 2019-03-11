package org.jsoup.parser;


import CharacterReader.EOF;
import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Assert;
import org.junit.Test;

import static CharacterReader.maxBufferLen;


/**
 * Test suite for character reader.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class CharacterReaderTest {
    public static final int maxBufferLen = maxBufferLen;

    @Test
    public void consume() {
        CharacterReader r = new CharacterReader("one");
        Assert.assertEquals(0, r.pos());
        Assert.assertEquals('o', r.current());
        Assert.assertEquals('o', r.consume());
        Assert.assertEquals(1, r.pos());
        Assert.assertEquals('n', r.current());
        Assert.assertEquals(1, r.pos());
        Assert.assertEquals('n', r.consume());
        Assert.assertEquals('e', r.consume());
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(EOF, r.consume());
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(EOF, r.consume());
    }

    @Test
    public void unconsume() {
        CharacterReader r = new CharacterReader("one");
        Assert.assertEquals('o', r.consume());
        Assert.assertEquals('n', r.current());
        r.unconsume();
        Assert.assertEquals('o', r.current());
        Assert.assertEquals('o', r.consume());
        Assert.assertEquals('n', r.consume());
        Assert.assertEquals('e', r.consume());
        Assert.assertTrue(r.isEmpty());
        r.unconsume();
        Assert.assertFalse(r.isEmpty());
        Assert.assertEquals('e', r.current());
        Assert.assertEquals('e', r.consume());
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(EOF, r.consume());
        r.unconsume();
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(EOF, r.current());
    }

    @Test
    public void mark() {
        CharacterReader r = new CharacterReader("one");
        r.consume();
        r.mark();
        Assert.assertEquals('n', r.consume());
        Assert.assertEquals('e', r.consume());
        Assert.assertTrue(r.isEmpty());
        r.rewindToMark();
        Assert.assertEquals('n', r.consume());
    }

    @Test
    public void consumeToEnd() {
        String in = "one two three";
        CharacterReader r = new CharacterReader(in);
        String toEnd = r.consumeToEnd();
        Assert.assertEquals(in, toEnd);
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void nextIndexOfChar() {
        String in = "blah blah";
        CharacterReader r = new CharacterReader(in);
        Assert.assertEquals((-1), r.nextIndexOf('x'));
        Assert.assertEquals(3, r.nextIndexOf('h'));
        String pull = r.consumeTo('h');
        Assert.assertEquals("bla", pull);
        r.consume();
        Assert.assertEquals(2, r.nextIndexOf('l'));
        Assert.assertEquals(" blah", r.consumeToEnd());
        Assert.assertEquals((-1), r.nextIndexOf('x'));
    }

    @Test
    public void nextIndexOfString() {
        String in = "One Two something Two Three Four";
        CharacterReader r = new CharacterReader(in);
        Assert.assertEquals((-1), r.nextIndexOf("Foo"));
        Assert.assertEquals(4, r.nextIndexOf("Two"));
        Assert.assertEquals("One Two ", r.consumeTo("something"));
        Assert.assertEquals(10, r.nextIndexOf("Two"));
        Assert.assertEquals("something Two Three Four", r.consumeToEnd());
        Assert.assertEquals((-1), r.nextIndexOf("Two"));
    }

    @Test
    public void nextIndexOfUnmatched() {
        CharacterReader r = new CharacterReader("<[[one]]");
        Assert.assertEquals((-1), r.nextIndexOf("]]>"));
    }

    @Test
    public void consumeToChar() {
        CharacterReader r = new CharacterReader("One Two Three");
        Assert.assertEquals("One ", r.consumeTo('T'));
        Assert.assertEquals("", r.consumeTo('T'));// on Two

        Assert.assertEquals('T', r.consume());
        Assert.assertEquals("wo ", r.consumeTo('T'));
        Assert.assertEquals('T', r.consume());
        Assert.assertEquals("hree", r.consumeTo('T'));// consume to end

    }

    @Test
    public void consumeToString() {
        CharacterReader r = new CharacterReader("One Two Two Four");
        Assert.assertEquals("One ", r.consumeTo("Two"));
        Assert.assertEquals('T', r.consume());
        Assert.assertEquals("wo ", r.consumeTo("Two"));
        Assert.assertEquals('T', r.consume());
        Assert.assertEquals("wo Four", r.consumeTo("Qux"));
    }

    @Test
    public void advance() {
        CharacterReader r = new CharacterReader("One Two Three");
        Assert.assertEquals('O', r.consume());
        r.advance();
        Assert.assertEquals('e', r.consume());
    }

    @Test
    public void consumeToAny() {
        CharacterReader r = new CharacterReader("One &bar; qux");
        Assert.assertEquals("One ", r.consumeToAny('&', ';'));
        Assert.assertTrue(r.matches('&'));
        Assert.assertTrue(r.matches("&bar;"));
        Assert.assertEquals('&', r.consume());
        Assert.assertEquals("bar", r.consumeToAny('&', ';'));
        Assert.assertEquals(';', r.consume());
        Assert.assertEquals(" qux", r.consumeToAny('&', ';'));
    }

    @Test
    public void consumeLetterSequence() {
        CharacterReader r = new CharacterReader("One &bar; qux");
        Assert.assertEquals("One", r.consumeLetterSequence());
        Assert.assertEquals(" &", r.consumeTo("bar;"));
        Assert.assertEquals("bar", r.consumeLetterSequence());
        Assert.assertEquals("; qux", r.consumeToEnd());
    }

    @Test
    public void consumeLetterThenDigitSequence() {
        CharacterReader r = new CharacterReader("One12 Two &bar; qux");
        Assert.assertEquals("One12", r.consumeLetterThenDigitSequence());
        Assert.assertEquals(' ', r.consume());
        Assert.assertEquals("Two", r.consumeLetterThenDigitSequence());
        Assert.assertEquals(" &bar; qux", r.consumeToEnd());
    }

    @Test
    public void matches() {
        CharacterReader r = new CharacterReader("One Two Three");
        Assert.assertTrue(r.matches('O'));
        Assert.assertTrue(r.matches("One Two Three"));
        Assert.assertTrue(r.matches("One"));
        Assert.assertFalse(r.matches("one"));
        Assert.assertEquals('O', r.consume());
        Assert.assertFalse(r.matches("One"));
        Assert.assertTrue(r.matches("ne Two Three"));
        Assert.assertFalse(r.matches("ne Two Three Four"));
        Assert.assertEquals("ne Two Three", r.consumeToEnd());
        Assert.assertFalse(r.matches("ne"));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void matchesIgnoreCase() {
        CharacterReader r = new CharacterReader("One Two Three");
        Assert.assertTrue(r.matchesIgnoreCase("O"));
        Assert.assertTrue(r.matchesIgnoreCase("o"));
        Assert.assertTrue(r.matches('O'));
        Assert.assertFalse(r.matches('o'));
        Assert.assertTrue(r.matchesIgnoreCase("One Two Three"));
        Assert.assertTrue(r.matchesIgnoreCase("ONE two THREE"));
        Assert.assertTrue(r.matchesIgnoreCase("One"));
        Assert.assertTrue(r.matchesIgnoreCase("one"));
        Assert.assertEquals('O', r.consume());
        Assert.assertFalse(r.matchesIgnoreCase("One"));
        Assert.assertTrue(r.matchesIgnoreCase("NE Two Three"));
        Assert.assertFalse(r.matchesIgnoreCase("ne Two Three Four"));
        Assert.assertEquals("ne Two Three", r.consumeToEnd());
        Assert.assertFalse(r.matchesIgnoreCase("ne"));
    }

    @Test
    public void containsIgnoreCase() {
        CharacterReader r = new CharacterReader("One TWO three");
        Assert.assertTrue(r.containsIgnoreCase("two"));
        Assert.assertTrue(r.containsIgnoreCase("three"));
        // weird one: does not find one, because it scans for consistent case only
        Assert.assertFalse(r.containsIgnoreCase("one"));
    }

    @Test
    public void matchesAny() {
        char[] scan = new char[]{ ' ', '\n', '\t' };
        CharacterReader r = new CharacterReader("One\nTwo\tThree");
        Assert.assertFalse(r.matchesAny(scan));
        Assert.assertEquals("One", r.consumeToAny(scan));
        Assert.assertTrue(r.matchesAny(scan));
        Assert.assertEquals('\n', r.consume());
        Assert.assertFalse(r.matchesAny(scan));
    }

    @Test
    public void cachesStrings() {
        CharacterReader r = new CharacterReader("Check\tCheck\tCheck\tCHOKE\tA string that is longer than 16 chars");
        String one = r.consumeTo('\t');
        r.consume();
        String two = r.consumeTo('\t');
        r.consume();
        String three = r.consumeTo('\t');
        r.consume();
        String four = r.consumeTo('\t');
        r.consume();
        String five = r.consumeTo('\t');
        Assert.assertEquals("Check", one);
        Assert.assertEquals("Check", two);
        Assert.assertEquals("Check", three);
        Assert.assertEquals("CHOKE", four);
        Assert.assertTrue((one == two));
        Assert.assertTrue((two == three));
        Assert.assertTrue((three != four));
        Assert.assertTrue((four != five));
        Assert.assertEquals(five, "A string that is longer than 16 chars");
    }

    @Test
    public void rangeEquals() {
        CharacterReader r = new CharacterReader("Check\tCheck\tCheck\tCHOKE");
        Assert.assertTrue(r.rangeEquals(0, 5, "Check"));
        Assert.assertFalse(r.rangeEquals(0, 5, "CHOKE"));
        Assert.assertFalse(r.rangeEquals(0, 5, "Chec"));
        Assert.assertTrue(r.rangeEquals(6, 5, "Check"));
        Assert.assertFalse(r.rangeEquals(6, 5, "Chuck"));
        Assert.assertTrue(r.rangeEquals(12, 5, "Check"));
        Assert.assertFalse(r.rangeEquals(12, 5, "Cheeky"));
        Assert.assertTrue(r.rangeEquals(18, 5, "CHOKE"));
        Assert.assertFalse(r.rangeEquals(18, 5, "CHIKE"));
    }

    @Test
    public void empty() {
        CharacterReader r = new CharacterReader("One");
        Assert.assertTrue(r.matchConsume("One"));
        Assert.assertTrue(r.isEmpty());
        r = new CharacterReader("Two");
        String two = r.consumeToEnd();
        Assert.assertEquals("Two", two);
    }

    @Test
    public void consumeToNonexistentEndWhenAtAnd() {
        CharacterReader r = new CharacterReader("<!");
        Assert.assertTrue(r.matchConsume("<!"));
        Assert.assertTrue(r.isEmpty());
        String after = r.consumeTo('>');
        Assert.assertEquals("", after);
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void notEmptyAtBufferSplitPoint() {
        CharacterReader r = new CharacterReader(new StringReader("How about now"), 3);
        Assert.assertEquals("How", r.consumeTo(' '));
        Assert.assertFalse("Should not be empty", r.isEmpty());
        Assert.assertEquals(' ', r.consume());
        Assert.assertFalse(r.isEmpty());
    }

    @Test
    public void bufferUp() {
        String note = "HelloThere";// + ! = 11 chars

        int loopCount = 64;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < loopCount; i++) {
            sb.append(note);
            sb.append("!");
        }
        String s = sb.toString();
        BufferedReader br = new BufferedReader(new StringReader(s));
        CharacterReader r = new CharacterReader(br);
        for (int i = 0; i < loopCount; i++) {
            String pull = r.consumeTo('!');
            Assert.assertEquals(note, pull);
            Assert.assertEquals('!', r.current());
            r.advance();
        }
        Assert.assertTrue(r.isEmpty());
    }
}
