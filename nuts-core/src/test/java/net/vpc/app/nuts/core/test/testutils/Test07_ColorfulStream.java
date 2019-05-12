/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.testutils;

import net.vpc.app.nuts.core.terminals.NutsAnsiUnixTermPrintStream;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test07_ColorfulStream {
    @Test
    public void test(){
        String msg="<-t>";
//        String msg="[[-t]] or [[--trace]] : enable trace operation with a meaning message \\( or disable it with [[--!trace]] or [[--trace=false]] \\)";
        NutsAnsiUnixTermPrintStream out=new NutsAnsiUnixTermPrintStream(System.out);
        out.println(msg);
    }
}
