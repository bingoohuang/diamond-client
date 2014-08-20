package org.n3r.diamond.client;


import org.junit.Test;
import org.n3r.diamond.client.cache.Spec;
import org.n3r.diamond.client.cache.SpecParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SpecParserTest {
    @Test
    public void testHjy() {
        Spec[] specs = SpecParser.parseSpecs("@org.lurker.diamond.MyUpdater(\"ABCD\") @Version(123)");
        System.out.println(specs);
    }

    @Test
    public void test1() {
        Spec paramsDef = SpecParser.parseSpec(" @Direct");
        assertEquals("Direct", paramsDef.getName());
        assertEquals(0, paramsDef.getParams().length);

        paramsDef = SpecParser.parseSpec("\t\r\n ");
        assertNull(paramsDef);

        paramsDef = SpecParser.parseSpec("");
        assertNull(paramsDef);

        paramsDef = SpecParser.parseSpec("@Direct ");
        assertEquals("Direct", paramsDef.getName());
        assertEquals(0, paramsDef.getParams().length);

        Spec[] paramsDefs = SpecParser.parseSpecs("@Direct @Dir");
        assertEquals(2, paramsDefs.length);
        assertEquals("Direct", paramsDefs[0].getName());
        assertEquals(0, paramsDefs[0].getParams().length);
        assertEquals("Dir", paramsDefs[1].getName());
        assertEquals(0, paramsDefs[0].getParams().length);

        paramsDefs = SpecParser.parseSpecs("@Direct  @Dir");
        assertEquals(2, paramsDefs.length);
        assertEquals("Direct", paramsDefs[0].getName());
        assertEquals(0, paramsDefs[0].getParams().length);
        assertEquals("Dir", paramsDefs[1].getName());
        assertEquals(0, paramsDefs[0].getParams().length);

        paramsDefs = SpecParser.parseSpecs("@Direct@Dir");
        assertEquals(2, paramsDefs.length);
        assertEquals("Direct", paramsDefs[0].getName());
        assertEquals(0, paramsDefs[0].getParams().length);
        assertEquals("Dir", paramsDefs[1].getName());
        assertEquals(0, paramsDefs[0].getParams().length);
    }

    @Test
    public void test2() {
        Spec paramsDef = SpecParser.parseSpec("@Direct()");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("", params[0]);
    }

    @Test(expected = RuntimeException.class)
    public void test3() {
        SpecParser.parseSpec("@Direct(");
    }

    @Test
    public void test4() {
        Spec paramsDef = SpecParser.parseSpec("@Direct( a  ,b)");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals("a", params[0]);
        assertEquals("b", params[1]);
    }

    @Test
    public void test40() {
        Spec paramsDef = SpecParser.parseSpec("@Direct( \"a\"\t , \"b\" )");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals("a", params[0]);
        assertEquals("b", params[1]);
    }

    @Test(expected = RuntimeException.class)
    public void test41() {
        SpecParser.parseSpec("@Direct( a , a\"b\" )");
    }

    @Test
    public void test5() {
        Spec paramsDef = SpecParser.parseSpec("@Direct(\" a  \", b )");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(" a  ", params[0]);
        assertEquals("b", params[1]);
    }

    @Test
    public void test6() {
        Spec paramsDef = SpecParser.parseSpec("@Direct(a\\,b)");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("a,b", params[0]);

        paramsDef = SpecParser.parseSpec("@Direct(\\(a\\,b\\))");
        assertEquals("Direct", paramsDef.getName());
        params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("(a,b)", params[0]);
    }

    @Test
    public void test7() {
        Spec paramsDef = SpecParser.parseSpec("@Direct(\"a,b\")");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("a,b", params[0]);
    }

    @Test
    public void test8() {
        Spec paramsDef = SpecParser.parseSpec("@Direct(\"@a,)(b\")");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("@a,)(b", params[0]);
    }

    @Test
    public void test9() {
        Spec paramsDef = SpecParser.parseSpec("@Direct(\"  @a,)(b\\\"  \")");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("  @a,)(b\"  ", params[0]);
    }

    @Test
    public void test10() {
        Spec paramsDef = SpecParser.parseSpec("@Direct(\"a\\r\\n\\tb\")");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("a\r\n\tb", params[0]);
    }

    @Test
    public void test101() {
        Spec paramsDef = SpecParser.parseSpec("@Direct  (  )");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("", params[0]);
    }

    @Test
    public void test102() {
        Spec paramsDef = SpecParser.parseSpec("@Direct(\"\")");
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("", params[0]);
    }

    @Test
    public void test11() {
        Spec[] specs = SpecParser.parseSpecs("@Direct @Dir @Direct1(  a  ,b) @Direct2(a\\,b) " +
                "@Direct3(\"a,b\") @Direct4(\"@a,)(b\") @Direct5(\"  \\\"@a,)(b  \")");
        assertEquals(7, specs.length);

        Spec paramsDef = specs[0];
        assertEquals("Direct", paramsDef.getName());
        String[] params = paramsDef.getParams();
        assertEquals(0, params.length);

        paramsDef = specs[1];
        assertEquals("Dir", paramsDef.getName());
        params = paramsDef.getParams();
        assertEquals(0, params.length);

        paramsDef = specs[2];
        assertEquals("Direct1", paramsDef.getName());
        params = paramsDef.getParams();
        assertEquals(2, params.length);
        assertEquals("a", params[0]);
        assertEquals("b", params[1]);

        paramsDef = specs[3];
        assertEquals("Direct2", paramsDef.getName());
        params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("a,b", params[0]);

        paramsDef = specs[4];
        assertEquals("Direct3", paramsDef.getName());
        params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("a,b", params[0]);

        paramsDef = specs[5];
        assertEquals("Direct4", paramsDef.getName());
        params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("@a,)(b", params[0]);

        paramsDef = specs[6];
        assertEquals("Direct5", paramsDef.getName());
        params = paramsDef.getParams();
        assertEquals(1, params.length);
        assertEquals("  \"@a,)(b  ", params[0]);
    }

    @Test(expected = RuntimeException.class)
    public void test12() {
        SpecParser.parseSpecs("Direct");
    }

    @Test(expected = RuntimeException.class)
    public void test13() {
        SpecParser.parseSpecs("@123Direct");
    }

    @Test(expected = RuntimeException.class)
    public void test14() {
        SpecParser.parseSpecs("@Direct(");
    }

    @Test(expected = RuntimeException.class)
    public void test15() {
        SpecParser.parseSpecs("@Direct(\"a,b)");
    }

    @Test(expected = RuntimeException.class)
    public void test16() {
        SpecParser.parseSpecs("@Direct(\"a,b\"\")");
    }

    @Test(expected = RuntimeException.class)
    public void test17() {
        SpecParser.parseSpecs("@");
    }

    @Test(expected = RuntimeException.class)
    public void test18() {
        SpecParser.parseSpecs("@Direct@");
    }

    @Test(expected = RuntimeException.class)
    public void test19() {
        SpecParser.parseSpec("@Die rt");
    }

    @Test(expected = RuntimeException.class)
    public void test20() {
        SpecParser.parseSpec("@Direct*");
    }

    @Test(expected = RuntimeException.class)
    public void test21() {
        SpecParser.parseSpec("@Direct)*");
    }

    @Test(expected = RuntimeException.class)
    public void test22() {
        SpecParser.parseSpec("@ a@Direct");
    }

    @Test(expected = RuntimeException.class)
    public void test23() {
        SpecParser.parseSpec("@Direct(\"a,b");
    }

    @Test(expected = RuntimeException.class)
    public void test24() {
        SpecParser.parseSpec("Direct(\"a,b");
    }

    @Test(expected = RuntimeException.class)
    public void test211() {
        SpecParser.parseSpec("Direct(bc\"dd\")");
    }
}
