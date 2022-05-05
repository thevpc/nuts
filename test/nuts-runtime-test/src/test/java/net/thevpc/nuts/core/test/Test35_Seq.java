/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTitleSequence;
import net.thevpc.nuts.text.NutsTitleSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author thevpc
 */
public class Test35_Seq {

    @Test
    public void test01() {
        NutsTitleSequence s=new DefaultNutsTitleSequence();
        System.out.println(s);
        s=s.next(5);
        Assertions.assertEquals("1.1.1.1.1.",s.toString());
        s=s.next(1);
        Assertions.assertEquals("2.",s.toString());
    }

    @Test
    public void test02() {
        NutsTitleSequence s = new DefaultNutsTitleSequence("1-A-a-*.");
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
        NutsTitleSequence s=new DefaultNutsTitleSequence("1-1)");
        System.out.println(s);
        s=s.next(5);
        Assertions.assertEquals("1-1-1-1-1)",s.toString());
        s=s.next(1);
        Assertions.assertEquals("2)",s.toString());
    }
}
