package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgParam;
import net.thevpc.nuts.text.NMsgTemplate;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MsgTest {

    @Test
    public void test01() {
        NMsgTemplate template = NMsgTemplate.ofC("a %s %n %s");
        List<String> paramNames = template.paramNames();
        Assertions.assertEquals(Arrays.asList("", ""), paramNames);
    }

    @Test
    public void test02() {
        NMsgTemplate template = NMsgTemplate.ofJ("a {} {0} {3}");
        List<String> paramNames = template.paramNames();
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3"), paramNames);

        template = NMsgTemplate.ofJ("a {} {}");
        paramNames = template.paramNames();
        Assertions.assertEquals(Arrays.asList("0", "1"), paramNames);
        TestUtils.println(template.build(NMsgParam.of("0", () -> "0"), NMsgParam.of("1", () -> "1")));

        template = NMsgTemplate.ofJ("a {} {0}");
        paramNames = template.paramNames();
        Assertions.assertEquals(Collections.singletonList("0"), paramNames);

        template = NMsgTemplate.ofJ("a {} {}");
        paramNames = template.paramNames();
        Assertions.assertEquals(Arrays.asList("0", "1"), paramNames);

        template = NMsgTemplate.ofJ("a {4} {}");
        paramNames = template.paramNames();
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), paramNames);
    }

    @Test
    public void test03() {
        List<NToken> textTokens = NStringUtils.parseDollarPlaceHolder("a${b}c").collect(Collectors.toList());
        TestUtils.println(textTokens);
        //Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), Arrays.asList(paramNames));
    }

    @Test
    public void test04() {
        List<NToken> textTokens = NStringUtils.parseDollarPlaceHolder("<a href=\"https://protos-erp.com\" style=\"color:#00a4bd\" target=\"_blank\">\n" +
                "        <img src=\"https://protos-erp.com/assets/crm/protos-banner-fr.png\"\n" +
                "        trackerFolder=\"prospect\" trackerName=\"protos-banner-fr.png\" trackerSecretEmail=\"${email}\" trackerSecretCompany=\"${company}\"\n" +
                "        alt=\"Banner 2024\" style=\"outline:none;text-decoration:none;border:none;max-width:100%;font-size:16px;border-radius: 25px;\" width=\"560\" align=\"middle\"/>\n" +
                "        </a>").collect(Collectors.toList());
        TestUtils.println(textTokens);
        //Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), Arrays.asList(paramNames));
    }

    @Test
    public void test05() {
        String r = NMsg.ofV("$JAVA_HOME/B",
                s -> {
                    switch (s) {
                        case "JAVA_HOME":
                            return "A";
                    }
                    return null;
                }).toString();
        TestUtils.println(r);
        Assertions.assertEquals("A/B", r);
        //Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), Arrays.asList(paramNames));
    }

    @Test
    public void test06() {
        List<NToken> textTokens = NStringUtils.parsePlaceHolder("s$${{example1}}$$\n$${{example2}}$$e",
                        Pattern.compile("(?s)(?m)\\$\\$\\{\\{(?<var>[^}]+)}}\\$\\$"),
                        "var")
                .collect(Collectors.toList());
        for (NToken textToken : textTokens) {
            TestUtils.println(textToken);
        }
        Assertions.assertEquals(5, textTokens.size());

        Assertions.assertEquals(NToken.TT_VAR, textTokens.get(1).ttype);
        Assertions.assertEquals(NToken.TT_VAR, textTokens.get(1).ttype);
        Assertions.assertEquals("example1", textTokens.get(1).sval);
        Assertions.assertEquals("$${{example1}}$$", textTokens.get(1).image);

        Assertions.assertEquals("example2", textTokens.get(3).sval);
        Assertions.assertEquals("$${{example2}}$$", textTokens.get(3).image);
        //Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), Arrays.asList(paramNames));
    }

    @Test
    public void test07() {
        Nuts.require("--color");
        NOut.println(NMsg.ofC("##:13:%s##", ":"));
        //NOut.println(NMsg.ofNtf("##AA##"));
    }

    @Test
    public void test08() {
        Nuts.require("--color");
        NOut.println(NMsg.ofJ("##:12:{}##", ":"));
        NOut.println(NMsg.ofJ("##:12:{0}##", ":"));
    }

    @Test
    public void test09() {
        Nuts.require("--color");
        NOut.println(NMsg.ofV("##:12:$v##", NMaps.of("v", ":")));
    }

    @Test
    public void test10() {
        Nuts.require("--color");
        NOut.println(NMsg.ofV("##:red:$v##", NMaps.of("v", "this is red")));
        NOut.println(NMsg.ofV("##:MediumVioletRed:$v##", NMaps.of("v", "this is MediumVioletRed")));
        NOut.println(NMsg.ofV("```SandyBrown $v```", NMaps.of("v", "this is SandyBrown")));
    }

    @Test
    public void test11() {
        Nuts.require("--color");
        NOut.println(NMsg.ofM("##:red:{{v}}##", NMaps.of("v", "this is red")));
    }

    @Test
    public void test12() {
        List<NToken> list1 = NStringUtils.parseMoustachePlaceHolder("{{v}}{{v}").collect(Collectors.toList());
        TestUtils.println(NMsg.ofM("{{v}}{{v}", NMaps.of("v", "this is red")));
    }

    @Test
    public void test13() {
        List<NToken> list2 = NStringUtils.parseDollarPlaceHolder("${v}${v").collect(Collectors.toList());
        TestUtils.println(NMsg.ofV("${v}${v", NMaps.of("v", "this is red")));
    }

    @Test
    public void test14() {
        List<NToken> list2 = NStringUtils.parseMoustachePlaceHolder("jdbc:postgresql://localhost:5432/{{database}}").collect(Collectors.toList());
        Assertions.assertEquals(2, list2.size());
    }
}
