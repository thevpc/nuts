/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
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

    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        String[] s = NutsIdListHelper.parseIdListStrings("java#[11,[", session);
        Assertions.assertArrayEquals(new String[]{"java#[11,["},s);
    }
    @Test
    public void test02() {
        String[] s = NutsIdListHelper.parseIdListStrings("java#[11,[ java#[11,[", session);
        //removed duplicates...
        Assertions.assertArrayEquals(new String[]{"java#[11,["},s);
    }
    @Test
    public void test03() {
        String[] s = NutsIdListHelper.parseIdListStrings("java#[11,[ java#[12,[", session);
        Assertions.assertArrayEquals(new String[]{"java#[11,[" ,"java#[12,["},s);
    }
}
