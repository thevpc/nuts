/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathExtensionType;
import net.thevpc.nuts.io.NPathNameParts;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;
import net.thevpc.nuts.io.NIOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
        Assertions.assertArrayEquals(new String[]{"a1.2", "", ""}, d("a1.2"));
        Assertions.assertArrayEquals(new String[]{"a1.2", "a", ".a"}, d("a1.2.a"));
        Assertions.assertArrayEquals(new String[]{"", "", ""}, d(""));
        Assertions.assertArrayEquals(new String[]{"", "", "."}, d("."));
        Assertions.assertArrayEquals(new String[]{"a", "", ""}, d("a"));
        Assertions.assertArrayEquals(new String[]{"a", "", "."}, d("a."));
        Assertions.assertArrayEquals(new String[]{"", "a", ".a"}, d(".a"));
        Assertions.assertArrayEquals(new String[]{"", "1", ".1"}, d(".1"));
        Assertions.assertArrayEquals(new String[]{"a", "1", ".1"}, d("a.1"));
        Assertions.assertArrayEquals(new String[]{"1.1", "", ""}, d("1.1"));
        Assertions.assertArrayEquals(new String[]{"1", "a1", ".a1"}, d("1.a1"));
        Assertions.assertArrayEquals(new String[]{"1", "1a", ".1a"}, d("1.1a"));
        Assertions.assertArrayEquals(new String[]{"1", "3af", ".3af"}, d("1.3af"));
        Assertions.assertArrayEquals(new String[]{"a1", "2s", ".2s"}, d("a1.2s"));
        Assertions.assertArrayEquals(new String[]{"a.config", "json", ".json"}, d("a.config.json"));
        Assertions.assertArrayEquals(new String[]{"some-value-1.8-20250206-external", "pdf", ".pdf"}, d("some-value-1.8-20250206-external.pdf"));
    }

    @Test
    public void test07() {
        Assertions.assertEquals(NPathNameParts.ofLong("a", "b.c"), NIOUtils.getPathNameParts("a.b.c", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("", null), NIOUtils.getPathNameParts("", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("", ""), NIOUtils.getPathNameParts(".", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("", "a"), NIOUtils.getPathNameParts(".a", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("a", ""), NIOUtils.getPathNameParts("a.", NPathExtensionType.LONG));

        Assertions.assertEquals(NPathNameParts.ofShort("a.b", "c"), NIOUtils.getPathNameParts("a.b.c", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("", null), NIOUtils.getPathNameParts("", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("", ""), NIOUtils.getPathNameParts(".", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("", "a"), NIOUtils.getPathNameParts(".a", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("a", ""), NIOUtils.getPathNameParts("a.", NPathExtensionType.SHORT));


    }

    private String[] d(String n) {
        NPath p = NPath.of(n);
        if (p == null) {
            return new String[]{"", "", ""};
        }
        NPathNameParts nameParts = p.getNameParts(NPathExtensionType.SMART);
        String[] strings = {nameParts.getBaseName(), nameParts.getExtension(), nameParts.getFullExtension()};
        return strings;
    }


}
