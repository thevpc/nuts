package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.format.xml.XmlUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenMetadata;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenMetadataParser;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.util.NStringBuilderImpl;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.xtra.ps.PosixPsParser;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

/**
 * Guards behavior of hot paths whose implementation was optimized (PERF batch).
 */
public class PerformanceRegressionTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void cformatFastPathKeepsSemantics() {
        Assertions.assertEquals("value=abc, n=7, pad=0042",
                NMsg.ofC("value=%s, n=%d, pad=%04d", "abc", 7, 42).toString());
        Assertions.assertEquals("x=demo:1.2",
                NMsg.ofC("x=%1$s:%2$s", "demo", "1.2").toString());
    }

    @Test
    public void jdkProviderDetectionUsesPrecompiledPatterns() {
        NJavaSdkUtils u = NJavaSdkUtils.of();
        Assertions.assertEquals("IBM Temurin", u.detectJdkProvider("AdoptOpenJDK temurin 17"));
        Assertions.assertEquals("Amazon Corretto", u.detectJdkProvider("corretto-21"));
        Assertions.assertNull(u.detectJdkProvider("mystery jdk"));
    }

    @Test
    public void repositoryLocationParsingStillWorks() {
        Assertions.assertEquals("name",
                NRepositoryUtils.createRepositoryLocation("name=maven-central").get().name());
        Assertions.assertEquals("maven-central",
                NRepositoryUtils.createRepositoryLocation("name=maven-central").get().path());
        Assertions.assertEquals("custom",
                NRepositoryUtils.createRepositoryLocation("custom").get().name());
    }

    @Test
    public void stringBuilderRegexReplacementStillWorks() {
        NStringBuilder b = new NStringBuilderImpl().append("a1b2c3");
        b.replaceFirst("[0-9]", "X");
        Assertions.assertEquals("aXb2c3", b.toString());
        b.replaceAll("[0-9]", "Y");
        Assertions.assertEquals("aXbYcY", b.toString());
    }

    @Test
    public void textSplittingStillWorks() {
        NText t = NText.ofPlain("a,b,,c");
        Assertions.assertEquals(4, t.split(",").size());
        Assertions.assertEquals(3, NText.ofPlain("a\nb\nc").splitLines().size());
        List<NText> withSep = t.split(",", true);
        Assertions.assertTrue(withSep.size() > 4);
    }

    @Test
    public void xmlBuildersAreReusable() {
        Assertions.assertNotNull(XmlUtils.createDocumentBuilder(true).newDocument());
        Assertions.assertNotNull(XmlUtils.createDocumentBuilder(false).newDocument());
    }

    @Test
    public void mavenMetadataRoundTrip() {
        MavenMetadata m = new MavenMetadata();
        m.setGroupId("net.example");
        m.setArtifactId("demo");
        m.setLastUpdated(java.util.Date.from(Instant.parse("2026-11-20T10:11:12Z")));
        m.getVersions().add("1.0.0");
        String xml = new MavenMetadataParser().toXmlString(m);
        Assertions.assertTrue(xml.contains("<artifactId>demo</artifactId>"), xml);
    }

    @Test
    public void psStartDateParsingStillWorks() {
        Instant i = PosixPsParser.parseStartDateLong("Fri Nov 20 10:11:12 2026");
        Assertions.assertNotNull(i);
        Assertions.assertTrue(i.toString().startsWith("2026-11-20"), String.valueOf(i));
    }
}
