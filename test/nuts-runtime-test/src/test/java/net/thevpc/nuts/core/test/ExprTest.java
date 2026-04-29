/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NStreamTokenizer;
import org.junit.jupiter.api.*;

import java.io.StringReader;

/**
 * @author thevpc
 */
public class ExprTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    private boolean accept(NExprOperator d, String pattern) {
        NExprOpType f;
        String n;
        if (pattern.startsWith("prefix:")) {
            f = NExprOpType.PREFIX;
            n = pattern.substring("prefix:".length());
        } else if (pattern.startsWith("postfix:")) {
            f = NExprOpType.POSTFIX;
            n = pattern.substring("postfix:".length());
        } else if (pattern.startsWith("infix:")) {
            f = NExprOpType.INFIX;
            n = pattern.substring("infix:".length());
        } else {
            return false;
        }
        return f.equals(d.operatorType()) && n.equals(d.name());

    }

    private boolean accept(NExprOperator d, String... patterns) {
        for (String pattern : patterns) {
            if (accept(d, pattern)) {
                return true;
            }
        }
        return false;
    }

    private void _retain(NExprContext expr, String... patterns) {
        if (expr instanceof NExprMutableContext) {
            NExprMutableContext d = (NExprMutableContext) expr;
            for (NExprOperator operator : d.getOperators()) {
                if (!accept(operator, patterns)) {
                    d.removeOperator(operator);
                }
            }
        }
    }

    @Test
    public void test1() throws Exception {
        NExprContext expr = _declareDefault();
        _retain(expr, "infix:+");
        NExprNode n = expr.parse("1+2+3").get();
        TestUtils.println(n);
        Assertions.assertEquals("1 + 2 + 3", n.toString());
    }

    private static NExprContext _declareDefault() {
        return NExprContextBuilder.of()
                .declareBuiltins()
                .build();
    }

    @Test
    public void test2() throws Exception {
        NExprContext expr = _declareDefault();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("1+2*3").get();
        TestUtils.println(n);
        Assertions.assertEquals("1 + 2 * 3", n.toString());
    }

    @Test
    public void test3() throws Exception {
        NExprContext expr = _declareDefault();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("a").get();
        Assertions.assertEquals(NExprNodeType.WORD, n.nodeType());
        TestUtils.println(n);
    }

    @Test
    public void test4() throws Exception {
        NExprContext expr = _declareDefault();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("(a&b)").get();
        n.toString();
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.nodeType());
        Assertions.assertEquals("(", n.name());
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.children().get(0).nodeType());
        Assertions.assertEquals("&", n.children().get(0).name());
        TestUtils.println(n);
    }


    @Test
    public void test5() throws Exception {
        NExprContext expr = _declareDefault();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("(a&&b)").get();
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.nodeType());
        Assertions.assertEquals("(", n.name());
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.children().get(0).nodeType());
        Assertions.assertEquals("&&", n.children().get(0).name());
        TestUtils.println(n);
    }

    @Test
    public void testTokenized() {
        NStreamTokenizer st = new NStreamTokenizer(new StringReader("8.0.0"));
        st.xmlComments(true);
        st.parseNumbers(false);
        st.wordChars('0', '9');
        st.wordChars('.', '.');
        st.wordChars('-', '-');

        int s;
        while ((s = st.nextToken()) != NToken.TT_EOF) {
            TestUtils.println(st.image);
        }
    }

    @Test
    public void testTokenize2() {
        NStreamTokenizer st = new NStreamTokenizer("<<");
        st.acceptTokenType(NToken.TT_LEFT_SHIFT);
        int i = st.nextToken();
        Assertions.assertEquals(NToken.TT_LEFT_SHIFT, i);
        System.out.println(i);
    }


    @Test
    public void test6() throws Exception {
        NExprContext expr = declareMutable();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("if (a) 'hello' else 'hella'").get();
        Assertions.assertEquals(NExprNodeType.IF, n.nodeType());
        TestUtils.println(n);
    }

    @Test
    public void test7b() throws Exception {
        NExprMutableContext expr = declareMutable();
//        _retain(expr,"infix:+");
        expr.declareVar("a");
        NExprNode n = expr.parse("a=1").get();
//        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n.eval(expr).get());
    }

    @Test
    public void test7() throws Exception {
        NExprContext expr = declareMutable();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("if (a) 'hello' else {'hella'};x=3").get();
//        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n);
    }

    @Test
    public void test8() throws Exception {
        NExprContext expr = declareMutable();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("printChunk(0);;;;\n").get();
//        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n);
    }

    @Test
    public void test9() throws Exception {
        NExprContext expr = declareMutable();
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("printChunk(0);;printChunk(0);;printChunk(0)\n").get();
//        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n);
    }

    private static NExprMutableContext declareMutable() {
        return NExprContextBuilder.of()
                .declareBuiltins()
                .buildMutable();
    }


    @Test
    public void test10() throws Exception {
        NExprMutableContext expr = declareMutable();
        expr.declareVar("v");
        expr.setVarValue("v", "me");
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("$'something for $v'").get();
        TestUtils.println(n.eval(expr).get());
        Assertions.assertEquals("something for me", n.eval(expr).get());
    }

    @Test
    public void test11() throws Exception {
        NExprMutableContext expr = declareMutable();
        NExprNode n = expr.parse("a*b+c").get();
        Assertions.assertTrue(n.name().equals("+"));
        Assertions.assertTrue(n.children().size() == 2);
        Assertions.assertTrue(n.children().get(0).name().equals("*"));
        Assertions.assertTrue(n.children().get(1) instanceof NExprWordNode);
    }

    @Test
    public void test12() throws Exception {
        NExprMutableContext expr = declareMutable();
        NExprNode n = expr.parse("a.b>1").get();
        Assertions.assertTrue(n.name().equals(">"));
        Assertions.assertTrue(n.children().size() == 2);
        Assertions.assertTrue(n.children().get(0).name().equals("."));
        Assertions.assertTrue(n.children().get(1) instanceof NExprLiteralNode);
    }
    @Test
    public void test13() throws Exception {
        NExprMutableContext expr = declareMutable();
        NExprNode n = expr.parse("a=b.c>2").get();
        Assertions.assertTrue(n.name().equals("="));
        Assertions.assertTrue(n.children().size() == 2);
        Assertions.assertTrue(n.children().get(0).name().equals("a"));
        Assertions.assertTrue(n.children().get(1).name().equals(">"));
    }

    @Test
    public void test14() throws Exception {
        NExprMutableContext expr = declareMutable();
        NExprNode n = expr.parse("(b.c)>2").get();
        Assertions.assertTrue(n.name().equals(">"));
        Assertions.assertTrue(n.children().size() == 2);
        Assertions.assertTrue(n.children().get(0).name().equals("("));
        Assertions.assertTrue(n.children().get(1) instanceof NExprLiteralNode);
    }

    @Test
    public void test15() throws Exception {
        NExprMutableContext expr = declareMutable();
        NExprNode n = expr.parse("plots[plotId].title").get();
        Assertions.assertTrue(n.name().equals("."));
        Assertions.assertTrue(n.children().size() == 2);
        Assertions.assertTrue(n.children().get(0).name().equals("["));
        Assertions.assertTrue(n.children().get(1) instanceof NExprWordNode);
    }
}
