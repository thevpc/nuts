package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.io.NUncompressVisitor;

import java.io.InputStream;

public class ExamplesOfZip {
    public void executeAll(NSession session) {
        executeCompress(session);
        executeUncompress(session);
    }

    public void executeCompress(NSession session) {
        session.out().println("Example of ## Compress ##");
        NPath example = NPath.ofUserDirectory(session).resolve("example");
        if (example.isDirectory()) {
            NCompress.of(session)
                    .addSource(example)
                    .setTarget(example.resolveSibling(example.getBaseName() + ".zip"))
                    .setPackaging("zip")
                    .run();
        }
    }

    public void executeUncompress(NSession session) {
        session.out().println("Example of ## Uncompress ##");
        NPath example = NPath.ofUserDirectory(session).resolve("example.zip");
        if (example.isRegularFile()) {
            session.out().println(NMsg.ofC("Listing %s", example));
            NUncompress.of(session)
                    .setSource(example)
                    .visit(new NUncompressVisitor() {
                        @Override
                        public boolean visitFolder(String path) {
                            return true;
                        }

                        @Override
                        public boolean visitFile(String path, InputStream inputStream) {
                            session.out().println(path);
                            return true;
                        }
                    })
                    .run();
            session.out().println(NMsg.ofC("Uncompressing %s", example));
            NUncompress.of(session)
                    .setSource(example)
                    .setTarget(example.resolveSibling("example-uncompressed"))
                    .run();
        }
    }
}
