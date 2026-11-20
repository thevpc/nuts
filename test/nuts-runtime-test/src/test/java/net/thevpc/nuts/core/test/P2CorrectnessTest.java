package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the P2 correctness batch (BUG-08..18).
 */
public class P2CorrectnessTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void lessThanOperatorsUseConsistentImagesAndVariants() {
        assertOperator("<", "<");
        assertOperator("<=", "<=");
        assertOperator("<==", "<==");
        assertOperator("<--", "<--");
        assertOperator("<-", "<-");
        assertOperator("<<", "<<");
        assertOperator("<<<", "<<<");
    }

    private static void assertOperator(String source, String expectedVariant) {
        TsonCustomLexer lexer = new TsonCustomLexer(source);
        NElementTokenImpl token = lexer.next();
        Assertions.assertNotNull(token, "no token produced for " + source);
        Assertions.assertEquals(source, token.image(), "image must match the source operator");
        Assertions.assertEquals(expectedVariant, token.variant(), "variant must match the operator image");
    }
}
