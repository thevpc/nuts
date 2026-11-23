package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.runtime.standalone.util.jclass.AbstractDiscoJavaProvider;
import net.thevpc.nuts.runtime.standalone.util.jclass.GraalVMProvider;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the Java download providers (Foojay Disco API).
 * <p>
 * The Foojay API answers {@code 400} for an unknown distribution id. The query used to let that
 * error escape, which aborted the whole search for a Java ({@code NJavaSdkUtils.resolveAndInstall}
 * tries every provider in turn), so an installed Java that would have been accepted was never reached.
 * A provider that cannot be queried must simply offer nothing.
 * <p>
 * These tests call the real Foojay API, hence {@code local-only}.
 */
@Tag("local-only")
public class JavaProviderResilienceTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    /** A provider whose distribution id Foojay rejects with HTTP 400. */
    private static class UnknownDistributionProvider extends AbstractDiscoJavaProvider {
        @Override
        public String getName() {
            return "unknown";
        }

        @Override
        protected String getDiscoDistributionName() {
            return "no_such_distribution";
        }
    }

    @Test
    public void providerRejectedByTheServerOffersNothingInsteadOfThrowing() {
        NOptional<NPath> p = Assertions.assertDoesNotThrow(
                () -> new UnknownDistributionProvider().resolveDownloadPath(
                        "jdk", 21, NOsFamily.WINDOWS, NArchFamily.X86_64, null));
        Assertions.assertFalse(p.isPresent());
    }

    @Test
    public void graalVmProviderUsesADistributionIdKnownToFoojay() {
        // used to be "graalvm_ce", which Foojay rejects ("unsupported distribution")
        NOptional<NPath> p = Assertions.assertDoesNotThrow(
                () -> new GraalVMProvider().resolveDownloadPath(
                        "jdk", 21, NOsFamily.WINDOWS, NArchFamily.X86_64, null));
        Assertions.assertTrue(p.isPresent(), "graalvm_community 21 should be downloadable on windows/x64");
    }
}
