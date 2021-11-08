package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.bridges.maven.LuceneIndexImporter;
import net.thevpc.nuts.runtime.standalone.index.ArtifactsIndexDB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test25_LuceneTest {
    @Test
    public void test() {
        NutsSession session = TestUtils.openNewTestWorkspace("-ZyKk");
        LuceneIndexImporter lii = new LuceneIndexImporter(session);
        long count = lii.importGzURL(
                Test25_LuceneTest.class.getResource(
                        "/net/thevpc/nuts/core/test/nexus-maven-repository-index.359.gz"
                ), "maven-central", session
        );
        long count0 = ArtifactsIndexDB.of(session).findAll(session).count();
        System.out.println(count);
        Assertions.assertEquals(count0, count);
    }
}
