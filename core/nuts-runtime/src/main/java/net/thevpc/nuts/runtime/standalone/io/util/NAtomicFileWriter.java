package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.io.NIOException;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Writes text files atomically so that a crash or a concurrent process can
 * never observe a truncated configuration file. The content is written to a
 * sibling temporary file, flushed and fsynced, then moved into place with
 * {@link StandardCopyOption#ATOMIC_MOVE} when the filesystem supports it.
 */
public final class NAtomicFileWriter {

    private NAtomicFileWriter() {
    }

    /** Callback that produces the file content. */
    public interface WriterTask {
        void write(Writer out) throws IOException;
    }

    public static void write(Path target, Charset charset, WriterTask task) {
        Path parent = target.toAbsolutePath().getParent();
        Path tmp = null;
        try {
            Files.createDirectories(parent);
            tmp = Files.createTempFile(parent, target.getFileName().toString() + ".", ".part");
            try (Writer w = Files.newBufferedWriter(tmp, charset,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                task.write(w);
                w.flush();
            }
            try (FileChannel channel = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                // best effort: a plain replace is still safer than in-place truncation
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            tmp = null;
        } catch (IOException ex) {
            throw new NIOException(ex);
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            }
        }
    }

    public static void writeString(Path target, String value, Charset charset) {
        write(target, charset, out -> out.write(value));
    }
}
