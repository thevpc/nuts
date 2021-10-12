package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdParser;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.mvn.DirtyLuceneIndexParser;
import net.thevpc.nuts.runtime.standalone.index.ArtifactsIndexDB;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LuceneIndexImporter {
    private NutsSession session;

    public LuceneIndexImporter(NutsSession session) {
        this.session = session;

    }

    public long importGzURL(URL url, String repository,NutsSession session) {
//        NutsWorkspace ws = session.getWorkspace();
        String tempGzFile = session.io().tmp().createTempFile("lucene-repository.gz").toString();
        session.io().copy()
                .setSession(session)
                .from(url).to(tempGzFile).run();
        String tempFolder = session.io().tmp().createTempFolder("lucene-repository").toString();
        session.io().uncompress().from(tempGzFile).to(
                tempFolder
        ).setFormat("gz").run();
        try {
            long[] ref=new long[1];
            Files.list(Paths.get(tempFolder)).forEach(
                    x -> {
                        ref[0]+=importFile(x.toString(),repository,session);
                    }
            );
            return ref[0];
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public long importFile(String filePath,String repository,NutsSession session) {
        ArtifactsIndexDB adb = ArtifactsIndexDB.of(session);
        NutsIdParser idParser = session.id().parser();
        int addedCount=0;
        int allCount=0;
        try (DirtyLuceneIndexParser p = new DirtyLuceneIndexParser(new FileInputStream(filePath))) {
            while (p.hasNext()) {
                NutsId id = idParser.parse(p.next()).builder().setRepository(repository).build();
                if (!adb.contains(id)) {
                    addedCount++;
                    adb.add(id);
                }
                allCount++;
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            adb.flush();
        }
        return addedCount;
    }
}
