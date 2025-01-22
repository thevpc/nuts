package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.md.MdElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DocusaurusPathFile extends DocusaurusFile {
    private final NPath path;

    public DocusaurusPathFile(String shortId, String longId, String title, NPath path, int order, NElement config) {
        super(shortId, longId, title, order, config);
        this.path = path;
    }

    public static DocusaurusFile ofFile(String id, String longId, String title, NPath path, int menuOrder, NElement config) {
        return new DocusaurusPathFile(id, longId, title, path, menuOrder, config);
    }

    public static DocusaurusFile ofFile(NPath path, NPath root) {
        int from = root.getNameCount();
        int to = path.getNameCount() - 1;
        String partialPath = from == to ? "" : path.subpath(from, to).toString();
        try (BufferedReader br = path.getBufferedReader()) {
            DocusaurusFile df = DocusaurusContentFile.ofTreeFile(br, partialPath, path.toString(), false);
            if (df != null && df.getShortId() != null) {
                return ofFile(df.getShortId(),
                        df.getLongId(), df.getTitle(), path, df.getOrder(), df.getConfig()
                );
            }
            return null;
        } catch (IOException iOException) {
            //
        }
        return null;
    }

    public NPath getPath() {
        return path;
    }

    public MdElement getContent() {
        try (Reader reader = getPath().getBufferedReader()) {
            DocusaurusFile tree = DocusaurusContentFile.ofTreeFile(reader, getLongId(), getLongId(),
                    true);
            return tree != null ? tree.getContent() : null;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
