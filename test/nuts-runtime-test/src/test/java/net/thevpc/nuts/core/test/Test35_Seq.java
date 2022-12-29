/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.text.DefaultNTitleSequence;
import net.thevpc.nuts.text.NTitleSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author thevpc
 */
public class Test35_Seq {

    @Test
    public void test01() {
        NTitleSequence s=new DefaultNTitleSequence();
        System.out.println(s);
        s=s.next(5);
        Assertions.assertEquals("1.1.1.1.1.",s.toString());
        s=s.next(1);
        Assertions.assertEquals("2.",s.toString());
    }

    @Test
    public void test02() {
        NTitleSequence s = new DefaultNTitleSequence("1-A-a-*.");
        System.out.println(s);
        Assertions.assertEquals("",s.toString());
        s=s.next(5);
        Assertions.assertEquals("   *",s.toString());
        s=s.next(5);
        Assertions.assertEquals("   *",s.toString());
        s=s.next(5);
        Assertions.assertEquals("   *",s.toString());
    }

    @Test
    public void test03() {
        NTitleSequence s=new DefaultNTitleSequence("1-1)");
        System.out.println(s);
        s=s.next(5);
        Assertions.assertEquals("1-1-1-1-1)",s.toString());
        s=s.next(1);
        Assertions.assertEquals("2)",s.toString());
    }
}
