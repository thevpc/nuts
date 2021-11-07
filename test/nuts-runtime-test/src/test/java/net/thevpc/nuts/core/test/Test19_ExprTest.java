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

import net.thevpc.nuts.NutsExpr;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.expr.StreamTokenizerExt;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author vpc
 */
public class Test19_ExprTest {

    private void _retain(NutsExpr expr,String... patterns){
        for (NutsExpr.OpType opt : NutsExpr.OpType.values()) {
            for (String o : expr.getOperatorNames(opt)) {
                boolean ok=false;
                for (String pattern : patterns) {
                    if(pattern.startsWith("prefix:")) {
                        String so = pattern.substring("prefix:".length());
                        if ((o.equals(so) && opt == NutsExpr.OpType.INFIX)) {
                            ok = true;
                            break;
                        }
                    }else if(pattern.startsWith("infix:")){
                        String so=pattern.substring("infix:".length());
                        if((o.equals(so) && opt== NutsExpr.OpType.INFIX)) {
                            ok=true;
                            break;
                        }
                    }else if(pattern.startsWith("postfix:")){
                        String so=pattern.substring("postfix:".length());
                        if((o.equals(so) && opt== NutsExpr.OpType.POSTFIX)) {
                            ok=true;
                            break;
                        }
                    }
                }
                if(!ok){
                    expr.setOperator(o, opt, -1, false, null);
                }
            }
        }
    }

    @Test
    public void test1() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace();
        NutsExpr expr = NutsExpr.of(session);
        _retain(expr,"infix:+");
        NutsExpr.Node n = expr.parse("1+2+3");
        System.out.println(n);
        Assertions.assertEquals("1 + 2 + 3",n.toString());
    }

    @Test
    public void test2() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace();
        NutsExpr expr = NutsExpr.of(session);
//        _retain(expr,"infix:+");
        NutsExpr.Node n = expr.parse("1+2*3");
        System.out.println(n);
        Assertions.assertEquals("1 + 2 * 3",n.toString());
    }

    @Test
    public void testTokenized() {
        NutsSession session = TestUtils.openExistingTestWorkspace();
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader("8.0.0"),session);
        st.xmlComments(true);
        st.doNotParseNumbers();
        st.wordChars('0','9');
        st.wordChars('.','.');
        st.wordChars('-','-');

        int s;
        while ((s = st.nextToken()) != StreamTokenizerExt.TT_EOF) {
            System.out.println(st.image);
        }
    }
}
