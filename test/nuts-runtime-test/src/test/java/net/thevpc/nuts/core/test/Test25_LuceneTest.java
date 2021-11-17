package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.LuceneIndexImporter;
import net.thevpc.nuts.runtime.standalone.repository.index.ArtifactsIndexDB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test25_LuceneTest {
    @Test
    public void test() {
        NutsSession session = TestUtils.openNewTestWorkspace("-ZyKk");
        LuceneIndexImporter lii = new LuceneIndexImporter(session);
        long countWritten = lii.importGzURL(
                Test25_LuceneTest.class.getResource(
                        "/net/thevpc/nuts/core/test/nexus-maven-repository-index.359.gz"
                ), "maven-central", session
        );
        long countRead = ArtifactsIndexDB.of(session).findAll(session).count();
        TestUtils.println(countWritten);
        Assertions.assertEquals(countWritten,countRead);
    }
}
