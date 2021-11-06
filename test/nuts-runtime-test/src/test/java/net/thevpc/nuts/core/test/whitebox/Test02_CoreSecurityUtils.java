package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.bundles.io.CoreSecurityUtils;
import org.junit.jupiter.api.Test;

public class Test02_CoreSecurityUtils {
    @Test
    public void testEncrypt(){
        NutsSession session = TestUtils.openNewTestWorkspace();
        char[] r = CoreSecurityUtils.defaultEncryptChars("Hello".toCharArray(), "okkay",session);
        System.out.println(new String(r));
        char[] i = CoreSecurityUtils.defaultDecryptChars(r, "okkay",session);
        System.out.println(new String(i));
    }
}
