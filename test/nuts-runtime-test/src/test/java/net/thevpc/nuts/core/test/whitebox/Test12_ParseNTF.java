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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thevpc
 */
public class Test12_ParseNTF {
    private static String baseFolder;

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null, new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        CoreIOUtils.delete(null, new File(baseFolder));
    }

    @Test
    public void test1() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "--skip-companions");
        NutsTextManager txt = session.text();
        NutsText parsed = txt.parse("##:error0:n#01##");
        System.out.println(parsed);
    }

    @Test
    public void test2() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "-Z",
                "--yes",
                "--skip-companions");
        NutsTextManager txt = session.text();

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        String str = "##:p2:╭───╮##\u001E\n##:p3:│##";
        NutsText q = txt.ofStyled(txt.parse(str), NutsTextStyle.error());
        String qs = q.toString();
        NutsText q2 = txt.parse(qs);
        q2 = txt.parse(qs);
        System.out.println(qs);
        NutsText parsed = txt.parse("##:error0:n#01##");
        System.out.println(parsed);
    }

    @Test
    public void test3() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "-Z",
                "--yes",
                "--skip-companions");
        NutsTextManager txt = session.text();

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        String str = "##:p2:╭───╮##\u001E\n##:p3:│##";
        NutsText q = txt.parse(str);
        q = txt.parse(str);
        System.out.println(q);
        NutsText parsed = txt.parse("##:error0:n#01##");
        System.out.println(parsed);
    }

    @Test
    public void test4() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "-Z",
                "--skip-companions");
        NutsTextManager txt = session.text();

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        //String str="##{error0:##{error0:not installed : ##:p1:ntomcat##\u001E}}##\u001E}##\u001E";
        String str = "##{error0:##{error0:not installed : ##:p1:ntomcat##\u001E}##\u001E}##\u001E";
        NutsText q2 = txt.parse(str);
        String str2 = q2.toString();
        System.out.println(str2);
    }


    @Test
    public void test5() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "-Z",
                "--skip-companions");
        NutsTextManager txt = session.text();

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
                "    ##:string0:\"installFolder\"##\u001E##:separator0::##\u001E ##:string0:\"/home/vpc/.local/share/nuts/apps/default-workspace/id/com/github/vatbub/mslinks/1.0.5\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justInstalled\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justRequired\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E\n" +
                "  ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"type\"##\u001E##:separator0::##\u001E ##:string0:\"REGULAR\"##\u001E\n" +
                "##:separator0:}##\u001E";
        NutsText q2 = txt.parse(str);
        String str2 = q2.toString();
        System.out.println(str2);
    }
    @Test
    public void test6() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "-Z",
                "--skip-companions");
        NutsTextManager txt = session.text();

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
                "    ##:string0:\"installFolder\"##\u001E##:separator0::##\u001E ##:string0:\"/home/vpc/.local/share/nuts/apps/default-workspace/id/com/github/vatbub/mslinks/1.0.5\"##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justInstalled\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E##:separator0:,##\u001E\n" +
                "    ##:string0:\"justRequired\"##\u001E##:separator0::##\u001E ##:keyword0:false##\u001E\n" +
                "  ##:separator0:}##\u001E##:separator0:,##\u001E\n" +
                "  ##:string0:\"type\"##\u001E##:separator0::##\u001E ##:string0:\"REGULAR\"##\u001E\n" +
                "##:separator0:}##\u001E";
        NutsText q2 = txt.parse(str);
        String str2 = q2.toString();
        System.out.println(str2);
    }

    @Test
    public void test7() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "-Z",
                "--skip-companions");
        NutsTextManager txt = session.text();

//        String str = "##{string0:\"<main>://com.github.vatbub:mslinks#1.0.5\"}##";
        String str = "##{string:a#b}##";
        NutsText q2 = txt.parse(str);
        String str2 = q2.toString();
        System.out.println(str2);
    }


    @Test
    public void test8() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        NutsSession session = TestUtils.openNewTestWorkspace("-ZSkK");
        NutsTextManager txt = session.text();

//        String str = "##{string0:\"<main>://com.github.vatbub:mslinks#1.0.5\"}##";
        String str = "##:string:\"a#b\"##";
        NutsText q2 = txt.parse(str);
        String str2 = q2.toString();
        System.out.println(str2);
    }


}
