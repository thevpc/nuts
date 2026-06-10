package net.thevpc.nuts.ext.compression;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.spi.NUncompressPackaging;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;

public class NUncompressTar implements NUncompressPackaging {

    public NUncompressTar() {
    }

    /**
     * Factory method to allow subclasses (like TarGz) to wrap the stream differently.
     */
    protected TarArchiveInputStream createTarInputStream(InputStream in) throws IOException {
        return new TarArchiveInputStream(in);
    }

    private String extractRoot(String fileName) {
        int si = fileName.indexOf("/");
        if (si >= 0) {
            return fileName.substring(0, si + 1);
        }
        return "";
    }

    @Override
    public void uncompressPackage(NUncompress uncompress, NInputSource source) {
        NOutputTarget target0 = uncompress.target();
        try {
            NPath _target = asValidTargetPath(target0);
            if (_target == null) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid target %s", target0));
            }
            Path folder = _target.toPath().get();
            NPath.of(folder).mkdirs();

            byte[] buffer = new byte[10 * 4 * 1024];
            InputStream _in = source.inputStream();
            try {
                try (TarArchiveInputStream tis = createTarInputStream(_in)) {
                    TarArchiveEntry entry;
                    String root = null;
                    while ((entry = tis.getNextTarEntry()) != null) {
                        String fileName0 = entry.getName();
                        String fileName = fileName0;

                        if (uncompress.isSkipRoot()) {
                            String root2 = extractRoot(fileName);
                            if (root == null) {
                                root = root2;
                            } else if (!Objects.equals(root2, root)) {
                                throw new IOException("not a single root tar : '" + root2 + "' <> '" + root + "'");
                            }
                            fileName = fileName.substring(root.length());
                            if (fileName.isEmpty() && entry.isDirectory()) {
                                continue; // skip the root directory
                            }
                        }
                        Path targetPath = folder.resolve(fileName);
                        NPath target = NPath.of(targetPath);

                        if (entry.isDirectory()) {
                            target.mkdirs();
                        }
                        else if (entry.isSymbolicLink()) {
                            // Create parent directories if needed
                            target.mkParentDirs();
                            String linkTarget = entry.getLinkName();
                            Files.createSymbolicLink(targetPath, Paths.get(linkTarget));
                            _LOG().log(NMsg.ofJ("symlink: {0} -> {1}", target, linkTarget).asFinestAlert());
                        }
                        else if (entry.isLink()) {
                            // Hard link – we can either copy the content or create a hard link.
                            // For simplicity, treat as regular file (copy content).
                            // More advanced: use Files.createLink(targetPath, Paths.get(entry.getLinkName()))
                            target.mkParentDirs();
                            try (OutputStream fos = Files.newOutputStream(targetPath)) {
                                int len;
                                while ((len = tis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                            _LOG().log(NMsg.ofJ("hard link copied as file: {0} -> {1}", target, entry.getLinkName()).asConfig());
                        }
                        else if (entry.isFile()) {
                            target.mkParentDirs();
                            try (OutputStream fos = Files.newOutputStream(targetPath)) {
                                int len;
                                while ((len = tis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        else {
                            // Unknown type (e.g., character device, block device, FIFO) – ignore or log warning
                            _LOG().log(NMsg.ofJ("skipping unsupported tar entry: {0}", fileName).asConfig());
                        }
                    }
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOG().log(NMsg.ofJ("error uncompressing {0} to {1} : {2}", source, target0, ex)
                    .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL));
            throw new NIOException(ex);
        }
    }

    @Override
    public void visitPackage(NUncompress uncompress, NInputSource source, NUncompressVisitor visitor) {
        try {
            InputStream _in = source.inputStream();
            try {
                try (TarArchiveInputStream tis = createTarInputStream(_in)) {
                    TarArchiveEntry entry;
                    String root = null;
                    while ((entry = tis.getNextTarEntry()) != null) {
                        String fileName = entry.getName();

                        if (uncompress.isSkipRoot()) {
                            if (root == null) {
                                if (entry.isDirectory()) {
                                    root = fileName;
                                    continue; // Skip visiting the root folder itself
                                } else {
                                    throw new IOException("not a single root tar");
                                }
                            }
                            if (fileName.startsWith(root)) {
                                fileName = fileName.substring(root.length());
                            } else {
                                throw new IOException("not a single root tar");
                            }
                        }

                        if (entry.isDirectory()) {
                            if (!visitor.visitFolder(fileName)) {
                                break;
                            }
                        } else {
                            if (!visitor.visitFile(fileName, new InputStream() {
                                @Override
                                public int read() throws IOException {
                                    return tis.read();
                                }

                                @Override
                                public int read(byte[] b, int off, int len) throws IOException {
                                    return tis.read(b, off, len);
                                }

                                @Override
                                public int read(byte[] b) throws IOException {
                                    return tis.read(b);
                                }
                            })) {
                                break;
                            }
                        }
                    }
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOG().log(NMsg.ofJ("error visiting {0} : {1}", source, ex).asConfig().withIntent(NMsgIntent.FAIL));
            throw new NIOException(ex);
        }
    }

    protected NLog _LOG() {
        return NLog.of(NUncompressTar.class);
    }

    private NPath asValidTargetPath(NOutputTarget target) {
        if (target != null) {
            if (target instanceof NPath) {
                NPath p = (NPath) target;
                return p;
            }
        }
        return null;
    }

    @NScore(fixed = NScorable.DEFAULT_SCORE)
    public static int getScore(NScorableContext context) {
        String z = NStringUtils.trim(context.criteria(String.class)).toLowerCase();
        if (z.equals("tar")) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}