package net.thevpc.nuts.core.test.bundles.mvn;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.bridges.maven.LuceneIndexImporter;
import net.thevpc.nuts.runtime.standalone.index.ArtifactsIndexDB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LuceneFileImporter {
    @Test
    public void test() {
        NutsSession session = TestUtils.openNewTestWorkspace("-ZyKk", "-w", "temp/test");
        LuceneIndexImporter lii = new LuceneIndexImporter(session);
        long count = lii.importGzURL(
                LuceneFileImporter.class.getResource(
                        "/net/thevpc/nuts/core/test/nexus-maven-repository-index.359.gz"
                ), "maven-central", session
        );
        long count0 = ArtifactsIndexDB.of(session).findAll().count();
        System.out.println(count);
        Assertions.assertEquals(count0, count);
    }
}
