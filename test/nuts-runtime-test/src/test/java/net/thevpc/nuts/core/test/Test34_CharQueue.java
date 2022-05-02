/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.util.collections.CharQueue;
import net.thevpc.nuts.runtime.standalone.util.collections.NutsMatchType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author thevpc
 */
public class Test34_CharQueue {

    @Test
    public void test01() {
        CharQueue q = new CharQueue(4);
        Assertions.assertEquals(0, q.length());
        Assertions.assertEquals("", q.toString());

        q.write("ab");
        Assertions.assertEquals(2, q.length());
        Assertions.assertEquals("ab", q.toString());
        q.write("cd");
        Assertions.assertEquals(4, q.length());
        Assertions.assertEquals("abcd", q.toString());
        q.write("ef");
        Assertions.assertEquals(6, q.length());
        Assertions.assertEquals("abcdef", q.toString());

        char c = q.read();
        Assertions.assertEquals('a', c);
        Assertions.assertEquals(5, q.length());
        Assertions.assertEquals("bcdef", q.toString());
        c = q.read();
        Assertions.assertEquals('b', c);
        Assertions.assertEquals(4, q.length());
        Assertions.assertEquals("cdef", q.toString());
        Assertions.assertEquals(NutsMatchType.FULL_MATCH, q.peekString("cdef").mode());
        Assertions.assertEquals(NutsMatchType.PARTIAL_MATCH, q.peekString("cdefg").mode());
        Assertions.assertEquals(NutsMatchType.NO_MATCH, q.peekString("cdgf").mode());
        String s = q.read(5);
        Assertions.assertEquals("cdef", s);
        Assertions.assertEquals(0, q.length());
        Assertions.assertEquals("", q.toString());
        Assertions.assertEquals(false, q.canRead());
    }

    @Test
    public void test02() {
        CharQueue q = new CharQueue();
        q.write("###");

        Assertions.assertEquals(NutsMatchType.FULL_MATCH, q.peekPattern("#").mode());
        Assertions.assertEquals(NutsMatchType.FULL_MATCH, q.peekPattern("###").mode());
        Assertions.assertEquals(NutsMatchType.PARTIAL_MATCH, q.peekPattern("####").mode());
        Assertions.assertEquals(NutsMatchType.NO_MATCH, q.peekPattern("#2").mode());
    }

    @Test
    public void test03() {
        CharQueue q = new CharQueue();
        q.write("###");
        Assertions.assertEquals(NutsMatchType.MATCH, q.peekPattern("#*").mode());
    }

    @Test
    public void test04() {
        CharQueue q = new CharQueue();
        q.write("a");
        Assertions.assertEquals('a', q.read());
    }

    @Test
    public void test05() {
        CharQueue q = new CharQueue(2,2);
        q.write("a");
        Assertions.assertEquals(0, q.getFrom());
        Assertions.assertEquals(1, q.getTo());
        Assertions.assertEquals(2, q.getAllocatedSize());

        q.ensureAvailable(1);
        Assertions.assertEquals(0, q.getFrom());
        Assertions.assertEquals(1, q.getTo());
        Assertions.assertEquals(2, q.getAllocatedSize());

        q.ensureAvailable(2);
        Assertions.assertEquals(0, q.getFrom());
        Assertions.assertEquals(1, q.getTo());
        Assertions.assertEquals(5, q.getAllocatedSize());

        q.read();

        q.ensureAvailable(4);
        Assertions.assertEquals(1, q.getFrom());
        Assertions.assertEquals(1, q.getTo());
        Assertions.assertEquals(5, q.getAllocatedSize());

        q.ensureAvailable(6);
        Assertions.assertEquals(0, q.getFrom());
        Assertions.assertEquals(0, q.getTo());
        Assertions.assertEquals(8, q.getAllocatedSize());

        q.ensureAvailable(8);
        Assertions.assertEquals(0, q.getFrom());
        Assertions.assertEquals(0, q.getTo());
        Assertions.assertEquals(8, q.getAllocatedSize());
    }
}
