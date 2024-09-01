/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.*;
import net.thevpc.nuts.runtime.standalone.text.parser.v2.NTFParser2;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author thevpc
 */
public class Test04_NTFTest {
    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        NTexts txt = NTexts.of(session);
        NText parsed = txt.parse("##:error0:n#01##");
        TestUtils.println(parsed);
    }

    @Test
    public void test02() {
        NTexts txt = NTexts.of(session);

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        String str = "##:p2:╭───╮##\u001E\n##:p3:│##";
        NText q = txt.ofStyled(txt.parse(str), NTextStyle.error());
        String qs = q.toString();
        NText q2 = txt.parse(qs);
        q2 = txt.parse(qs);
        TestUtils.println(qs);
        NText parsed = txt.parse("##:error0:n#01##");
        TestUtils.println(parsed);
    }

    @Test
    public void test03() {
        NTexts txt = NTexts.of(session);

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        String str = "##:p2:╭───╮##\u001E\n##:p3:│##";
        NText q = txt.parse(str);
        q = txt.parse(str);
        TestUtils.println(q);
        NText parsed = txt.parse("##:error0:n#01##");
        TestUtils.println(parsed);
    }

    @Test
    public void test04() {
        NTexts txt = NTexts.of(session);

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        //String str="##{error0:##{error0:not installed : ##:p1:ntomcat##\u001E}}##\u001E}##\u001E";
        String str = "##{error0:##{error0:not installed : ##:p1:ntomcat##\u001E}##\u001E}##\u001E";
        NText q2 = txt.parse(str);
        String str2 = q2.toString();
        TestUtils.println(str2);
    }


    @Test
    public void test05() {
        NTexts txt = NTexts.of(session);

        String str = "##:separator0:{##\u001E\n" +
                "  ##:string0:\"id\"##\u001E##:separator0::##\u001E ##:string0:\"<main>://com.github.vatbub:mslinks#1.0.5\"##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"descriptor\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "    ##:string0:\"id\"##\u001E##:separator0::##\u001E ##:string0:\"com.github.vatbub:mslinks#1.0.5\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"parents\"##\u001E##:separator0::##\u001E ##:separator0:[##\u001E\n" +
                "      ##:string0:\"com.github.vatbub:parentPom#0.0.25\"##\u001E\n" +
                "    ##:separator0:]##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"packaging\"##\u001E##:separator0::##\u001E ##:string0:\"jar\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"executable\"##\u001E##:separator0::##\u001E ##:keyword0:true##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"name\"##\u001E##:separator0::##\u001E ##:string0:\"mslinks\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"description\"##\u001E##:separator0::##\u001E ##:string0:\"Java library for parsing and creating Windows shortcut files\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"platform\"##\u001E##:separator0::##\u001E ##:separator0:[##\u001E\n" +
                "      ##:string0:\"java\"##\u001E\n" +
                "    ##:separator0:]##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"properties\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "      ##:string0:\"project.build.sourceEncoding\"##\u001E##:separator0::##\u001E ##:string0:\"UTF-8\"##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"gitRepoName\"##\u001E##:separator0::##\u001E ##:string0:\"mslinks\"##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"maven.build.timestamp.format\"##\u001E##:separator0::##\u001E ##:string0:\"yyyyMMddHHmmss\"##\u001E\n" +
                "    ##:separator0:}##\u001E\n" +
                "  ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"repositoryUuid\"##\u001E##:separator0::##\u001E ##:string0:\"<main>\"##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"repositoryName\"##\u001E##:separator0::##\u001E ##:string0:\"<main>\"##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"installInformation\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "    ##:string0:\"id\"##\u001E##:separator0::##\u001E ##:string0:\"maven-local://com.github.vatbub:mslinks#1.0.5\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"installStatus\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "      ##:string0:\"installed\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"required\"##\u001E##:separator0::##\u001E ##:keyword0:true##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"obsolete\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"defaultVersion\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E\n" +
                "    ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"wasInstalled\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"wasRequired\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"lasModifiedDate\"##\u001E##:separator0::##\u001E ##:string0:\"2021-07-26T23:31:51.845Z\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"createdDate\"##\u001E##:separator0::##\u001E ##:string0:\"2021-07-26T23:31:51.845Z\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"installUser\"##\u001E##:separator0::##\u001E ##:string0:\"admin\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"installFolder\"##\u001E##:separator0::##\u001E ##:string0:\"/home/me/.local/share/nuts/apps/default-workspace/id/com/github/vatbub/mslinks/1.0.5\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justInstalled\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justRequired\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E\n" +
                "  ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"type\"##\u001E##:separator0::##\u001E ##:string0:\"REGULAR\"##\u001E\n" +
                "##:separator0:}##\u001E";
        NText q2 = txt.parse(str);
        String str2 = q2.toString();
        TestUtils.println(str2);
    }

    @Test
    public void test06() {
        NTexts txt = NTexts.of(session);

        String str = "##:separator0:{##\u001E\n" +
                "  ##:string0:\"id\"##\u001E##:separator0::##\u001E ##{string0:\"<main>://com.github.vatbub:mslinks#1.0.5\"}##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"descriptor\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "    ##:string0:\"id\"##\u001E##:separator0::##\u001E ##{string0:\"com.github.vatbub:mslinks#1.0.5\"}##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"parents\"##\u001E##:separator0::##\u001E ##:separator0:[##\u001E\n" +
                "      ##{string0:\"com.github.vatbub:parentPom#0.0.25\"}##\u001E\n" +
                "    ##:separator0:]##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"packaging\"##\u001E##:separator0::##\u001E ##:string0:\"jar\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"executable\"##\u001E##:separator0::##\u001E ##:keyword0:true##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"name\"##\u001E##:separator0::##\u001E ##:string0:\"mslinks\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"description\"##\u001E##:separator0::##\u001E ##:string0:\"Java library for parsing and creating Windows shortcut files\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"platform\"##\u001E##:separator0::##\u001E ##:separator0:[##\u001E\n" +
                "      ##:string0:\"java\"##\u001E\n" +
                "    ##:separator0:]##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"properties\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "      ##:string0:\"project.build.sourceEncoding\"##\u001E##:separator0::##\u001E ##:string0:\"UTF-8\"##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"gitRepoName\"##\u001E##:separator0::##\u001E ##:string0:\"mslinks\"##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"maven.build.timestamp.format\"##\u001E##:separator0::##\u001E ##:string0:\"yyyyMMddHHmmss\"##\u001E\n" +
                "    ##:separator0:}##\u001E\n" +
                "  ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"repositoryUuid\"##\u001E##:separator0::##\u001E ##:string0:\"<main>\"##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"repositoryName\"##\u001E##:separator0::##\u001E ##:string0:\"<main>\"##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"installInformation\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "    ##:string0:\"id\"##\u001E##:separator0::##\u001E ##{string0:\"maven-local://com.github.vatbub:mslinks#1.0.5\"}##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"installStatus\"##\u001E##:separator0::##\u001E ##:separator0:{##\u001E\n" +
                "      ##:string0:\"installed\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"required\"##\u001E##:separator0::##\u001E ##:keyword0:true##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"obsolete\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "      ##:string0:\"defaultVersion\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E\n" +
                "    ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"wasInstalled\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"wasRequired\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"lasModifiedDate\"##\u001E##:separator0::##\u001E ##:string0:\"2021-07-27T08:59:44.780Z\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"createdDate\"##\u001E##:separator0::##\u001E ##:string0:\"2021-07-27T08:59:44.780Z\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"installUser\"##\u001E##:separator0::##\u001E ##:string0:\"admin\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"installFolder\"##\u001E##:separator0::##\u001E ##:string0:\"/home/me/.local/share/nuts/apps/default-workspace/id/com/github/vatbub/mslinks/1.0.5\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justInstalled\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justRequired\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E\n" +
                "  ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"type\"##\u001E##:separator0::##\u001E ##:string0:\"REGULAR\"##\u001E\n" +
                "##:separator0:}##\u001E";
        NText q2 = txt.parse(str);
        String str2 = q2.toString();
        TestUtils.println(str2);
    }

    @Test
    public void test07() {
        NTexts txt = NTexts.of(session);

//        String str = "##{string0:\"<main>://com.github.vatbub:mslinks#1.0.5\"}##";
        String str = "##{string:a#b}##";
        NText q2 = txt.parse(str);
        String str2 = q2.toString();
        TestUtils.println(str2);
    }


    @Test
    public void test08() {
        NTexts txt = NTexts.of(session);

//        String str = "##{string0:\"<main>://com.github.vatbub:mslinks#1.0.5\"}##";
        String str = "##:string:\"a#b\"##";
        NText q2 = txt.parse(str);
        String str2 = q2.toString();
        TestUtils.println(str2);
    }

    @Test
    public void test09() {
        NTexts text = NTexts.of(session);
        NText q = text.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined()));
        TestUtils.println(q.toString());
    }


    //    @Test
//    public void test0(){
//        String msg="[[-t]] or [[--trace]] : enable trace operation with a meaning message ( or disable it with [[--!trace]] or [[--trace=false]] )";
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

    private static void writeColors(String s) {
        TestUtils.println(s);
        NSession session = TestUtils.openNewTestWorkspace("--verbose", "--install-companions=false");
        {
            session.out().println(s);
        }
    }

//    @Test
//    public void test2() {
////        String msg="x{{\\?}}x";
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        PrintStream out = new PrintStream(new FormatOutputStream(System.out, ws));
//        for (String msg : new String[]{
//                "[]", "<>",
//                "\"\"",
//                "''", "{}"
//        }) {
//            for (char c : msg.toCharArray()) {
//                out.print(c);
//            }
//            out.println();
//            TestUtils.println(ws.text().builder().append(msg).filteredText());
//        }
//    }
//
//    @Test
//    public void test3() {
////        String msg="x{{\\?}}x";
//
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        PrintStream out = new PrintStream(new FormatOutputStream(System.out, ws));
//        out.println("#####value             ##### = me");
//    }
//
//    @Test
//    public void test4() {
////        String msg="x{{\\?}}x";
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
////        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
//                .setWriteConfiguration(new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//        NutsTextNodeWriterStringer w2 = new NutsTextNodeWriterStringer(System.out, ws);
////        String text = "[#tet] hello == \\= me\n";
//        String text = "\n##) njob" +
//                "\n##) njob" +
//                "\n###) njob" +
//                "\n####) njob" +
//                "\n#####) njob" +
//                "\n######) njob" +
//                "\n#######) njob" +
//                "\n########) njob" +
//                "\n#########) njob" +
//                "\n##########) njob" +
//                "";
//        TestUtils.println(text);
//        TestUtils.println("\n--------------------------------");
//        NutsTextNodeParser parser = new DefaultNTextNodeParser(ws);
//        NutsText node = parser.parse(new StringReader(text));
//        TestUtils.println(node);
//        TestUtils.println("\n--------------------------------");
//        w.writeNode(node);
//        TestUtils.println("\n--------------------------------");
//        w2.writeNode(node, new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//
////        ByteArrayOutputStream bos = new ByteArrayOutputStream();
////        PrintStream out = new PrintStream(new FormatOutputStream(bos));
////        out.println("[#tet] hello == \\= me");
////        TestUtils.println("as a result :: " + new String(bos.toByteArray()));
//    }
//
//    @Test
//    public void test5() {
////        String msg="x{{\\?}}x";
//
////        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
//                .setWriteConfiguration(new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//        NutsTextNodeWriterStringer w2 = new NutsTextNodeWriterStringer(System.out, ws);
////        String text = "[#tet] hello == \\= me\n";
//        String text =
//                "\n 1 ## text ##" +
//                        "\n 2 ### text ###" +
//                        "\n 3 #### text ####" +
//                        "\n 4 ##### text #####" +
//                        "\n 5 ###### text ######" +
//                        "\n 6 ####### text #######" +
//                        "\n 7 ######## text ########" +
//                        "\n 8 ######### text #########" +
//                        "\n 9 ########## text ##########" +
//                        "\n" +
//                        "\n 1 ```@@ text @@``` @@ text @@" +
//                        "\n 2 ```@@@ text @@@``` @@@ text @@@" +
//                        "\n 3 ```@@@@ text @@@@``` @@@@ text @@@@" +
//                        "\n 4 ```@@@@@ text @@@@@``` @@@@@ text @@@@@" +
//                        "\n 5 ```@@@@@@ text @@@@@@``` @@@@@@ text @@@@@@" +
//                        "\n 6 ```@@@@@@@ text @@@@@@@``` @@@@@@@ text @@@@@@@" +
//                        "\n 7 ```@@@@@@@@ text @@@@@@@@``` @@@@@@@@ text @@@@@@@@" +
//                        "\n 8 ```@@@@@@@@@ text @@@@@@@@@``` @@@@@@@@@ text @@@@@@@@@" +
//                        "\n 9 ```@@@@@@@@@@ text @@@@@@@@@@``` @@@@@@@@@@ text @@@@@@@@@@" +
//                        "\n" +
//                        "\n 1 ~~ text ~~" +
//                        "\n 2 ~~~ text ~~~" +
//                        "\n 3 ~~~~ text ~~~~" +
//                        "\n 4 ~~~~~ text ~~~~~" +
//                        "\n" +
//                        "\n ##\u001E###hello###me##" +
//                        "\n ## ###hello### me##" +
//                        "";
//        TestUtils.println(text);
//        TestUtils.println("\n--------------------------------");
//        NutsTextNodeParser parser = new DefaultNTextNodeParser(ws);
//        NutsText node = parser.parse(new StringReader(text));
//        TestUtils.println(node);
//        TestUtils.println("\n--------------------------------");
//        w.writeNode(node);
//        TestUtils.println("\n--------------------------------");
//        w2.writeNode(node, new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//
////        ByteArrayOutputStream bos = new ByteArrayOutputStream();
////        PrintStream out = new PrintStream(new FormatOutputStream(bos));
////        out.println("[#tet] hello == \\= me");
////        TestUtils.println("as a result :: " + new String(bos.toByteArray()));
//    }
//
//    @Test
//    public void test6() {
////        String msg="x{{\\?}}x";
//
////        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
//                .setWriteConfiguration(new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//        NutsTextNodeWriterStringer w2 = new NutsTextNodeWriterStringer(System.out, ws);
////        String text = "[#tet] hello == \\= me\n";
//        String text = "unable to create system terminal : %s";
//        TestUtils.println(text);
//        TestUtils.println("\n--------------------------------");
//        NutsTextNodeParser parser = new DefaultNTextNodeParser(ws);
//        NutsText node = parser.parse(new StringReader(text));
//        TestUtils.println(node);
//        TestUtils.println("\n--------------------------------");
//        w.writeNode(node);
//        TestUtils.println("\n--------------------------------");
//        w2.writeNode(node, new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//
////        ByteArrayOutputStream bos = new ByteArrayOutputStream();
////        PrintStream out = new PrintStream(new FormatOutputStream(bos));
////        out.println("[#tet] hello == \\= me");
////        TestUtils.println("as a result :: " + new String(bos.toByteArray()));
//    }
//
//    @Test
//    public void test7() {
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        String t_colors = CoreIOUtils.loadString(getClass().getResourceAsStream(
//                "/net/thevpc/nuts/runtime/ntf-help.ntf"
//        ), true);
//        writeColors(t_colors);
//    }
//
//    @Test
//    public void test8() {
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        {
//            String t_string = "###\u001Eaa\u001E###";
//            writeColors(t_string);
//        }
//        {
//            String t_string = "####aa####";
//            writeColors(t_string);
//        }
//        {
//            String t_string = "###\u001E####aa####\u001E###";
//            writeColors(t_string);
//        }
//        {
//            String t_string = "###\u001E####aa####\u001E###";
//            NutsText node = new DefaultNTextNodeParser(ws).parse(new StringReader(t_string));
//            TestUtils.println();
//            NutsTextNodeWriter w = new NutsTextNodeWriterStringer(System.out, ws)
//                    .setWriteConfiguration(new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//            Assertions.assertTrue(
//                    node instanceof NutsTextStyled
//            );
//            Assertions.assertTrue(
//                    ((NutsTextStyled) node).getChild() instanceof NutsTextStyled
//            );
//            Assertions.assertTrue(
//                    ((NutsTextStyled) (((NutsTextStyled) node).getChild())).getChild() instanceof NutsTextPlain
//            );
//            w.writeNode(node);
//        }
//    }
//
//    @Test
//    public void test9() {
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        {
//            String t_string = "##) aa\n" +
//                    "hello";
//            writeColors(t_string);
//        }
//
//    }
//
//    @Test
//    public void test10() {
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        {
//            String t_string =
//                    "${workspace}\n" +
//                            "\n" +
//                            "##)SYNOPSIS\n" +
//                            "```sh\n" +
//                            "nuts [<options>]... <command> <args> ...\n" +
//                            "```\n" +
//                            "For Help, type ```sh nuts help```\n" +
//                            "\n" +
//                            "Welcome to ```sh nuts```. Yeah, it is ###working###...";
//            writeColors(t_string);
//        }
//    }
//
//    @Test
//    public void test11() {
//        String t_string =
//                "\u001E\u001E\u001E\u001E\u001E\u001Eheelo\u001E\u001E";
//        writeColors(t_string);
//
//    }
//
//    @Test
//    public void test12() {
//        String t_string =
//                "hello ```!later-reset-line```";
//        writeColors(t_string);
//    }
//
//    @Test
//    public void test13() {
//        String t_string =
//                " ```sh install [-options]... <nuts-artifact>... <args> ...```\n" +
//                        "      install ```sh nuts``` package <nuts-artifact>\n" +
//                        "      for more details, type : ```sh nuts``` help install\n";
//        writeColors(t_string);
//    }
//
//    @Test
//    public void test14() {
//        String t_string =
//                " ```sh license [-options]...```\n" +
//                        "      show license info and exit\n" +
//                        "      for more details, type : ```sh nuts``` help info\n" +
//                        "\n" +
//                        " ```sh install [-options]... <nuts-artifact>... <args> ...```\n" +
//                        "      install ```sh nuts``` package <nuts-artifact>\n" +
//                        "      for more details, type : ```sh nuts``` help install\n" +
//                        "\n" +
//                        " ```sh uninstall [-options]... <nuts-artifact>... <args> ...```\n" +
//                        "      uninstall  ```sh nuts``` package <nuts-artifact>\n" +
//                        "      for more details, type : ```sh nuts``` help uninstall\n" +
//                        "\n" +
//                        " ```sh update [-options]... <ids> ...  <args> ...```\n" +
//                        "      check if a newer version of ```sh nuts``` or any of the provided <ids>\n" +
//                        "      is available in the current workspace and perform update by downloading (fetch)\n" +
//                        "      and installing the artifact. The updated version is promoted to 'default' version.\n" +
//                        "      for more details, type : ```sh nuts``` help update\n" +
//                        "            \n" +
//                        " ```sh check-updates} [-options]... <ids> ...```\n" +
//                        "      check if a newer version of ```sh nuts``` is available in the current workspace without performing updates\n" +
//                        "      Takes the same arguments and options as ```sh update``` command\n" +
//                        "      for more details, type : ```sh nuts``` help check-updates\n" +
//                        "\n" +
//                        " ```sh search [-options]... <ids> ...```\n" +
//                        "      search for <ids>\n" +
//                        "      for more details, type : ```sh nuts``` help search\n" +
//                        "            \n" +
//                        " ```sh fetch [-options]... <ids> ...```\n" +
//                        "      download <ids>  without installing them\n" +
//                        "      for more details, type : ```sh nuts``` help fetch\n" +
//                        "            \n" +
//                        " ```sh deploy [-options]... <id> ...```\n" +
//                        "      deploy <ids>  without installing them\n" +
//                        "      for more details, type : ```sh nuts``` help deploy\n" +
//                        "            \n" +
//                        " ```sh undeploy [-options]... <id> ...```\n" +
//                        "      undeploy <ids>\n" +
//                        "      for more details, type : ```sh nuts``` help undeploy\n" +
//                        "            \n" +
//                        " ```sh exec [-options]... [command] <args>...```\n" +
//                        " ```sh --exec [-options]... [command] <args>...```\n" +
//                        " ```sh -e [-options]... [command] <args>...```\n" +
//                        "      run command with the given executor options ( it will be considered an option if it\n" +
//                        "      starts with ```sh -``` ). This is helpful to issue JVM Options to executor for instance.\n" +
//                        "      for more details, type : ```sh nuts``` help exec\n" +
//                        "\n" +
//                        " ```sh which [-options]... [command] ...```\n" +
//                        "      show command to be executed if run with 'exec' \n" +
//                        "      for more details, type : ```sh nuts``` help which\n" +
//                        "            \n" +
//                        " ```sh --reset [-options]...```\n" +
//                        "       reset (delete) ```sh nuts``` workspace folder. Will bootstrap a new workspace unless ```sh -K```\n" +
//                        "       (```sh --skip-welcome```) option is armed.\n" +
//                        "       Actually this is a special command that is available only at boot time.\n" +
//                        "       Available command options are :\n" +
//                        "       ```sh -y``` : to skip confirmation\n" +
//                        "\n" +
//                        " ```sh - <args>...```\n" +
//                        "   run a nut's shell (nsh) command with the remaining arguments\n" +
//                        "\n" +
//                        "#!include</net/thevpc/nuts/runtime/includes/standard-options-format.ntf>\n" +
//                        "\n" +
//                        "##EXAMPLES:##\n" +
//                        "```sh\n" +
//                        "        nuts help\n" +
//                        "```\n" +
//                        "            shows this help and exit\n" +
//                        "       \n" +
//                        "```sh\n" +
//                        "        nuts --workspace /home/me/some-folder --archetype=minimal\n" +
//                        "```\n" +
//                        "            A minimal (####minimal#### archetype) workspace will be created\n" +
//                        "            and saved if no workspace was resolved. The workspace handles local \n" +
//                        "            packages only\n" +
//                        "       \n" +
//                        "```sh\n" +
//                        "        nuts --workspace /home/me/some-folder  update\n" +
//                        "```\n" +
//                        "            updates ```sh nuts``` to the very latest version using workspace location\n" +
//                        "            /home/me/some-folder.\n" +
//                        "\n" +
//                        "```sh\n" +
//                        "        nuts --workspace /home/me/some-folder  --yes -e -Xmx1G netbeans-launcher\n" +
//                        "```\n" +
//                        "            run netbeans-launcher with JVM option ```sh -Xmx1G```. If the artifact is not installed\n" +
//                        "            it will be automatically installed ( ```sh --yes``` modifier helps disabling interactive mode)\n";
//        writeColors(t_string);
//    }
//
//    @Test
//    public void test15() {
//        String t_string =
//                " ```sh \"hello\" <hello> <-hello> -hello [-hello] [<-hello>] \"world\"  'I am here' `where are you`? ```\n";
//        writeColors(t_string);
//
//    }
//
//    @Test
//    public void test16() {
//        String t_string =
//                " ```sh \"$HOME/AppData/Roaming/nuts/apps\" $HOME/AppData/Roaming/nuts/apps \"${HOME:dd}/AppData/Roaming/nuts/apps\"```\n";
//        writeColors(t_string);
//    }
//
//    @Test
//    public void test17() {
//        String t_string =
//                "###\\####";
//        writeColors(t_string);
//    }
//
//    @Test
//    public void test18() {
//        String t_string =
//                "```Text```";
//        writeColors(t_string);
//    }
//
//    @Test
//    public void test19() {
//        String t_string =
//                "```sh ```";
//        writeColors(t_string);
//    }
//
//
//    @Test
//    public void test20() {
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        {
//            String t_string =
//                    "##)Hello you:\n how are you" +
//                            "\n##)Hello you:\n how are you" +
//                            "\n\n##) Hello you:\n how are you";
//            writeColors(t_string);
//        }
//    }
//
//    @Test
//    public void test21() {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 1; i <= 255; i++) {
//            sb.append("##:+" + i + " hello##\n");
//        }
//        for (int i = 1; i <= 255; i++) {
//            sb.append("##:+s" + i + " hello##\n");
//        }
//        writeColors(sb.toString());
//    }
//
//    @Test
//    public void test22() {
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        {
//            String sb = "##:p1:njob##\u001E";
//            writeColors(sb);
//        }
//    }
//
//    @Test
//    public void test23() {
//
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        {
//            for (int i = 0; i < 255; i++) {
//                String sb = "##:" + i + ":foreground " + i + "## " + "##:s" + i + ":background " + i + "\n##";
//                writeColors(sb);
//            }
////            ByteArrayOutputStream bout=new ByteArrayOutputStream();
////            w = new NutsTextNodeWriterRenderer(bout, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
////                    .setWriteConfiguration(new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
////            w.writeNode(text);
////            TestUtils.println(bout);
//        }
//    }
//
//    @Test
//    public void test24() {
//
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        {
//            NutsText command = ws.text().command(NutsTerminals.CMD_LATER_RESET_LINE);
//            NutsTextNodeWriter w = new NutsTextNodeWriterRenderer(System.out, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
//                    .setWriteConfiguration(new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
//            w.writeNode(command);
//
//            //            ByteArrayOutputStream bout=new ByteArrayOutputStream();
////            w = new NutsTextNodeWriterRenderer(bout, AnsiUnixTermPrintRenderer.ANSI_RENDERER, ws)
////                    .setWriteConfiguration(new NutsTextWriteConfiguration().setTitleNumberEnabled(true));
////            w.writeNode(text);
////            TestUtils.println(bout);
//        }
//    }
//
//    @Test
//    public void test25() {
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace();
//        NutsSession session = ws.createSession();
//        session.out().print("Hi");
//        ws.io().term().sendCommand(session.out(), NutsTerminals.CMD_LATER_RESET_LINE);
//        session.out().print("Bye");
//    }
//
//    @Test
//    public void test26() {
//
//        String s = "##:foregroundxd787af:Text##";
//        writeColors(s);
//    }
//
//    @Test
//    public void test27() {
//        String s;
//        s = "##{s12:AA##:12:BB##\u001E##:6:CC##DD}##";
//        writeColors(s);
//        TestUtils.println();
//        s = "##{s12:AA#}##";
//        writeColors(s);
//        TestUtils.println();
//    }
//
//    @Test
//    public void test28() {
//
//        String s = "#a";
//        writeColors(s);
//    }
//
//    @Test
//    public void test29() {
//        String s = "##:027:     _\\_        _\\_       ##";
//        writeColors(s);
//    }
//
//    @Test
//    public void test30() {
////        writeColors("##)Hello");
////        writeColors("##)Hello");
////        writeColors("\n##)Hello");
//        writeColors("```!include classpath:/net/thevpc/nuts/runtime/includes/standard-header.ntf```\n" +
//                "\n" +
//                "```sh ntf``` aka ##nuts text format## is a markdown like text format enabling colored text styles.\n" +
//                "```sh ntf``` is the standard format used in the standard output and standard error. It is as\n" +
//                " well the standard format used in help manuals.\n" +
//                "\n" +
//                "type (```sh nuts help --colors```) to display this help\n" +
//                "\n" +
//                "##) NTF special characters:\n" +
//                "### \\# \\\u001E \\\\ \\' ### are special characters\n" +
//                "### : { and } ### are also spacial character inside \\# styles\n" +
//                "###\\#### defines primary styles\n" +
//                "###\\\u001E### is a 'nop' character. it is used as a separator when required. It is not displayed.\n" +
//                "###\\`\\`\\`Text\\`\\`\\`### defines verbatim text\n" +
//                "###\\`\\`\\`lang code-bloc\\`\\`\\`### defines formatted code in the given language/format (such as sh, java, json, ...)\n" +
//                "###\\\\### You can escape special characters using ###\\\\### character\n" +
//                "\n" +
//                "\n" +
//                "##) NTF COLORS:\n" +
//                "nuts text format is based on the following constructs:\n" +
//                "\n" +
//                "##Format##                                   | ##Display##                       | ##Description##\n" +
//                "----------------------------------------------------------------------------------------------------\n" +
//                "Text ```#Text#```                              | Text #Text#                   | plain text\n" +
//                "```##Text##```                                 | ##Text##                          | primary1 = title 1\n" +
//                "```###Text###```                               | ###Text###                          | primary2 = title 2\n" +
//                "```####Text####```                             | ####Text####                          | primary3 = title 3\n" +
//                "```#####Text#####```                           | #####Text#####                          | primary4 = title 4\n" +
//                "```######Text######```                         | ######Text######                          | primary5 = title 5\n" +
//                "```#######Text#######```                       | #######Text#######                          | primary6 = title 6\n" +
//                "```########Text########```                     | ########Text########                          | primary7 = title 7\n" +
//                "```#########Text#########```                   | #########Text#########                          | primary8 = title 8\n" +
//                "```##########Text##########```                 | ##########Text##########                          | primary9 = title 9\n" +
//                "```##:1:Text##```  ```##:p1:Text##```  ```##:s1:Text##```  | ##:1:Text##  ##:1:Text## ##:s1:Text##               | primary 1\n" +
//                "```##:2:Text##```  ```##:p2:Text##```  ```##:s2:Text##```  | ##:2:Text##  ##:2:Text## ##:s2:Text##               | primary 2\n" +
//                "```##:3:Text##```  ```##:p3:Text##```  ```##:s3:Text##```  | ##:3:Text##  ##:3:Text## ##:s3:Text##               | primary 3\n" +
//                "```##:4:Text##```  ```##:p4:Text##```  ```##:s4:Text##```  | ##:4:Text##  ##:4:Text## ##:s4:Text##               | primary 4\n" +
//                "```##:5:Text##```  ```##:p5:Text##```  ```##:s5:Text##```  | ##:5:Text##  ##:5:Text## ##:s5:Text##               | primary 5\n" +
//                "```##:6:Text##```  ```##:p6:Text##```  ```##:s6:Text##```  | ##:6:Text##  ##:6:Text## ##:s6:Text##               | primary 6\n" +
//                "```##:7:Text##```  ```##:p7:Text##```  ```##:s7:Text##```  | ##:7:Text##  ##:7:Text## ##:s7:Text##               | primary 7\n" +
//                "```##:8:Text##```  ```##:p8:Text##```  ```##:s8:Text##```  | ##:8:Text##  ##:8:Text## ##:s8:Text##               | primary 8\n" +
//                "```##:9:Text##```  ```##:p9:Text##```  ```##:s9:Text##```  | ##:9:Text##  ##:9:Text## ##:s9:Text##               | primary 9\n" +
//                "```##:10:Text##``` ```##:p10:Text##``` ```##:s10:Text##``` | ##:10:Text##  ##:10:Text## ##:s10:Text##               | primary 10\n" +
//                "```##:11:Text##``` ```##:p11:Text##``` ```##:s11:Text##``` | ##:11:Text##  ##:11:Text## ##:s11:Text##               | primary 11\n" +
//                "```##:12:Text##``` ```##:p12:Text##``` ```##:s12:Text##``` | ##:12:Text##  ##:12:Text## ##:s12:Text##               | primary 12\n" +
//                "```##:13:Text##``` ```##:p13:Text##``` ```##:s13:Text##``` | ##:13:Text##  ##:13:Text## ##:s13:Text##               | primary 13\n" +
//                "```##:14:Text##``` ```##:p14:Text##``` ```##:s14:Text##``` | ##:14:Text##  ##:14:Text## ##:s14:Text##               | primary 14\n" +
//                "```##:15:Text##``` ```##:p15:Text##``` ```##:s15:Text##``` | ##:15:Text##  ##:15:Text## ##:s15:Text##               | primary 15\n" +
//                "```##:/:Text##``` ```##:_:Text##``` ```##:%:Text##```      | ##:/:Text##  ##:_:Text## ##:%:Text##               | italic, underlined, blink\n" +
//                "```##:!:Text##``` ```##:+:Text##```                  | ##:!:Text## ##:+:Text##                     | reversed, bold\n" +
//                "```##:primary3:Text##```                       | ##:primary3:Text##                          | primary3\n" +
//                "```##:secondary5:Text##```                     | ##:s4:Text##                          | secondary5\n" +
//                "```##{s12:AA##:12:BB##\u001E##:6:CC##DD}##```       | ##{s12:AA##:12:BB##\u001E##:6:CC##DD}##                      | composed colors, note the \\\u001E separator\n" +
//                "```##:f158:AA## ##:f58:BB## ##:f201:CC##```    | ##:f158:AA## ##:f58:BB## ##:f201:CC##                      | foreground 8bits colors\n" +
//                "```##:foreground158:Text##```                  | ##:foreground158:Text##                          | foreground 158 (8bits)\n" +
//                "```##:fxd787af:Text##```                       | ##:fxd787af:Text##                          | foreground Pink (d787af in 24bits)\n" +
//                "```##:foregroundxd787af:Text##```              | ##:foregroundxd787af:Text##                          | foreground Pink (d787af in 24bits)\n" +
//                "```##:b158:Text##```                           | ##:b158:Text##                          | background 158 (8bits)\n" +
//                "```##:bxd787af:Text##```                       | ##:bxd787af:Text##                | background Pink (24bits)\n" +
//                "\n" +
//                "##) NTF SPECIAL FORMATS:\n" +
//                "\n" +
//                "##Format##                                   | ##Display##                       | ##Description##\n" +
//                "----------------------------------------------------------------------------------------------------\n" +
//                "\\\\`\\\\#\\\\\u001E                                    | \\`\\#\\\u001E                           | escaped characters\n" +
//                "\\`\\`\\` Text with # escaped\\`\\`\\`               | ``` Text with # escaped```           | escaped text (note the starting space)\n" +
//                "\\`\\`\\`underlined underlined\\`\\`\\`              | ```underlined underlined```                    | underlined\n" +
//                "\\`\\`\\`italic italic\\`\\`\\`                      | ```italic italic```                        | italic\n" +
//                "\\`\\`\\`striked striked\\`\\`\\`                    | ```striked striked```                       | striked\n" +
//                "\\`\\`\\`reversed reversed\\`\\`\\`                  | ```reversed reversed```                      | reversed\n" +
//                "\\`\\`\\`error  error\\`\\`\\` \\`\\`\\`warn  warn\\`\\`\\`  ... | ```error  error``` ```warn  warn``` ```info  info```  ```config  config```       | several token types\n" +
//                "...                                      | ```comments  comments``` ```string  string``` ```number  number``` ```boolean  boolean```|\n" +
//                "                                         | ```keyword  keyword``` ```option  option``` ```input  input```     |\n" +
//                "                                         | ```operator  operator``` ```separator  separator``` ```success  success```    |\n" +
//                "                                         | ```danger  danger``` ```fail  fail``` ```var  var``` ```pale  pale```          |\n" +
//                "\n" +
//                "##) NTF TITLES:\n" +
//                "\n" +
//                "#) Title 1\n" +
//                "```#) Title 1```\n" +
//                "##) Title 2\n" +
//                "```##) Title 2```\n" +
//                "###) Title 3\n" +
//                "```###) Title 3```\n" +
//                "####) Title 4\n" +
//                "```####) Title 4```\n" +
//                "#####) Title 5\n" +
//                "```#####) Title 5```\n" +
//                "######) Title 6\n" +
//                "```######) Title 6```\n" +
//                "#######) Title 7\n" +
//                "```#######) Title 7```\n" +
//                "########) Title 8\n" +
//                "```########) Title 8```\n" +
//                "#########) Title 9\n" +
//                "```#########) Title 9```\n" +
//                "\n" +
//                "\n" +
//                "##) NTF SYNTAX COLORING:\n" +
//                "\n" +
//                "###) Xml format\n" +
//                "####) NTF syntax\n" +
//                "####) NTF syntax\n" +
//                "\\`\\`\\`xml <xml n='value'></xml>\\`\\`\\`\n" +
//                "####) Coloring Result\n" +
//                "```xml <xml n='value'></xml>```\n" +
//                "\n" +
//                "###) Json format\n" +
//                "####) NTF syntax\n" +
//                "\\`\\`\\`json {k:'value',n:'value'}\\`\\`\\`\n" +
//                "####) Coloring Result\n" +
//                "```json {k:'value',n:'value'}```\n" +
//                "\n" +
//                "###) Java format\n" +
//                "####) NTF syntax\n" +
//                "\\`\\`\\`java\n" +
//                "public class A{\n" +
//                "    int a=12;\n" +
//                "}\n" +
//                "\\`\\`\\`\n" +
//                "####) Coloring Result\n" +
//                "```java\n" +
//                "public class A{\n" +
//                "    int a=12;\n" +
//                "}\n" +
//                "```\n" +
//                "\n" +
//                "###) Shell commandline format\n" +
//                "####) NTF syntax\n" +
//                "\\`\\`\\`sh cmd arg user -option=true\\`\\`\\`\n" +
//                "####) Coloring Result\n" +
//                "```sh cmd arg user -option=true```\n" +
//                "\n");
//
//    }
//
//
//    @Test
//    public void test31() {
//        String s =
//                "\n##) njob"
//                +"\n###) njob"
//                ;
//        writeColors(s);
//    }

    @Test
    public void test10() {
        String s =
                "\\`\\`\\`xml <xml n='value'></xml>\\`\\`\\`\n" +
                        "####) Coloring Result\n" +
//"```xml <xml n='value'></xml>```\n" +
//"\n" +
//"###) Json format\n" +
//"####) NTF syntax\n" +
//"\\`\\`\\`json {k:'value',n:'value'}\\`\\`\\`\n" +
//"####) Coloring Result\n" +
//"```json {k:'value',n:'value'}```\n" +
//"\n" +
//"###) Java format\n" +
//"####) NTF syntax\n" +
//"\\`\\`\\`java\n" +
//"public class A{\n" +
//"    int a=12;\n" +
//"}\n" +
//"\\`\\`\\`\n" +
//"####) Coloring Result\n" +
//"```java\n" +
//"public class A{\n" +
//"    int a=12;\n" +
//"}\n" +
//"```\n" +
//"\n" +
//"###) Shell commandline format\n" +
//"####) NTF syntax\n" +
//"\\`\\`\\`sh cmd arg user -option=true\\`\\`\\`\n" +
//"####) Coloring Result\n" +
//"```sh cmd arg user -option=true```\n" +
//"\n" +
                        "";
        writeColors(s);
    }

    @Test
    public void test11() {
        NTexts text = NTexts.of(session);
        NText str = text.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined()));
        TestUtils.println(str.toString());
        Assertions.assertEquals("##{_,success:re-install}##\u001E", str.toString());
    }

    @Test
    public void test12() {
        NTexts text = NTexts.of(session);
        String str = "```system \"C:\\U\\v\" ```";
        NText t = text.parse(str);
        Assertions.assertEquals("\"C:\\U\\v\" ", t.filteredText());
    }

    @Test
    public void test13() {
        NTexts text = NTexts.of(session);
        String str = "```system \\``````";
        NText t = text.parse(str);
        Assertions.assertEquals("```", t.filteredText());
    }
    //////////////////


    @Test
    public void test14() {
        NTFParser2 p = createParser();
        p.offer("Hello World");
        NText r = p.read();
        Assertions.assertEquals(NTextType.PLAIN, r.getType());
        Assertions.assertEquals("Hello World", r.toString());
    }

    private NTFParser2 createParser() {
        return new NTFParser2(session);
    }

    @Test
    public void test15() {
        NTFParser2 p = createParser();
        String s = "Hello #World#";
        p.offer(s);
        NText r = p.read();
        Assertions.assertNull(r);
        r = p.readFully();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.PLAIN, r.getType());
        Assertions.assertEquals("Hello #World\\#", r.toString());
    }

    @Test
    public void test16() {
        NTFParser2 p = createParser();
        String s = "#) Hello #World\\#";
        p.offer(s);
        NText r = p.read();
        Assertions.assertNull(r);
        r = p.readFully();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.TITLE, r.getType());
        Assertions.assertEquals("#) Hello #World\\#\n", r.toString());
    }

    @Test
    public void test17() {
        for (int i = 1; i < 10; i++) {
            String q = CoreStringUtils.fillString('#', i);
            NTFParser2 p = createParser();
            String s = q + ") Hello #World#";
            p.offer(s);
            NText r = p.read();
            Assertions.assertNull(r);
            r = p.readFully();
            Assertions.assertNotNull(r);
            Assertions.assertEquals(NTextType.TITLE, r.getType());
            Assertions.assertEquals(i, ((NTextTitle) r).getLevel());
            Assertions.assertEquals(q + ") Hello #World\\#\n", r.toString());
        }
    }

    @Test
    public void test18() {
        for (int i = 1; i < 10; i++) {
            String q = CoreStringUtils.fillString('#', i + 1);
            NTFParser2 p = createParser();
            String s = q + "Hello #World# " + q;
            TestUtils.println(s);
            p.offer(s);
            NText r = p.read();
            Assertions.assertNull(r);
            r = p.readFully();
            Assertions.assertNotNull(r);
            Assertions.assertEquals(NTextType.STYLED, r.getType());
//            Assertions.assertEquals(q + " Hello #World\\# " + q, r.toString());
            Assertions.assertEquals("##{p" + i + ":Hello #World# }##\u001E", r.toString());
        }
    }

    @Test
    public void test19() {
        NTFParser2 p = createParser();
        String s = "```!anchor some anchor```";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.ANCHOR, r.getType());
        Assertions.assertEquals("```!anchor some anchor```", r.toString());
    }

    @Test
    public void test20() {
        NTFParser2 p = createParser();
        String s = "```!link some link```";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.LINK, r.getType());
        Assertions.assertEquals("```!link some link```", r.toString());
    }

    @Test
    public void test21() {
        NTFParser2 p = createParser();
        String s = "```!link:some link```";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.LINK, r.getType());
        Assertions.assertEquals("```!link:some link```", r.toString());
    }

    @Test
    public void test22() {
        NTFParser2 p = createParser();
        String s = "```!link\nsome link```";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.LINK, r.getType());
        Assertions.assertEquals("```!link\nsome link```", r.toString());
    }

    @Test
    public void test23() {
        NTFParser2 p = createParser();
        String s = "```java\npublic class \\` some thing \\``` some thing```";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.CODE, r.getType());
        Assertions.assertEquals("public class \\` some thing ``` some thing", ((NTextCode) r).getText());
        Assertions.assertEquals("```java\npublic class \\` some thing \\``` some thing```", r.toString());
    }

    @Test
    public void test24() {
        NTFParser2 p = createParser();
        String s = "##:1:text##";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNull(r);
        r = p.readFully();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.STYLED, r.getType());
        Assertions.assertEquals("##{p1:text}##\u001E", r.toString());
    }

    @Test
    public void test25() {
        for (char c : "/_+%!-".toCharArray()) {
            NTFParser2 p = createParser();
            String s = "##:" + c + ":text##";
            TestUtils.println(s);
            p.offer(s);
            NText r = p.read();
            Assertions.assertNull(r);
            r = p.readFully();
            Assertions.assertNotNull(r);
            Assertions.assertEquals(NTextType.STYLED, r.getType());
            Assertions.assertEquals("##{" + c + ":text}##\u001E", r.toString());
        }
    }

    @Test
    public void test26() {
        for (String v : new String[]{
                "/:italic",
                "_/underlined",
                "%:blink",
                "!:reversed",
                "+:bold",
                "-:striked"
        }) {
            char c = v.charAt(0);
            String rr = v.substring(2);
            NTFParser2 p = createParser();
            String s = "##:" + rr + ":text##";
            TestUtils.println(s);
            p.offer(s);
            NText r = p.read();
            Assertions.assertNull(r);
            r = p.readFully();
            Assertions.assertNotNull(r);
            Assertions.assertEquals(NTextType.STYLED, r.getType());
            Assertions.assertEquals("##{" + c + ":text}##\u001E", r.toString());
        }

        NTFParser2 p = createParser();
        String s = "##:italic:text##";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNull(r);
        r = p.readFully();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.STYLED, r.getType());
        Assertions.assertEquals("##{/:text}##\u001E", r.toString());
    }

    @Test
    public void test27() {
        NTFParser2 p = createParser();
        String s = "##:_:text##";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNull(r);
        r = p.readFully();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.STYLED, r.getType());
        Assertions.assertEquals("##{_:text}##\u001E", r.toString());
    }

    @Test
    public void test28() {
        NTFParser2 p = createParser();
        String s = "```!clear-line```";
        TestUtils.println(s);
        p.offer(s);
        NText r = p.read();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(NTextType.COMMAND, r.getType());
        Assertions.assertEquals("```!clear-line```", r.toString());
    }

    @Test
    public void test29() {
        NTFParser2 p = createParser();
        String s = "##{version:0.8.4}##\u001E";
        TestUtils.println(s);
        p.offer(s);
        NText r1 = p.readFully();
        NText r2 = p.readFully();
        Assertions.assertNotNull(r1);
        Assertions.assertNull(r2);
        Assertions.assertEquals(NTextType.STYLED, r1.getType());
        Assertions.assertEquals("##{version:0.8.4}##\u001E", r1.toString());
    }

    @Test
    public void test30() {
        NTextBuilder b = NTexts.of(session).ofBuilder()
                .append("a")
                .appendCode("sh", "b")
                .append("c");
        Assertions.assertEquals(3, b.textLength());
    }

    @Test
    public void test31() {
        NString s = NString.of("a```sh b```\u001Ec", session);
        NText t = s.toText();
        Assertions.assertEquals(NTextType.LIST, t.getType());
        Assertions.assertEquals(3, ((NTextList) t).size());
    }

    @Test
    public void test32() {
        NString s = NString.of(
                "##:_:location##:##:path:/home## (Tan Type)\n", session);
        NText t = s.toText();
        Assertions.assertEquals(NTextType.LIST, t.getType());
        Assertions.assertEquals(4, ((NTextList) t).size());
    }

    @Test
    public void test33() {
        String q = "###) SYNOPSIS:\n" +
                "```sh\n" +
                "njob tasks | jobs | projects | summary...\n" +
                "```\n" +
                "    manage tasks, jobs, projects\n";
        NText s = NString.of(
                q, session).toText();
        NString im = s.immutable();
        Assertions.assertEquals(NTextType.LIST, s.getType());
        Assertions.assertEquals(q, s.toString());
    }
    @Test
    public void test34() {
        NText s = NString.of(
                "```!clear-line``````!move-line-start```", session).toText();
        Assertions.assertEquals(NTextType.LIST, s.getType());
        Assertions.assertEquals(2, ((NTextList)s).size());
        Assertions.assertEquals(true, ((NTextList)s).get(0) instanceof NTextCmd);
        Assertions.assertEquals(true, ((NTextCmd)((NTextList)s).get(0)).getCommand().getName().equals("clear-line"));
    }
    @Test
    public void test35() {
        String str = "```##{s12:AA##:12:BB##\\u001E##:6:CC##DD}##```       | ##{s12:AA##:12:BB##\\u001E##:6:CC##DD}##                      | composed colors, note the \\\\u001E separator\n" +
                "```##:f158:AA## ##:f58:BB## ##:f201:CC##```    | ##:f158:AA## ##:f58:BB## ##:f201:CC##                      | foreground 8bits colors\n";
        //String str = "##{s12:AA##:12:BB##\\u001E##:6:CC##DD}##";
        //String str = "##{s12:AA##:12:BB##u001E##:6:CC##DD}##";
        //String str = "##{s12:AA##:12:BB##u001E##:6:CC##}##";
        //String str = "##{s12:AA##:12:BB##u}##";
        //String str = "##{s12:AA##:12:BB##u##:12:BB##}##";
        //String str = "##{s1:A##:1:B##C##:1:D##}##";
        //String str = "##{s12:AA##:12:BB##}##";
        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
        Assertions.assertEquals(NTextType.LIST, parsed.getType());
        Assertions.assertEquals(12, ((NTextList)parsed).size());
    }
    @Test
    public void test36() {
        String str = "##:fxd787af:Text##";

        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
//        Assertions.assertEquals(NTextType.LIST, parsed.getType());
//        Assertions.assertEquals(12, ((NTextList)parsed).size());
    }
    @Test
    public void test37() {
        String str = "```##:primary3:Text##```                       | ##:primary3:Text##                          | primary3\n" +
                "```##:secondary5:Text##```                     | ##:s4:Text##                          | secondary5\n" +
                "```##{s12:AA##:12:BB##\\u001E##:6:CC##DD}##```       | ##{s12:AA##:12:BB##\\u001E##:6:CC##DD}##                      | composed colors, note the \\\\u001E separator\n" +
                "```##:f158:AA## ##:f58:BB## ##:f201:CC##```    | ##:f158:AA## ##:f58:BB## ##:f201:CC##                      | foreground 8bits colors\n" +
                "```##:foreground158:Text##```                  | ##:foreground158:Text##                          | foreground 158 (8bits)\n" +
                "```##:fxd787af:Text##```                       | ##:fxd787af:Text##                          | foreground Pink (d787af in 24bits)\n" +
                "```##:foregroundxd787af:Text##```              | ##:foregroundxd787af:Text##                          | foreground Pink (d787af in 24bits)\n" +
                "```##:b158:Text##```                           | ##:b158:Text##                          | background 158 (8bits)\n" +
                "```##:bxd787af:Text##```                       | ##:bxd787af:Text##                | background Pink (24bits)";

        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
        Assertions.assertEquals(NTextType.LIST, parsed.getType());
        Assertions.assertEquals(40, ((NTextList)parsed).size());
    }
    @Test
    public void test38() {
        String str = "```##:bxd787af:Text##```";
        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
//        Assertions.assertEquals(NTextType.LIST, parsed.getType());
//        Assertions.assertEquals(12, ((NTextList)parsed).size());
    }
    @Test
    public void test39() {
        String str = "\\```underlined underlined\\```";
        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
        Assertions.assertEquals(NTextType.PLAIN, parsed.getType());
        String text = ((NTextPlain) parsed).getText();
        Assertions.assertEquals("```underlined underlined```", text);
        String text2 = parsed.toString();
        Assertions.assertEquals(str, text2);
    }

    @Test
    public void test40() {
        String str = "```#) Title 1```";
        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
        Assertions.assertEquals(NTextType.CODE, parsed.getType());
        String text = ((NTextCode) parsed).getText();
        Assertions.assertEquals("#) Title 1", text);
        String text2 = parsed.toString();
        //will add leading space
        Assertions.assertEquals("``` #) Title 1```", text2);
    }

    @Test
    public void test41() {
        String str = "###) Xml format\n" +
                "####) NTF syntax\n"
        ;
        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
        Assertions.assertEquals(NTextType.LIST, parsed.getType());
        Assertions.assertEquals(2, ((NTextList) parsed).size());
        Assertions.assertEquals(NTextType.TITLE, ((NTextList) parsed).get(0).getType());
        Assertions.assertEquals(NTextType.TITLE, ((NTextList) parsed).get(1).getType());
    }
    @Test
    public void test42() {
        String str = "##{pale:2024-06-07 23:28:39.328}##\u001E ##{config:CONFIG}##\u001E ##{info:START  }##\u001E : start ```sh nuts``` ```primary3 ##{version:0.8.4}##\u001E``` at 2024-06-07 23:28:28.660";
        session.out().println(str);
        NText parsed = NString.of(str, session).toText();
        TestUtils.println(parsed);
//        Assertions.assertEquals(NTextType.LIST, parsed.getType());
//        Assertions.assertEquals(2, ((NTextList) parsed).size());
//        Assertions.assertEquals(NTextType.TITLE, ((NTextList) parsed).get(0).getType());
//        Assertions.assertEquals(NTextType.TITLE, ((NTextList) parsed).get(1).getType());
    }
}
