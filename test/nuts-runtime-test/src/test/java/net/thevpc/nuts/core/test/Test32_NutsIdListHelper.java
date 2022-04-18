/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
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
        List<NutsId> s = NutsId.ofList("java#[11,[").get(session);
        Assertions.assertEquals(Arrays.asList(NutsId.of("java#[11,[").get(session)), s);
    }

    @Test
    public void test02() {
        List<NutsId> s = NutsId.ofList("java#[11,[ java#[11,[").get(session);
        //removed duplicates...
        Assertions.assertEquals(Arrays.asList(NutsId.of("java#[11,[").get(session)), s);
    }

    @Test
    public void test03() {
        List<NutsId> s = NutsId.ofList("java#[11,[ java#[12,[").get(session);
        Assertions.assertEquals(Arrays.asList(
                NutsId.of("java#[11,[").get(session),
                NutsId.of("java#[12,[").get(session)
        ), s);
    }
}
