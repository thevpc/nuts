package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NMsgParam;
import net.thevpc.nuts.NMsgTemplate;
import net.thevpc.nuts.spi.NRepositoryLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class Test39_NMsgTest {

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

}
