package net.thevpc.nuts.runtime.standalone.repository.impl.maven.lucene;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.runtime.standalone.repository.index.ArtifactsIndexDB;
import net.thevpc.nuts.spi.NPaths;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LuceneIndexImporter {
    private NSession session;

    public LuceneIndexImporter(NSession session) {
        this.session = session;

    }

    public long importGzURL(URL url, String repository, NSession session) {
//        NutsWorkspace ws = session.getWorkspace();
        String tempGzFile = NPaths.of(session).createTempFile("lucene-repository.gz").toString();
        NCp.of(session)
                .setSession(session)
                .from(url).to(NPath.of(tempGzFile,session)).run();
        String tempFolder = NPaths.of(session).createTempFolder("lucene-repository").toString();
        NUncompress.of(session).from(NPath.of(tempGzFile,session)).to(
                NPath.of(tempFolder,session)
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
            throw new NIOException(session, ex);
        }
    }

    public long importFile(String filePath, String repository, NSession session) {
        ArtifactsIndexDB adb = ArtifactsIndexDB.of(session);
        int addedCount=0;
        int allCount=0;
        try (DirtyLuceneIndexParser p = new DirtyLuceneIndexParser(new FileInputStream(filePath),session)) {
            while (p.hasNext()) {
                NId id = NId.of(p.next()).get(session).builder().setRepository(repository).build();
                if (!adb.contains(id,session)) {
                    addedCount++;
                    adb.add(id,session);
                }
                allCount++;
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        } finally {
            adb.flush(session);
        }
        return addedCount;
    }
}
