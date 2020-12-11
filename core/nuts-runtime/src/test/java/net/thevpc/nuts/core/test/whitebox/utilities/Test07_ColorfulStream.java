/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.whitebox.utilities;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.format.text.*;
import net.thevpc.nuts.runtime.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.format.text.renderer.AnsiUnixTermPrintRenderer;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import org.junit.jupiter.api.Assertions;
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
        NutsWorkspace ws= Nuts.openWorkspace();
        PrintStream out = new PrintStream(new FormatOutputStream(System.out,ws));
        for (String msg : new String[]{
                "[]", "<>",
                "\"\"",
                "''", "{}"
        }) {
            for (char c : msg.toCharArray()) {
                out.print(c);
            }
            out.println();
            TestUtils.println(ws.formats().text().filterText(msg));
        }
    }

    @Test
    public void test3() {
//        String msg="x{{\\?}}x";

        NutsWorkspace ws= Nuts.openWorkspace();
        PrintStream out = new PrintStream(new FormatOutputStream(System.out,ws));
        out.println("#####value             ##### = me");
    }

    @Test
    public void test4() {
//        String msg="x{{\\?}}x";
        NutsWorkspace ws = Nuts.openWorkspace();
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER,ws)
                .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true))
                ;
        NutsTextNodeWriterStringer w2 = new NutsTextNodeWriterStringer(System.out);
//        String text = "[#tet] hello == \\= me\n";
        String text = "\n##) njob" +
                "\n###) njob" +
                "\n####) njob" +
                "\n#####) njob" +
                "\n######) njob" +
                "\n#######) njob" +
                "\n########) njob" +
                "\n#########) njob" +
                "\n##########) njob" +
                "";
        System.out.println(text);
        System.out.println("\n--------------------------------");
        NutsTextNodeParser parser = new DefaultNutsTextNodeParser(ws);
        NutsTextNode node = parser.parse(new StringReader(text));
        System.out.println(node);
        System.out.println("\n--------------------------------");
        w.writeNode(node);
        System.out.println("\n--------------------------------");
        w2.writeNode(node, new NutsTextNodeWriteConfiguration().setNumberTitles(true));

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        PrintStream out = new PrintStream(new FormatOutputStream(bos));
//        out.println("[#tet] hello == \\= me");
//        System.out.println("as a result :: " + new String(bos.toByteArray()));
    }

    @Test
    public void test5() {
//        String msg="x{{\\?}}x";

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER,ws)
                .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
        NutsTextNodeWriterStringer w2 = new NutsTextNodeWriterStringer(System.out);
//        String text = "[#tet] hello == \\= me\n";
        String text =
                "\n 1 ## text ##" +
                "\n 2 ### text ###" +
                "\n 3 #### text ####" +
                "\n 4 ##### text #####" +
                "\n 5 ###### text ######" +
                "\n 6 ####### text #######" +
                "\n 7 ######## text ########" +
                "\n 8 ######### text #########" +
                "\n 9 ########## text ##########" +
                "\n"+
                "\n 1 ```@@ text @@``` @@ text @@" +
                "\n 2 ```@@@ text @@@``` @@@ text @@@" +
                "\n 3 ```@@@@ text @@@@``` @@@@ text @@@@" +
                "\n 4 ```@@@@@ text @@@@@``` @@@@@ text @@@@@" +
                "\n 5 ```@@@@@@ text @@@@@@``` @@@@@@ text @@@@@@" +
                "\n 6 ```@@@@@@@ text @@@@@@@``` @@@@@@@ text @@@@@@@" +
                "\n 7 ```@@@@@@@@ text @@@@@@@@``` @@@@@@@@ text @@@@@@@@" +
                "\n 8 ```@@@@@@@@@ text @@@@@@@@@``` @@@@@@@@@ text @@@@@@@@@" +
                "\n 9 ```@@@@@@@@@@ text @@@@@@@@@@``` @@@@@@@@@@ text @@@@@@@@@@" +
                "\n"+
                "\n 1 ~~ text ~~" +
                "\n 2 ~~~ text ~~~" +
                "\n 3 ~~~~ text ~~~~" +
                "\n 4 ~~~~~ text ~~~~~"+
                "\n"+
                "\n ##ø###hello###me##"+
                "\n ## ###hello### me##"+
                ""
            ;
        System.out.println(text);
        System.out.println("\n--------------------------------");
        NutsTextNodeParser parser = new DefaultNutsTextNodeParser(ws);
        NutsTextNode node = parser.parse(new StringReader(text));
        System.out.println(node);
        System.out.println("\n--------------------------------");
        w.writeNode(node);
        System.out.println("\n--------------------------------");
        w2.writeNode(node, new NutsTextNodeWriteConfiguration().setNumberTitles(true));

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        PrintStream out = new PrintStream(new FormatOutputStream(bos));
//        out.println("[#tet] hello == \\= me");
//        System.out.println("as a result :: " + new String(bos.toByteArray()));
    }

    @Test
    public void test6() {
//        String msg="x{{\\?}}x";

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER,ws)
                .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
        NutsTextNodeWriterStringer w2 = new NutsTextNodeWriterStringer(System.out);
//        String text = "[#tet] hello == \\= me\n";
        String text = "unable to create system terminal : %s";
        System.out.println(text);
        System.out.println("\n--------------------------------");
        NutsTextNodeParser parser = new DefaultNutsTextNodeParser(ws);
        NutsTextNode node = parser.parse(new StringReader(text));
        System.out.println(node);
        System.out.println("\n--------------------------------");
        w.writeNode(node);
        System.out.println("\n--------------------------------");
        w2.writeNode(node, new NutsTextNodeWriteConfiguration().setNumberTitles(true));

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        PrintStream out = new PrintStream(new FormatOutputStream(bos));
//        out.println("[#tet] hello == \\= me");
//        System.out.println("as a result :: " + new String(bos.toByteArray()));
    }

    @Test
    public void test7() {
        NutsWorkspace ws = Nuts.openWorkspace();
        String t_colors=CoreIOUtils.loadString(getClass().getResourceAsStream("nuts-help-colors.help"),true);
        NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_colors));
        NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER,ws)
                .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
        w.writeNode(node);
    }

    @Test
    public void test8() {
        NutsWorkspace ws = Nuts.openWorkspace();
        {
            String t_string = "###øaaø###";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }
        {
            String t_string = "####aa####";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }
        {
            String t_string = "###ø####aa####ø###";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }
        {
            String t_string = "###ø####aa####ø###";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterStringer(System.out)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            Assertions.assertTrue(
                    node instanceof NutsTextNodeStyled
            );
            Assertions.assertTrue(
                    ((NutsTextNodeStyled)node).getChild() instanceof NutsTextNodeStyled
            );
            Assertions.assertTrue(
                    ((NutsTextNodeStyled)(((NutsTextNodeStyled)node).getChild())).getChild() instanceof NutsTextNodePlain
            );
            w.writeNode(node);
        }
    }

    @Test
    public void test9() {
        NutsWorkspace ws = Nuts.openWorkspace();
        {
            String t_string = "##) aa\n" +
                    "hello";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }

    }

    @Test
    public void test10() {
        NutsWorkspace ws = Nuts.openWorkspace();
        {
            String t_string =
                    "${workspace}\n" +
                    "\n" +
                    "##)SYNOPSIS\n" +
                    "```sh\n" +
                    "nuts [<options>]... <command> <args> ...\n" +
                    "```\n" +
                    "For Help, type ```sh nuts help```\n" +
                    "\n" +
                    "Welcome to ##nuts##. Yeah, it is ###working###...";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }

    }
}
