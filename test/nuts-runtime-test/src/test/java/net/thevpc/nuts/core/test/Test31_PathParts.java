/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author thevpc
 */
public class Test31_PathParts {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        NPathParts h = new NPathParts("http://a:password@here.com/a/b/c?a=toz");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com", h.getAuthority());
        Assertions.assertEquals("/a/b/c", h.getFile());
        Assertions.assertEquals("a=toz", h.getQuery());
        Assertions.assertEquals("", h.getRef());
    }

    @Test
    public void test02() {
        NPathParts h = new NPathParts("http://a:password@here.com:12?a=toz/be");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12", h.getAuthority());
        Assertions.assertEquals("", h.getFile());
        Assertions.assertEquals("a=toz/be", h.getQuery());
        Assertions.assertEquals("", h.getRef());
    }

    @Test
    public void test03() {
        NPathParts h = new NPathParts("http://a:password@here.com:12/");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12", h.getAuthority());
        Assertions.assertEquals("/", h.getFile());
        Assertions.assertEquals("", h.getQuery());
        Assertions.assertEquals("", h.getRef());
    }

    @Test
    public void test04() {
        NPathParts h = new NPathParts("http://a:password@here.com:12#something");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12", h.getAuthority());
        Assertions.assertEquals("", h.getFile());
        Assertions.assertEquals("", h.getQuery());
        Assertions.assertEquals("something", h.getRef());
    }

    @Test
    public void test05() {
        NPathParts h = new NPathParts("#something");
        Assertions.assertEquals(NPathParts.Type.REF, h.getType());
        Assertions.assertEquals("", h.getProtocol());
        Assertions.assertEquals("", h.getAuthority());
        Assertions.assertEquals("", h.getFile());
        Assertions.assertEquals("", h.getQuery());
        Assertions.assertEquals("something", h.getRef());
    }

    @Test
    public void test06() {
        String[] s = d("a1.2");
        Assertions.assertArrayEquals(new String[]{"a1.2", ""}, s);
        s = d("a1.2.a");
        Assertions.assertArrayEquals(new String[]{"a1.2", "a"}, s);
        s = d("");
        Assertions.assertArrayEquals(new String[]{"", ""}, s);
        s = d(".");
        Assertions.assertArrayEquals(new String[]{"", ""}, s);
        s = d("a");
        Assertions.assertArrayEquals(new String[]{"a", ""}, s);
        s = d("a.");
        Assertions.assertArrayEquals(new String[]{"a", ""}, s);
        s = d(".a");
        Assertions.assertArrayEquals(new String[]{"", "a"}, s);
        s = d(".1");
        Assertions.assertArrayEquals(new String[]{"", "1"}, s);
        s = d("a.1");
        Assertions.assertArrayEquals(new String[]{"a", "1"}, s);
        s = d("1.1");
        Assertions.assertArrayEquals(new String[]{"1.1", ""}, s);
        s = d("1.a1");
        Assertions.assertArrayEquals(new String[]{"1", "a1"}, s);
        s = d("1.1a");
        Assertions.assertArrayEquals(new String[]{"1", "1a"}, s);
        s = d("1.3af");
        Assertions.assertArrayEquals(new String[]{"1", "3af"}, s);
        s = d("a1.2s");
        Assertions.assertArrayEquals(new String[]{"a1", "2s"}, s);
        s = d("a.config.json");
        Assertions.assertArrayEquals(new String[]{"a.config", "json"}, s);
    }

    private String[] d(String n) {
        NPath p = NPath.of(n);
        if(p==null){
            return new String[]{"",""};
        }
        return new String[]{p.getSmartBaseName(), p.getSmartExtension()};
    }


}
