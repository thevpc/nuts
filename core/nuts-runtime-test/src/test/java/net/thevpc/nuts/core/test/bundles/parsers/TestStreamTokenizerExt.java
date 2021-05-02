package net.thevpc.nuts.core.test.bundles.parsers;

import net.thevpc.nuts.runtime.bundles.parsers.StreamTokenizerExt;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class TestStreamTokenizerExt {
    @Test
    public void test1() {
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader("8.0.0"));
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
