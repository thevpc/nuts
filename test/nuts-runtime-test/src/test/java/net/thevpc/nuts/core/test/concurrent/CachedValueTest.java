/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.NBootOptionsBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NBeanRef;
import net.thevpc.nuts.runtime.standalone.concurrent.NRetryCallStoreMemory;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


/**
 * @author thevpc
 */
public class CachedValueTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        NOut.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void test1() {
        NCachedValue<String> cv=NCachedValue.of(()->{
            NOut.println("calculating cached value");
            return "Hello";
        });
        NOut.println(cv.get());
        NOut.println(cv.get());
        NOut.println(cv.invalidate().get());
        NOut.println(cv.get());
    }
}
