/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NIdBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NStringMapFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class Test32_Id {

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
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&cond-properties='a,b=c'";
        NId s = NId.get(t1).get();
        Assertions.assertEquals("net.sourceforge.cobertura",s.getGroupId());
        Assertions.assertEquals("cobertura",s.getArtifactId());
        Assertions.assertEquals("asm:asm,asm:asm-tree,log4j:log4j,oro:oro",s.getProperties().get("exclusions"));
        Assertions.assertEquals(null,s.getCondition().getProperties().get("a"));
        Assertions.assertEquals("c",s.getCondition().getProperties().get("b"));
        TestUtils.println(s);
    }

    @Test
    public void test05() {
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&cond-properties=a%2Cb%3Dc";
        NId s = NId.of(t1);
        Assertions.assertEquals("net.sourceforge.cobertura",s.getGroupId());
        Assertions.assertEquals("cobertura",s.getArtifactId());
        Assertions.assertEquals("asm:asm,asm:asm-tree,log4j:log4j,oro:oro",s.getProperties().get("exclusions"));
        Assertions.assertEquals(null,s.getCondition().getProperties().get("a"));
        Assertions.assertEquals("c",s.getCondition().getProperties().get("b"));
        TestUtils.println(s);
    }

    @Test
    public void test06() {
        NId a = NIdBuilder.of()
                .setProperty("a", "?")
                .build();
        Map<String, String> p = a.getProperties();
        TestUtils.println(a.toString());
        Assertions.assertEquals(1,p.size());
        Assertions.assertEquals("?a=%3F",a.toString());
    }

    @Test
    public void test07() {
        NId a = NId.of("a");
        Assertions.assertEquals(null,a.getGroupId());
        Assertions.assertEquals("a",a.getArtifactId());
        Assertions.assertEquals(null,a.getClassifier());
    }

    @Test
    public void test08() {
        NId a = NId.of(":a");
        Assertions.assertEquals(null,a.getGroupId());
        Assertions.assertEquals("a",a.getArtifactId());
        Assertions.assertEquals(null,a.getClassifier());
    }
    @Test
    public void test09() {
        NId a = NId.of("a:");
        Assertions.assertEquals(null,a.getGroupId());
        Assertions.assertEquals("a",a.getArtifactId());
        Assertions.assertEquals(null,a.getClassifier());
    }

    @Test
    public void test10() {
        NId a = NId.of(":a:");
        Assertions.assertEquals(null,a.getGroupId());
        Assertions.assertEquals("a",a.getArtifactId());
        Assertions.assertEquals(null,a.getClassifier());
    }

    @Test
    public void test11() {
        NId a = NId.of("g:a");
        Assertions.assertEquals("g",a.getGroupId());
        Assertions.assertEquals("a",a.getArtifactId());
        Assertions.assertEquals(null,a.getClassifier());
    }
    @Test
    public void test12() {
        NId a = NId.of(":a:c");
        Assertions.assertEquals(null,a.getGroupId());
        Assertions.assertEquals("a",a.getArtifactId());
        Assertions.assertEquals("c",a.getClassifier());
    }
    @Test
    public void test13() {
        NId a = NId.of("g:a:c");
        Assertions.assertEquals("g",a.getGroupId());
        Assertions.assertEquals("a",a.getArtifactId());
        Assertions.assertEquals("c",a.getClassifier());
    }
}
