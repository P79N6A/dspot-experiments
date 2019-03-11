package water.parser;


import org.junit.Assert;
import org.junit.Test;
import water.Key;
import water.TestUtil;
import water.fvec.Frame;
import water.fvec.Vec;
import water.util.StringUtils;

import static ParseSetup.HAS_HEADER;
import static ParseSetup.NO_HEADER;


public class ParserTest2 extends TestUtil {
    private final char[] SEPARATORS = new char[]{ ',', ' ' };

    @Test
    public void testNAs() {
        String[] data = new String[]{ "\'C1Chunk\',C1SChunk, \'C2Chunk\', \'C2SChunk\',  \'C4Chunk\',  \'C4FChunk\',  \'C8Chunk\',  \'C8DChunk\',   \'Categorical\'\n" + "0,       0.0,          0,           0,           0,          0 ,          0,   8.878979,           A \n", "1,       0.1,          1,         0.1,           1,          1 ,          1,   1.985934,           B \n", "2,       0.2,          2,         0.2,           2,          2 ,          2,   3.398018,           C \n", "3,       0.3,          3,         0.3,           3,          3 ,          3,   9.329589,           D \n", "4,       0.4,          4,           4,           4,          4 , 2147483649,   0.290184,           A \n", "0,       0.5,          0,           0,     -100000,    1.234e2 ,-2147483650,   1e-30,              B \n", "254,    0.25,       2550,      6553.4,      100000,    2.345e-2,          0,    1e30,              C \n", " ,          ,           ,            ,            ,            ,           ,        ,                \n", "?,        NA,          ?,           ?,           ?,           ?,          ?,       ?,                \n" };
        Key rkey = ParserTest.makeByteVec(data);
        ParseSetup ps = new ParseSetup(DefaultParserProviders.CSV_INFO, ((byte) (',')), false, HAS_HEADER, 9, new String[]{ "'C1Chunk'", "C1SChunk", "'C2Chunk'", "'C2SChunk'", "'C4Chunk'", "'C4FChunk'", "'C8Chunk'", "'C8DChunk'", "'Categorical'" }, ParseSetup.strToColumnTypes(new String[]{ "Numeric", "Numeric", "Numeric", "Numeric", "Numeric", "Numeric", "Numeric", "Numeric", "Enum" }), null, null, null);
        Frame fr = ParseDataset.parse(Key.make("na_test.hex"), new Key[]{ rkey }, true, ps);
        int nlines = ((int) (fr.numRows()));
        Assert.assertEquals(9, nlines);
        Assert.assertEquals(9, fr.numCols());
        for (int i = 0; i < (nlines - 2); ++i)
            for (Vec v : fr.vecs())
                Assert.assertTrue(((("error at line " + i) + ", vec ") + (v.chunkForChunkIdx(0).getClass().getSimpleName())), ((!(Double.isNaN(v.at(i)))) && (!(v.isNA(i)))));


        for (int j = 0; j < (fr.vecs().length); j++) {
            Vec v = fr.vecs()[j];
            for (int i = nlines - 2; i < nlines; ++i)
                Assert.assertTrue(((((((i + ", ") + j) + ":") + (v.at(i))) + ", ") + (v.isNA(i))), ((Double.isNaN(v.at(i))) && (v.isNA(i))));

        }
        fr.delete();
    }

    /**
     * there's no official grammar for CSV, and especially no directive on how to handle blank lines.
     * but common parsers (e.g. Python CSV parser) ignores them.
     * Useful to consider anyway, especially for trailing lines
     */
    @Test
    public void testIgnoreBlankLines() {
        // the data chunks are intentionally cut at some various edge cases (cut before data not currently supported)
        String[] data = new String[]{ "'C1', 'C2', 'C3', " + ((" \'C4\'\n" + // chunk1&2
        " \t\n") + "1,       1,"), "         1,        1\n"// chunk3
         + "2,       2,         ", "2,        2\n"// chunk4
         + "3,       3,         3", ",        3\n"// chunk5
         + " ,        ,          ,      ", "   \n"// chunk6
         + ("\n" + "  \n"), " \n" + // chunk7
        ((((("\t\n" + "  ") + "  \n") + // chunk8
        "\t\t\t\n") + " \t") + "\n") };
        Key dataKey = ParserTest.makeByteVec(data);
        ParseSetup ps = new ParseSetup(DefaultParserProviders.CSV_INFO, ((byte) (',')), false, HAS_HEADER, 4, new String[]{ "'C1'", "'C2'", "'C3'", "'C4'" }, ParseSetup.strToColumnTypes(new String[]{ "Numeric", "Numeric", "Numeric", "Numeric" }), null, null, null);
        Frame fr = null;
        try {
            fr = ParseDataset.parse(Key.make("blank_lines_test.hex"), new Key[]{ dataKey }, true, ps);
            Assert.assertEquals(4, fr.numRows());
            Assert.assertTrue(fr.hasNAs());
            Assert.assertEquals(4, fr.naCount());
            for (int i = 0; i < 4; i++) {
                // only last=4th row contains NAs
                Assert.assertTrue(fr.vec(i).isNA(3));
            }
        } finally {
            if (fr != null)
                fr.delete();

        }
    }

    @Test
    public void testSingleQuotes() {
        String[] data = new String[]{ "\'Tomass,test,first,line\'\n\'Tomas\'\'s,test2\',test2\nlast,\'line\'\'", "s, trailing, piece'" };
        String[][] expectFalse = new String[][]{ TestUtil.ar("'Tomass", "test", "first", "line'"), TestUtil.ar("'Tomas''s", "test2'", "test2", null), TestUtil.ar("last", "'line''s", "trailing", "piece'") };
        Key k = ParserTest.makeByteVec(data);
        ParseSetup gSetupF = ParseSetup.guessSetup(null, StringUtils.bytesOf(data[0]), /* single quote */
        new ParseSetup(DefaultParserProviders.CSV_INFO, ((byte) (',')), false, 4, NO_HEADER, null, null));
        gSetupF._column_types = ParseSetup.strToColumnTypes(new String[]{ "Enum", "Enum", "Enum", "Enum" });
        Frame frF = ParseDataset.parse(Key.make(), new Key[]{ k }, false, gSetupF);
        ParserTest2.testParsed(frF, expectFalse);
        String[][] expectTrue = new String[][]{ TestUtil.ar("Tomass,test,first,line", null), TestUtil.ar("Tomas''stest2", "test2"), TestUtil.ar("last", "lines trailing piece") };
        ParseSetup gSetupT = ParseSetup.guessSetup(null, StringUtils.bytesOf(data[0]), /* single quote */
        new ParseSetup(DefaultParserProviders.CSV_INFO, ((byte) (',')), true, 2, NO_HEADER, null, null));
        gSetupT._column_types = ParseSetup.strToColumnTypes(new String[]{ "Enum", "Enum", "Enum", "Enum" });
        Frame frT = ParseDataset.parse(Key.make(), new Key[]{ k }, true, gSetupT);
        // testParsed(frT,expectTrue);  // not currently passing
        frT.delete();
    }

    @Test
    public void testSingleQuotes2() {
        Frame fr = TestUtil.parse_test_file("smalldata/junit/test_quote.csv");
        Assert.assertEquals(fr.numCols(), 11);
        Assert.assertEquals(fr.numRows(), 7);
        fr.delete();
    }

    @Test
    public void testDoubleQuotes() {
        Frame fr = null;
        try {
            String[] data = new String[]{ "Tomass,test,\"Feline says \"\"meh\"\".\",line\nTomass,test2,second,line\nTomass,test3,last,line" };
            Key k = ParserTest.makeByteVec(data);
            ParseSetup gSetupF = ParseSetup.guessSetup(null, StringUtils.bytesOf(data[0]), /* single quote */
            new ParseSetup(DefaultParserProviders.CSV_INFO, ((byte) (',')), false, NO_HEADER, 4, null, null));
            gSetupF._column_types = ParseSetup.strToColumnTypes(new String[]{ "String", "String", "String", "String" });
            fr = ParseDataset.parse(Key.make(), new Key[]{ k }, true, gSetupF);
            BufferedString str = new BufferedString();
            Vec[] vecs = fr.vecs();
            Assert.assertEquals(fr.numCols(), 4);
            Assert.assertEquals(fr.numRows(), 3);
            Assert.assertEquals("Feline says \"meh\".", vecs[2].atStr(str, 0).toString());
            fr.delete();
        } finally {
            if (fr != null)
                fr.delete();

        }
    }

    // Test very sparse data
    @Test
    public void testSparse() {
        // Build 100 zero's and 1 one.
        double[][] exp = new double[101][1];
        exp[50][0] = 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++)
            sb.append("0.0\n");

        sb.append("1.0\n");
        for (int i = 0; i < 50; i++)
            sb.append("0.0\n");

        Key k = ParserTest.makeByteVec(sb.toString());
        ParserTest.testParsed(ParseDataset.parse(Key.make(), k), exp, 101);
        // Build 100 zero's and 1 non-zero.
        exp = new double[101][1];
        exp[50][0] = 2;
        sb = new StringBuilder();
        for (int i = 0; i < 50; i++)
            sb.append("0\n");

        sb.append("2\n");
        for (int i = 0; i < 50; i++)
            sb.append("0\n");

        k = ParserTest.makeByteVec(sb.toString());
        ParserTest.testParsed(ParseDataset.parse(Key.make(), k), exp, 101);
        // Build 100 zero's and some non-zeros.  Last line is truncated.
        for (char sep : SEPARATORS) {
            exp = new double[101][2];
            exp[50][0] = 2;
            exp[50][1] = 3;
            exp[100][0] = 0;
            // Truncated final line
            exp[100][1] = Double.NaN;
            sb = new StringBuilder();
            for (int i = 0; i < 50; i++)
                sb.append("0").append(sep).append("0\n");

            sb.append("2").append(sep).append("3\n");
            for (int i = 0; i < 49; i++)
                sb.append("0").append(sep).append("0\n");

            sb.append("0");
            // Truncated final line
            k = ParserTest.makeByteVec(sb.toString());
            ParserTest.testParsed(ParseDataset.parse(Key.make(), k), exp, 101);
        }
        // Build 100000 zero's and some one's
        sb = new StringBuilder();
        exp = new double[100100][1];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 1000; j++)
                sb.append("0\n");

            sb.append("1\n");
            exp[((i * 1001) + 1000)][0] = 1;
        }
        k = ParserTest.makeByteVec(sb.toString());
        ParserTest.testParsed(ParseDataset.parse(Key.make(), k), exp, 100100);
        // Build 100 zero's, then 100 mix of -1001 & 1001's (to force a
        // sparse-short, that finally inflates to a full dense-short).
        sb = new StringBuilder();
        for (int i = 0; i < 100; i++)
            sb.append("0\n");

        for (int i = 0; i < 100; i += 2)
            sb.append("-1001\n1001\n");

        exp = new double[200][1];
        for (int i = 0; i < 100; i += 2) {
            exp[(i + 100)][0] = -1001;
            exp[(i + 101)][0] = 1001;
        }
        k = ParserTest.makeByteVec(sb.toString());
        ParserTest.testParsed(ParseDataset.parse(Key.make(), k), exp, 200);
    }

    // test correctnes of sparse chunks
    // added after failing to encode properly following data as
    // 0s were not considered when computing compression strategy and then
    // lemin was 6108 and there was Short overflow when encoding zeros.
    // So, the first column was compressed into C2SChunk with 0s causing short overflow,
    @Test
    public void testSparse2() {
        String data = "0,     0,0,0,0,0\n" + ((((((((((((((((((((((((((((((("0,     0,0,0,0,0\n" + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "35351, 0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "6108,  0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "35351, 0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "6334,  0,0,0,0,0\n") + "0,     0,0,0,0,0\n") + "0,     0,0,0,0,0\n");
        double[][] exp = new double[][]{ TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(35351, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(6108, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(35351, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(6334, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0), TestUtil.ard(0, 0, 0, 0, 0, 0) };
        Key k = ParserTest.makeByteVec(data);
        ParserTest.testParsed(ParseDataset.parse(Key.make(), k), exp, 33);
    }
}
