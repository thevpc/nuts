package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MsgTest {

    @Test
    public void test01() {
        NMsgTemplate template = NMsgTemplate.ofC("a %s %n %s");
        String[] paramNames = template.getParamNames();
        Assertions.assertEquals(Arrays.asList("", ""), Arrays.asList(paramNames));
    }

    @Test
    public void test02() {
        NMsgTemplate template = NMsgTemplate.ofJ("a {} {0} {3}");
        String[] paramNames = template.getParamNames();
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3"), Arrays.asList(paramNames));

        template = NMsgTemplate.ofJ("a {} {}");
        paramNames = template.getParamNames();
        Assertions.assertEquals(Arrays.asList("0", "1"), Arrays.asList(paramNames));
        System.out.println(template.build(NMsgParam.of("0", () -> "0"), NMsgParam.of("1", () -> "1")));

        template = NMsgTemplate.ofJ("a {} {0}");
        paramNames = template.getParamNames();
        Assertions.assertEquals(Arrays.asList("0"), Arrays.asList(paramNames));

        template = NMsgTemplate.ofJ("a {} {}");
        paramNames = template.getParamNames();
        Assertions.assertEquals(Arrays.asList("0", "1"), Arrays.asList(paramNames));

        template = NMsgTemplate.ofJ("a {4} {}");
        paramNames = template.getParamNames();
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), Arrays.asList(paramNames));
    }

    @Test
    public void test03() {
        List<NToken> textTokens = NStringUtils.parseDollarPlaceHolder("a${b}c").collect(Collectors.toList());
        System.out.println(textTokens);
        //Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), Arrays.asList(paramNames));
    }

    @Test
    public void test04() {
        List<NToken> textTokens = NStringUtils.parseDollarPlaceHolder("<a href=\"https://protos-erp.com\" style=\"color:#00a4bd\" target=\"_blank\">\n" +
                "        <img src=\"https://protos-erp.com/assets/crm/protos-banner-fr.png\"\n" +
                "        trackerFolder=\"prospect\" trackerName=\"protos-banner-fr.png\" trackerSecretEmail=\"${email}\" trackerSecretCompany=\"${company}\"\n" +
                "        alt=\"Banner 2024\" style=\"outline:none;text-decoration:none;border:none;max-width:100%;font-size:16px;border-radius: 25px;\" width=\"560\" align=\"middle\"/>\n" +
                "        </a>").collect(Collectors.toList());
        System.out.println(textTokens);
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
        System.out.println(r);
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
            System.out.println(textToken);
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
        NOut.println(NMsg.ofV("##:12:$v##", NMaps.of("v",":")));
    }
}
