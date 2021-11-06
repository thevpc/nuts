package net.thevpc.nuts.core.test.bundles.parsers;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.expr.StreamTokenizerExt;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class TestStreamTokenizerExt {
    @Test
    public void test1() {
        NutsSession session = TestUtils.openExistingTestWorkspace();
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader("8.0.0"),session);
        st.xmlComments(true);
        st.doNotParseNumbers();
        st.wordChars('0','9');
        st.wordChars('.','.');
        st.wordChars('-','-');

        int s;
        while ((s = st.nextToken()) != StreamTokenizerExt.TT_EOF) {
            System.out.println(st.image);
        }
    }
}
