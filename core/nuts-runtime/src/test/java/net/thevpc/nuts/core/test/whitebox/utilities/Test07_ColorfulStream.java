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
 * @author thevpc
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
        String t_colors=CoreIOUtils.loadString(getClass().getResourceAsStream("nuts-help-colors.ntf"),true);
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
                            "Welcome to ```sh nuts```. Yeah, it is ###working###...";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }
    }

    @Test
    public void test11() {
        NutsWorkspace ws = Nuts.openWorkspace();
        {
            String t_string =
                    "øøøøøøheeloøø";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }

    }

    @Test
    public void test12() {
        NutsWorkspace ws = Nuts.openWorkspace();
        {
            String t_string =
                    "hello ```!later-reset-line```";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }

    }

    @Test
    public void test13() {
        NutsWorkspace ws = Nuts.openWorkspace();
        {
            String t_string =
                    " ```sh install [-options]... <nuts-artifact>... <args> ...```\n" +
                            "      install ```sh nuts``` package <nuts-artifact>\n" +
                            "      for more details, type : ```sh nuts``` help install\n";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }

    }

    @Test
    public void test14() {
        NutsWorkspace ws = Nuts.openWorkspace();
        {
            String t_string =
                            " ```sh license [-options]...\n" +
                            "      show license info and exit\n" +
                            "      for more details, type : ```sh nuts``` help info\n" +
                            "\n" +
                            " ```sh install [-options]... <nuts-artifact>... <args> ...```\n" +
                            "      install ```sh nuts``` package <nuts-artifact>\n" +
                            "      for more details, type : ```sh nuts``` help install\n" +
                            "\n" +
                            " ```sh uninstall [-options]... <nuts-artifact>... <args> ...```\n" +
                            "      uninstall  ```sh nuts``` package <nuts-artifact>\n" +
                            "      for more details, type : ```sh nuts``` help uninstall\n" +
                            "\n" +
                            " ```sh update [-options]... <ids> ...  <args> ...```\n" +
                            "      check if a newer version of ```sh nuts``` or any of the provided <ids>\n" +
                            "      is available in the current workspace and perform update by downloading (fetch)\n" +
                            "      and installing the artifact. The updated version is promoted to 'default' version.\n" +
                            "      for more details, type : ```sh nuts``` help update\n" +
                            "            \n" +
                            " ```sh check-updates} [-options]... <ids> ...```\n" +
                            "      check if a newer version of ```sh nuts``` is available in the current workspace without performing updates\n" +
                            "      Takes the same arguments and options as ```sh update``` command\n" +
                            "      for more details, type : ```sh nuts``` help check-updates\n" +
                            "\n" +
                            " ```sh search [-options]... <ids> ...```\n" +
                            "      search for <ids>\n" +
                            "      for more details, type : ```sh nuts``` help search\n" +
                            "            \n" +
                            " ```sh fetch [-options]... <ids> ...```\n" +
                            "      download <ids>  without installing them\n" +
                            "      for more details, type : ```sh nuts``` help fetch\n" +
                            "            \n" +
                            " ```sh deploy [-options]... <id> ...```\n" +
                            "      deploy <ids>  without installing them\n" +
                            "      for more details, type : ```sh nuts``` help deploy\n" +
                            "            \n" +
                            " ```sh undeploy [-options]... <id> ...```\n" +
                            "      undeploy <ids>\n" +
                            "      for more details, type : ```sh nuts``` help undeploy\n" +
                            "            \n" +
                            " ```sh exec [-options]... [command] <args>...\n" +
                            " ```sh --exec [-options]... [command] <args>...\n" +
                            " ```sh -e [-options]... [command] <args>...\n" +
                            "      run command with the given executor options ( it will be considered an option if it\n" +
                            "      starts with ```sh -``` ). This is helpful to issue JVM Options to executor for instance.\n" +
                            "      for more details, type : ```sh nuts``` help exec\n" +
                            "\n" +
                            " ```sh which [-options]... [command] ...```\n" +
                            "      show command to be executed if run with 'exec' \n" +
                            "      for more details, type : ```sh nuts``` help which\n" +
                            "            \n" +
                            " ```sh --reset [-options]...\n" +
                            "       reset (delete) ```sh nuts``` workspace folder. Will bootstrap a new workspace unless ```sh -K```\n" +
                            "       (```sh --skip-welcome```) option is armed.\n" +
                            "       Actually this is a special command that is available only at boot time.\n" +
                            "       Available command options are :\n" +
                            "       ```sh -y``` : to skip confirmation\n" +
                            "\n" +
                            " ```sh -``` <args>...\n" +
                            "   run a nut's shell (nsh) command with the remaining arguments\n" +
                            "\n" +
                            "#!include</net/thevpc/nuts/includes/standard-options-format.ntf>\n" +
                            "\n" +
                            "##EXAMPLES:##\n" +
                            "```sh\n" +
                            "        nuts help\n" +
                            "```\n" +
                            "            shows this help and exit\n" +
                            "       \n" +
                            "```sh\n" +
                            "        nuts --workspace /home/me/some-folder --archetype=minimal\n" +
                            "```\n" +
                            "            A minimal (####minimal#### archetype) workspace will be created\n" +
                            "            and saved if no workspace was resolved. The workspace handles local \n" +
                            "            packages only\n" +
                            "       \n" +
                            "```sh\n" +
                            "        nuts --workspace /home/me/some-folder  update\n" +
                            "```\n" +
                            "            updates ```sh nuts``` to the very latest version using workspace location\n" +
                            "            /home/me/some-folder.\n" +
                            "\n" +
                            "```sh\n" +
                            "        nuts --workspace /home/me/some-folder  --yes -e -Xmx1G netbeans-launcher\n" +
                            "```\n" +
                            "            run netbeans-launcher with JVM option ```sh -Xmx1G```. If the artifact is not installed\n" +
                            "            it will be automatically installed ( ```sh --yes``` modifier helps disabling interactive mode)\n";
            NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(t_string));
            System.out.println();
            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
                    .setWriteConfiguration(new NutsTextNodeWriteConfiguration().setNumberTitles(true));
            w.writeNode(node);
        }

    }
}
