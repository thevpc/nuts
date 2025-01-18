package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NPlatformSignature;
import net.thevpc.nuts.util.NPlatformSignatureMap;
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
        NPlatformSignatureMap<NPlatformSignature> cm=new NPlatformSignatureMap<NPlatformSignature>(NPlatformSignature.class);
        cm.put(NPlatformSignature.of(CharSequence.class,int.class), NPlatformSignature.of(CharSequence.class,int.class));
        cm.put(NPlatformSignature.of(String.class,Number.class), NPlatformSignature.of(String.class,Number.class));
        Assertions.assertEquals(
                NPlatformSignature.of(CharSequence.class,int.class),
                cm.get(NPlatformSignature.of(String.class,Integer.class)).get()
        );
    }


}
