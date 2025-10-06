/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.NBootOptionsBuilder;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NBeanRef;
import net.thevpc.nuts.runtime.standalone.concurrent.NRetryCallStoreMemory;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


/**
 * @author thevpc
 */
public class RetryCallTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        System.out.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void test1() {
        NRetryCallStore jdbcStore = new NRetryCallStoreMemory(); // any store here
        NBeanContainer springContainer = new NBeanContainer() {
            Map<String, Object> beans = new HashMap<>();

            {
                beans.put("callSomeThingBean", new NCallable<NBeanRef>() {
                    @Override
                    public NBeanRef call() {
                        return null;
                    }
                });
                beans.put("resultSomeThingBean", new NCallable<NBeanRef>() {
                    @Override
                    public NBeanRef call() {
                        return null;
                    }
                });
            }

            @Override
            public <T> NOptional<T> get(NBeanRef ref) {
                return NOptional.ofNamed((T) beans.get(ref.getId()), ref.getId());
            }
        };
        NRetryCallFactory factory = NConcurrent.of()
                .retryCallFactory()
                .withStore(jdbcStore)
                .withBeanContainer(springContainer)
                ;
        factory.of("something", NBeanRef.of("callSomeThingBean").as(NCallable.class))
                .setHandler(NBeanRef.of("resultSomeThingBean").as(NRetryCall.Handler.class))
                .callAsync();
    }


}
