package net.thevpc.nuts.core.test;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringMapFormat;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class Test42_NStringMapFormat {

    @Test
    public void test01() {
        NStringMapFormat t = NStringMapFormat.of("=", null, null, false);
        NOptional<Map<String, List<String>>> u = t.parseDuplicates("src=\"https://protos-erp.com/assets/crm/protos-banner-fr.png\"\n" +
                "        tf=\"p\" tn=\"banner-fr.png\" trackerSecretEmail=\"${email}\" tsc=\"${company}\"\n" +
                "        alt=\"Banner 2024\" style=\"outline:none;text-decoration:none;border:none;max-width:100%;font-size:16px;border-radius: 25px;\" width=\"560\" align=\"middle\"/");
//        NOptional<Map<String, List<String>>> u = t.parseDuplicates("src=\"https\"\n");
        System.out.println(u);
    }

    @Test
    public void test02() {
        Map<String, String> u = NStringMapFormat.DEFAULT.parse("c=52&s=17.0").get();
//        NOptional<Map<String, List<String>>> u = t.parseDuplicates("src=\"https\"\n");
        System.out.println(u);
    }


}
