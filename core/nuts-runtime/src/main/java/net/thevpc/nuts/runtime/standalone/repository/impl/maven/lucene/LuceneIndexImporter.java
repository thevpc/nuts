package net.thevpc.nuts.runtime.standalone.repository.impl.maven.lucene;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.runtime.standalone.repository.index.ArtifactsIndexDB;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LuceneIndexImporter {
    public LuceneIndexImporter() {
    }

    public long importGzURL(URL url, String repository) {
//        NutsWorkspace ws = session.getWorkspace();
        String tempGzFile = NPath.ofTempFile("lucene-repository.gz").toString();
        NCp.of()
                .from(url).to(NPath.of(tempGzFile)).run();
        String tempFolder = NPath.ofTempFolder("lucene-repository").toString();
        NUncompress.of().from(NPath.of(tempGzFile)).to(
                NPath.of(tempFolder)
        ).setPackaging("gz").run();
        try {
            long[] ref=new long[1];
            Files.list(Paths.get(tempFolder)).forEach(
                    x -> {
                        ref[0]+=importFile(x.toString(),repository);
                    }
            );
            return ref[0];
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public long importFile(String filePath, String repository) {
        ArtifactsIndexDB adb = ArtifactsIndexDB.of();
        int addedCount=0;
        int allCount=0;
        try (DirtyLuceneIndexParser p = new DirtyLuceneIndexParser(new FileInputStream(filePath))) {
            while (p.hasNext()) {
                NId id = NId.of(p.next()).get().builder().setRepository(repository).build();
                if (!adb.contains(id)) {
                    addedCount++;
                    adb.add(id);
                }
                allCount++;
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        } finally {
            adb.flush();
        }
        return addedCount;
    }
}
