/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.whitebox.utilities;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.util.fprint.*;
import net.thevpc.nuts.runtime.util.fprint.parser.DefaultTextNodeParser;
import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;
import net.thevpc.nuts.runtime.util.fprint.renderer.AnsiUnixTermPrintRenderer;
import net.thevpc.nuts.runtime.util.fprint.util.FormattedPrintStreamUtils;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.io.StringReader;

/**
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
            for (char c : msg.toCharArray()) {
                out.print(c);
            }
            out.println();
            TestUtils.println(FormattedPrintStreamUtils.filterText(msg));
        }
    }

    @Test
    public void test3() {
//        String msg="x{{\\?}}x";

        PrintStream out = new PrintStream(new FormatOutputStream(System.out));
        out.println("==value             == \\= me");
    }

    @Test
    public void test4() {
//        String msg="x{{\\?}}x";

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TextNodeWriter w = new TextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER);
        TextNodeWriterStringer w2 = new TextNodeWriterStringer(System.out);
//        String text = "[#tet] hello == \\= me\n";
        String text = "##) njob";
        System.out.println(text);
        System.out.println("--------------------------------");
        TextNodeParser parser = new DefaultTextNodeParser();
        TextNode node = parser.parse(new StringReader(text));
        System.out.println(node);
        System.out.println("--------------------------------");
        w.writeNode(node, new TextNodeWriterContext().setNumberTitles(true));
        System.out.println("--------------------------------");
        w2.writeNode(node, new TextNodeWriterContext().setNumberTitles(true));

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        PrintStream out = new PrintStream(new FormatOutputStream(bos));
//        out.println("[#tet] hello == \\= me");
//        System.out.println("as a result :: " + new String(bos.toByteArray()));
    }
}
