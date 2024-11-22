/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
public class Test19_ExprTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewTestWorkspace();
    }

    private boolean accept(NExprOpDeclaration d, String pattern) {
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
        return f.equals(d.getType()) && n.equals(d.getName());

    }

    private boolean accept(NExprOpDeclaration d, String... patterns) {
        for (String pattern : patterns) {
            if (accept(d, pattern)) {
                return true;
            }
        }
        return false;
    }

    private void _retain(NExprDeclarations expr, String... patterns) {
        if (expr instanceof NExprMutableDeclarations) {
            NExprMutableDeclarations d = (NExprMutableDeclarations) expr;
            for (NExprOpDeclaration operator : d.getOperators()) {
                if (!accept(operator, patterns)) {
                    d.removeDeclaration(operator);
                }
            }
        }
    }

    @Test
    public void test1() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
        _retain(expr, "infix:+");
        NExprNode n = expr.parse("1+2+3").get();
        TestUtils.println(n);
        Assertions.assertEquals("1 + 2 + 3", n.toString());
    }

    @Test
    public void test2() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("1+2*3").get();
        TestUtils.println(n);
        Assertions.assertEquals("1 + 2 * 3", n.toString());
    }

    @Test
    public void test3() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("a").get();
        Assertions.assertEquals(NExprNodeType.WORD, n.getType());
        TestUtils.println(n);
    }

    @Test
    public void test4() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("(a&b)").get();
        n.toString();
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.getType());
        Assertions.assertEquals("(", n.getName());
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.getChildren().get(0).getType());
        Assertions.assertEquals("&", n.getChildren().get(0).getName());
        TestUtils.println(n);
    }


    @Test
    public void test5() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("(a&&b)").get();
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.getType());
        Assertions.assertEquals("(", n.getName());
        Assertions.assertEquals(NExprNodeType.OPERATOR, n.getChildren().get(0).getType());
        Assertions.assertEquals("&&", n.getChildren().get(0).getName());
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
        while ((s = st.nextToken()) != NStreamTokenizer.TT_EOF) {
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
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("if (a) 'hello' else 'hella'").get();
        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n);
    }

    @Test
    public void test7() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("if (a) 'hello' else {'hella'};x=3").get();
//        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n);
    }
    @Test
    public void test8() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("printChunk(0);;;;\n").get();
//        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n);
    }

    @Test
    public void test9() throws Exception {
        NExprDeclarations expr = NExpr.of().newDeclarations(true);
//        _retain(expr,"infix:+");
        NExprNode n = expr.parse("printChunk(0);;printChunk(0);;printChunk(0)\n").get();
//        Assertions.assertEquals(NExprNodeType.IF, n.getType());
        TestUtils.println(n);
    }
}
