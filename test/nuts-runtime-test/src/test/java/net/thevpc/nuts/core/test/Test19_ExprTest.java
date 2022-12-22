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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.xtra.expr.NutsToken;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StreamTokenizerExt;
import org.junit.jupiter.api.*;

import java.io.StringReader;

/**
 * @author thevpc
 */
public class Test19_ExprTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewTestWorkspace();
    }

    private boolean accept(NutsExprOpDeclaration d, String pattern) {
        NutsExprOpType f;
        String n;
        if (pattern.startsWith("prefix:")) {
            f = NutsExprOpType.PREFIX;
            n = pattern.substring("prefix:".length());
        } else if (pattern.startsWith("postfix:")) {
            f = NutsExprOpType.POSTFIX;
            n = pattern.substring("postfix:".length());
        } else if (pattern.startsWith("infix:")) {
            f = NutsExprOpType.INFIX;
            n = pattern.substring("infix:".length());
        } else {
            return false;
        }
        return f.equals(d.getType()) && n.equals(d.getName());

    }

    private boolean accept(NutsExprOpDeclaration d, String... patterns) {
        for (String pattern : patterns) {
            if (accept(d, pattern)) {
                return true;
            }
        }
        return false;
    }

    private void _retain(NutsExprDeclarations expr, String... patterns) {
        if (expr instanceof NutsExprMutableDeclarations) {
            NutsExprMutableDeclarations d = (NutsExprMutableDeclarations) expr;
            for (NutsExprOpDeclaration operator : d.getOperators()) {
                if (!accept(operator, patterns)) {
                    d.removeDeclaration(operator);
                }
            }
        }
    }

    @Test
    public void test1() throws Exception {
        NutsExprDeclarations expr = NutsExpr.of(session).newDeclarations(true);
        _retain(expr, "infix:+");
        NutsExprNode n = expr.parse("1+2+3").get();
        TestUtils.println(n);
        Assertions.assertEquals("1 + 2 + 3", n.toString());
    }

    @Test
    public void test2() throws Exception {
        NutsExprDeclarations expr = NutsExpr.of(session).newDeclarations(true);
//        _retain(expr,"infix:+");
        NutsExprNode n = expr.parse("1+2*3").get();
        TestUtils.println(n);
        Assertions.assertEquals("1 + 2 * 3", n.toString());
    }

    @Test
    public void test3() throws Exception {
        NutsExprDeclarations expr = NutsExpr.of(session).newDeclarations(true);
//        _retain(expr,"infix:+");
        NutsExprNode n = expr.parse("a").get();
        Assertions.assertEquals(NutsExprNodeType.WORD, n.getType());
        TestUtils.println(n);
    }

    @Test
    public void test4() throws Exception {
        NutsExprDeclarations expr = NutsExpr.of(session).newDeclarations(true);
//        _retain(expr,"infix:+");
        NutsExprNode n = expr.parse("(a&b)").get();
        n.toString();
        Assertions.assertEquals(NutsExprNodeType.OPERATOR, n.getType());
        Assertions.assertEquals("(", n.getName());
        Assertions.assertEquals(NutsExprNodeType.OPERATOR, n.getChildren().get(0).getType());
        Assertions.assertEquals("&", n.getChildren().get(0).getName());
        TestUtils.println(n);
    }


    @Test
    public void test5() throws Exception {
        NutsExprDeclarations expr = NutsExpr.of(session).newDeclarations(true);
//        _retain(expr,"infix:+");
        NutsExprNode n = expr.parse("(a&&b)").get();
        Assertions.assertEquals(NutsExprNodeType.OPERATOR, n.getType());
        Assertions.assertEquals("(", n.getName());
        Assertions.assertEquals(NutsExprNodeType.OPERATOR, n.getChildren().get(0).getType());
        Assertions.assertEquals("&&", n.getChildren().get(0).getName());
        TestUtils.println(n);
    }

    @Test
    public void testTokenized() {
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader("8.0.0"), session);
        st.xmlComments(true);
        st.doNotParseNumbers();
        st.wordChars('0', '9');
        st.wordChars('.', '.');
        st.wordChars('-', '-');

        int s;
        while ((s = st.nextToken()) != StreamTokenizerExt.TT_EOF) {
            TestUtils.println(st.image);
        }
    }

    @Test
    public void testTokenize2() {
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader("<<"), session);
        int i = st.nextToken();
        Assertions.assertEquals(NutsToken.TT_LEFT_SHIFT, i);
        System.out.println(i);
    }


}
