/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.whitebox.utilities;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import net.vpc.app.nuts.runtime.util.fprint.FormatOutputStream;
import net.vpc.app.nuts.runtime.util.fprint.util.FormattedPrintStreamUtils;
import org.junit.Test;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public class Test07_ColorfulStream {
//    @Test
//    public void test0(){
//        String msg="[[-t]] or [[--trace]] : enable trace operation with a meaning message \\( or disable it with [[--!trace]] or [[--trace=false]] \\)";
//        NutsPrintStreamFormattedUnixAnsi out=new NutsPrintStreamFormattedUnixAnsi(System.out);
//        out.println(msg);
//    }
//    @Test
//    public void test(){
//        String msg="<-t>";
//        NutsPrintStreamFormattedUnixAnsi out=new NutsPrintStreamFormattedUnixAnsi(System.out);
//        out.println(msg);
//    }
//    
//    @Test
//    public void test1(){
//        String msg="x{{\\?}}x";
//        NutsPrintStreamFormattedUnixAnsi out=new NutsPrintStreamFormattedUnixAnsi(System.out);
//        out.println(msg);
//        out.println();
//    }

    @Test
    public void test2() {
//        String msg="x{{\\?}}x";

        PrintStream out = new PrintStream(new FormatOutputStream(System.out));
        for (String msg : new String[]{
            "[]", "<>",
            "\"\"",
            "''", "{}"
        }) {
            out.println(msg);
            TestUtils.println(FormattedPrintStreamUtils.filterText(msg));
        }
    }

    @Test
    public void test3() {
//        String msg="x{{\\?}}x";

        PrintStream out = new PrintStream(new FormatOutputStream(System.out));
        out.println("==value             == \\= me");
    }
}
