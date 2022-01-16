/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.io.util.NutsPathParts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thevpc
 */
public class Test31_NutsPathParts {

    @BeforeAll
    public static void init() {
    }

    @Test
    public void test01() {
        NutsPathParts h=new NutsPathParts("http://a:password@here.com/a/b/c?a=toz");
        Assertions.assertEquals(NutsPathParts.Type.URL,h.getType());
        Assertions.assertEquals("http",h.getProtocol());
        Assertions.assertEquals("a:password@here.com",h.getAuthority());
        Assertions.assertEquals("/a/b/c",h.getLocation());
        Assertions.assertEquals("a=toz",h.getQuery());
        Assertions.assertEquals("",h.getRef());
    }
    @Test
    public void test02() {
        NutsPathParts h=new NutsPathParts("http://a:password@here.com:12?a=toz/be");
        Assertions.assertEquals(NutsPathParts.Type.URL,h.getType());
        Assertions.assertEquals("http",h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12",h.getAuthority());
        Assertions.assertEquals("",h.getLocation());
        Assertions.assertEquals("a=toz/be",h.getQuery());
        Assertions.assertEquals("",h.getRef());
    }

    @Test
    public void test03() {
        NutsPathParts h=new NutsPathParts("http://a:password@here.com:12/");
        Assertions.assertEquals(NutsPathParts.Type.URL,h.getType());
        Assertions.assertEquals("http",h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12",h.getAuthority());
        Assertions.assertEquals("/",h.getLocation());
        Assertions.assertEquals("",h.getQuery());
        Assertions.assertEquals("",h.getRef());
    }

    @Test
    public void test04() {
        NutsPathParts h=new NutsPathParts("http://a:password@here.com:12#something");
        Assertions.assertEquals(NutsPathParts.Type.URL,h.getType());
        Assertions.assertEquals("http",h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12",h.getAuthority());
        Assertions.assertEquals("",h.getLocation());
        Assertions.assertEquals("",h.getQuery());
        Assertions.assertEquals("something",h.getRef());
    }

    @Test
    public void test05() {
        NutsPathParts h=new NutsPathParts("#something");
        Assertions.assertEquals(NutsPathParts.Type.REF,h.getType());
        Assertions.assertEquals("",h.getProtocol());
        Assertions.assertEquals("",h.getAuthority());
        Assertions.assertEquals("",h.getLocation());
        Assertions.assertEquals("",h.getQuery());
        Assertions.assertEquals("something",h.getRef());
    }

}
