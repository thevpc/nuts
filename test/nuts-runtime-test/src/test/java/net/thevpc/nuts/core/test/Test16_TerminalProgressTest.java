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

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author vpc
 */
public class Test16_TerminalProgressTest {


    @Test
    public void test1() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace(
                "-byZSKk"
        );

        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            session.getTerminal().printProgress((i / 100f), "message %s", i);
        }
    }

    @Test
    public void test2() {
        CProgressBar rr = new CProgressBar(null);
        rr.setFormatter(CProgressBar.CIRCLES2);
        rr.setMinPeriod(-1);
        for (CProgressBar.Formatter formatter : new CProgressBar.Formatter[]{
//            CProgressBar.CIRCLES2,
//            CProgressBar.CIRCLES,
//            CProgressBar.DOTS1,
//            CProgressBar.DOTS2,
//            CProgressBar.PARALLELOGRAM,
//            CProgressBar.RECTANGLES,
//            CProgressBar.RECTANGLES2,
//            CProgressBar.RECTANGLES3,
                CProgressBar.RECTANGLES4,
//            CProgressBar.SIMPLE,
        }) {
            rr.setFormatter(formatter);
            for (int i = 0; i < 100; i++) {
                System.out.printf("%2d ::" + rr.progress(i) + "\n", i);
            }
            for (int i = 0; i < 12; i++) {
                int finalI = i;
                rr.setIndeterminatePosition((CProgressBar bar, int size) -> finalI % size);
                System.out.printf("%2d ::" + rr.progress(-1) + "\n", i);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(TestCProgressBar.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
    }

    @Test
    public void test3() {
        NutsSession session = TestUtils.openNewTestWorkspace("-k");
        CProgressBar rr = new CProgressBar(session);
        rr.setFormatter(CProgressBar.CIRCLES2);
        rr.setMinPeriod(-1);
        for (CProgressBar.Formatter formatter : new CProgressBar.Formatter[]{
//            CProgressBar.CIRCLES2,
                CProgressBar.CIRCLES,
//            CProgressBar.DOTS1,
//            CProgressBar.DOTS2,
//            CProgressBar.PARALLELOGRAM,
//            CProgressBar.RECTANGLES,
//            CProgressBar.RECTANGLES2,
//            CProgressBar.RECTANGLES3,
                CProgressBar.RECTANGLES4,
//            CProgressBar.SIMPLE,
        }) {
            rr.setFormatter(formatter);
            for (int i = 0; i < 100; i++) {
//                System.out.printf("%2d ::" + rr.progress(i) + "\n", i);
                session.out().printf("%2d ::" + rr.progress(i) + "\n", i);
            }
            for (int i = 0; i < 12; i++) {
                int finalI = i;
                rr.setIndeterminatePosition((CProgressBar bar, int size) -> finalI % size);
                System.out.printf("%2d ::" + rr.progress(-1) + "\n", i);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(TestCProgressBar.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
    }

    @Test
    public void test4() {
        CProgressBar rr = new CProgressBar(null);
        for (int i = 0; i < 12; i++) {
            int finalI = i;
            rr.setIndeterminatePosition(new CProgressBar.IndeterminatePosition() {
                @Override
                public int evalIndeterminatePos(CProgressBar bar, int size) {
                    return finalI % size;
                }
            });
            TestUtils.println(i + " ::" + rr.progress(-1));
        }
        Assertions.assertTrue(true);
    }
}