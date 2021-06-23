package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.runtime.bundles.io.CoreSecurityUtils;
import org.junit.jupiter.api.Test;

public class Test02_CoreSecurityUtils {
    @Test
    public void testEncrypt(){
        char[] r = CoreSecurityUtils.defaultEncryptChars("Hello".toCharArray(), "okkay");
        System.out.println(new String(r));
        char[] i = CoreSecurityUtils.defaultDecryptChars(r, "okkay");
        System.out.println(new String(i));
    }
}
