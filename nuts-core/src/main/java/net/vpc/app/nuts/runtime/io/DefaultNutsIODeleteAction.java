package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.NutsIODeleteAction;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

public class DefaultNutsIODeleteAction extends AbstractNutsIODeleteAction {
    private Exception error;

    public DefaultNutsIODeleteAction(NutsWorkspace ws) {
        super(ws);
    }

    private void grabException(IOException e) {
        this.error = e;
        if (isFailFast()) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public NutsIODeleteAction run() {
        Path t = CoreIOUtils.toPath(getTarget());
        if (t == null) {
            if (getTarget() == null) {
                throw new NutsException(getWs(), "Missing Target to delete");
            }
            throw new NutsException(getWs(), "Unsupported Target to delete");
        }
        if (!Files.exists(t)) {
            grabException(new FileNotFoundException(t.toString()));
            return this;
        }
        if (Files.isRegularFile(t)) {
            try {
                Files.delete(t);
            } catch (IOException e) {
                grabException(e);
                return this;
            }
            return this;
        }
        final int[] deleted = new int[]{0, 0, 0};
        NutsLogger LOG = getWs().log().of(CoreIOUtils.class);
        try {
            Files.walkFileTree(t, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        Files.delete(file);
                        if (LOG != null) {
                            LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete file " + file);
                        }
                        deleted[0]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete file Failed : " + file);
                        }
                        deleted[2]++;
                        grabException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.delete(dir);
                        if (LOG != null) {
                            LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete folder " + dir);
                        }
                        deleted[1]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete folder Failed : " + dir);
                        }
                        deleted[2]++;
                        grabException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            if (error != null) {
                grabException(e);
            }
        }
        return this;
    }
}
