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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class IdTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        List<NId> s = NId.getList("java#[11,[").get();
        Assertions.assertEquals(Arrays.asList(NId.get("java#[11,[").get()), s);
    }

    @Test
    public void test02() {
        List<NId> s = NId.getList("java#[11,[ java#[11,[").get();
        //removed duplicates...
        Assertions.assertEquals(Arrays.asList(NId.get("java#[11,[").get()), s);
    }

    @Test
    public void test03() {
        List<NId> s = NId.getList("java#[11,[ java#[12,[").get();
        Assertions.assertEquals(Arrays.asList(
                NId.get("java#[11,[").get(),
                NId.get("java#[12,[").get()
        ), s);
    }

    @Test
    public void test04() {
        String t1="net.sourceforge.cobertura:cobertura#coberturaVersion?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&cond-properties=a%2Cb%3Dc";
        NId s = NId.of(t1);
        Assertions.assertEquals("net.sourceforge.cobertura",s.groupId());
        Assertions.assertEquals("cobertura",s.artifactId());
        Assertions.assertEquals("coberturaVersion",s.version().toString());
        Assertions.assertEquals("asm:asm,asm:asm-tree,log4j:log4j,oro:oro",s.properties().get("exclusions"));
        Assertions.assertEquals(null,s.condition().properties().get("a"));
        Assertions.assertEquals("c",s.condition().properties().get("b"));
        TestUtils.println(s);
    }

    @Test
    public void test05() {
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&cond-properties=a%2Cb%3Dc";
        NId s = NId.of(t1);
        Assertions.assertEquals("net.sourceforge.cobertura",s.groupId());
        Assertions.assertEquals("cobertura",s.artifactId());
        Assertions.assertEquals("asm:asm,asm:asm-tree,log4j:log4j,oro:oro",s.properties().get("exclusions"));
        Assertions.assertEquals(null,s.condition().properties().get("a"));
        Assertions.assertEquals("c",s.condition().properties().get("b"));
        TestUtils.println(s);
    }

    @Test
    public void test06() {
        NId a = NIdBuilder.of()
                .setProperty("a", "?")
                .build();
        Map<String, String> p = a.properties();
        TestUtils.println(a.toString());
        Assertions.assertEquals(1,p.size());
        Assertions.assertEquals("?a=%3F",a.toString());
    }

    @Test
    public void test07() {
        NId a = NId.of("a");
        Assertions.assertEquals(null,a.groupId());
        Assertions.assertEquals("a",a.artifactId());
        Assertions.assertEquals(null,a.classifier());
    }

    @Test
    public void test08() {
        NId a = NId.of(":a");
        Assertions.assertEquals(null,a.groupId());
        Assertions.assertEquals("a",a.artifactId());
        Assertions.assertEquals(null,a.classifier());
    }
    @Test
    public void test09() {
        NId a = NId.of("a:");
        Assertions.assertEquals("a",a.groupId());
        Assertions.assertEquals(null,a.artifactId());
        Assertions.assertEquals(null,a.classifier());
    }

    @Test
    public void test10() {
        NId a = NId.of(":a:");
        Assertions.assertEquals(null,a.groupId());
        Assertions.assertEquals("a",a.artifactId());
        Assertions.assertEquals(null,a.classifier());
    }

    @Test
    public void test11() {
        NId a = NId.of("g:a");
        Assertions.assertEquals("g",a.groupId());
        Assertions.assertEquals("a",a.artifactId());
        Assertions.assertEquals(null,a.classifier());
    }
    @Test
    public void test12() {
        NId a = NId.of(":a:c");
        Assertions.assertEquals(null,a.groupId());
        Assertions.assertEquals("a",a.artifactId());
        Assertions.assertEquals("c",a.classifier());
    }
    @Test
    public void test13() {
        NId a = NId.of("g:a:c");
        Assertions.assertEquals("g",a.groupId());
        Assertions.assertEquals("a",a.artifactId());
        Assertions.assertEquals("c",a.classifier());
    }
    @Test
    public void test14() {
        NId a = NId.of("::c");
        Assertions.assertEquals(null,a.groupId());
        Assertions.assertEquals(null,a.artifactId());
        Assertions.assertEquals("c",a.classifier());
    }
    @Test
    public void test15() {
        NId a = NId.of(":");
        Assertions.assertEquals(null,a.groupId());
        Assertions.assertEquals(null,a.artifactId());
        Assertions.assertEquals(null,a.classifier());
    }
    @Test
    public void test16() {
        NId a = NId.of("::#2");
        Assertions.assertEquals(null,a.groupId());
        Assertions.assertEquals(null,a.artifactId());
        Assertions.assertEquals(null,a.classifier());
        Assertions.assertEquals("2",a.version().toString());
    }
}
