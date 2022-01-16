/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.id.NutsIdListHelper;
import net.thevpc.nuts.runtime.standalone.io.util.NutsPathParts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thevpc
 */
public class Test32_NutsIdListHelper {

    @BeforeAll
    public static void init() {
    }

    @Test
    public void test01() {
        String[] s = NutsIdListHelper.parseIdListStrings("java#[11,[", null);
        Assertions.assertArrayEquals(new String[]{"java#[11,["},s);
    }
    @Test
    public void test02() {
        String[] s = NutsIdListHelper.parseIdListStrings("java#[11,[ java#[11,[", null);
        Assertions.assertArrayEquals(new String[]{"java#[11,[" ,"java#[11,["},s);
    }
}
