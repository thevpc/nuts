/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

/**
 * @author thevpc
 */
public class Test20_VersionTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test1() {
        checkEq("1.0", "[1.0]", session);
    }

    @Test
    public void test2() {
        checkEq("[1.0]", "[1.0]", session);
    }

    @Test
    public void test3() {
        checkEq("[1.0,[", "[1.0,[", session);
    }

    @Test
    public void test4() {
        checkEq("[1.0,1.0[", "[1.0[", session);
    }

    @Test
    public void test5() {
        checkEq("]1.0,1.0[", "]1.0[", session);
    }

    @Test
    public void test6() {
        checkEq("[,1.0[", "],1.0[", session);
    }

    @Test
    public void test7() {
        checkEq("[,[", "],[", session);
    }

    @Test
    public void test8() {
        checkEq("[1,2[  ,  [1,3]", "[1,2[, [1,3]", session);
    }

    @Test
    public void test9() {
        String value = NutsVersion.BLANK.inc(-1).getValue();
        TestUtils.println(value);
        Assertions.assertEquals("1", value);
    }

    @Test
    public void test10() {
        NutsVersion v1 = NutsVersion.of("1.2-preview").get();
        NutsVersion v2 = NutsVersion.of("1.2").get();
        Assertions.assertTrue(v1.compareTo(v2)<0);
    }

    @Test
    public void test11() {
        NutsVersion v1 = NutsVersion.of("1.2-preview").get();
        NutsVersion v2 = NutsVersion.of("1.2.1").get();
        Assertions.assertTrue(v1.compareTo(v2)<0);
    }

    @Test
    public void test12() {
        Assertions.assertTrue(DefaultNutsVersion.compareVersions("1.2-preview","1.2.1")<0);
        Assertions.assertTrue(DefaultNutsVersion.compareVersions("1.2","1.2.1")<0);
        Assertions.assertTrue(DefaultNutsVersion.compareVersions("1.2-preview","1.2-rc")>0);
        Assertions.assertTrue(DefaultNutsVersion.compareVersions("1.2-alpha","1.2-beta")<0);
        Assertions.assertTrue(DefaultNutsVersion.compareVersions("1.2-rc","1.2-ga")<0);
        Assertions.assertTrue(DefaultNutsVersion.compareVersions("1.2-gamma","1.2-Gamma")==0);
        Assertions.assertTrue(DefaultNutsVersion.compareVersions("1.2-gamma","1.2-hecta")<0);
    }

    @Test
    public void test13() {
        checkEq("[1,2[  ,  [1,2],  [1,3]", "[1,2[, [1,2], [1,3]", session);
    }

    private void checkEq(String a, String b, NutsSession session) {
        NutsVersionFilter u = NutsVersionFilters.of(session).parse(a);
        String b2 = u.toString();
        Assertions.assertEquals(b, b2);
        TestUtils.println(a + " ==> " + b);
    }
}
