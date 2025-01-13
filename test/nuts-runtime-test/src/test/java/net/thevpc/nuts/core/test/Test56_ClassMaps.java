package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NSig;
import net.thevpc.nuts.util.NSigMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Test56_ClassMaps {

    @BeforeAll
    static void init() {
        TestUtils.openNewTestWorkspace();
    }



    @Test
    public void test01() {
        NSigMap<NSig> cm=new NSigMap<NSig>(NSig.class);
        cm.put(NSig.of(CharSequence.class,int.class), NSig.of(CharSequence.class,int.class));
        cm.put(NSig.of(String.class,Number.class), NSig.of(String.class,Number.class));
        Assertions.assertEquals(
                NSig.of(CharSequence.class,int.class),
                cm.get(NSig.of(String.class,Integer.class)).get()
        );
    }


}
