package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.lucene.LuceneIndexImporter;
import net.thevpc.nuts.runtime.standalone.repository.index.ArtifactsIndexDB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LuceneTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test() {
        LuceneIndexImporter lii = new LuceneIndexImporter();
        long countWritten = lii.importGzURL(
                LuceneTest.class.getResource(
                        "/net/thevpc/nuts/core/test/nexus-maven-repository-index.359.gz"
                ), "maven-central"
        );
        long countRead = ArtifactsIndexDB.of().findAll().count();
        TestUtils.println(countWritten);
        Assertions.assertEquals(countWritten,countRead);
    }
}
