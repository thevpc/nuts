/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author thevpc
 */
public class TextTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }



    @Test
    public void test1() {
        List<NText> split = NText.ofList(NText.of("a/:b"), NText.of("/"), NText.of("c"), NText.of("/"))
                .split("/:",true);
        System.out.println(split);
        split = NText.of(NText.of("a/:b/c/"))
                .split("/:",true);
        System.out.println(split);
    }
}
