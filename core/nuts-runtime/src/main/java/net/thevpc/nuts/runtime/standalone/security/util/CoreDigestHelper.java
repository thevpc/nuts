package net.thevpc.nuts.runtime.standalone.security.util;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CoreDigestHelper {
    private final List<Throwable> errors = new ArrayList<>();
    private final MessageDigest md;
    private boolean collected;
    private String collectedString;
    private NutsSession session;

    public CoreDigestHelper(NutsSession session) {
        this.session=session;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public CoreDigestHelper append(List<URL> urls) {
        return append(urls.toArray(new URL[0]));
    }

    public CoreDigestHelper append(URL... urls) {
        try {
            if (urls != null) {
                for (URL url : urls) {
                    append(url);
                }
            }
        } catch (Exception e) {
            errors.add(e);
        }
        return this;
    }

    public CoreDigestHelper append(URL url) {
        if (url != null) {
            Path ff = NutsPath.of(url,session).asFile();
            if (ff != null) {
                append(ff);
                return this;
            }
            InputStream is = null;
            try {
                is = url.openStream();
                if (is != null) {
                    append(is);
                }
            } catch (Exception e) {
                errors.add(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }
        return this;
    }

    public CoreDigestHelper append(Path p) {
        if (p != null) {
            if (Files.isDirectory(p)) {
                updateDirectory(p);
            } else if (Files.isRegularFile(p)) {
                try (InputStream is = Files.newInputStream(p)) {
                    append(is);
                } catch (IOException ex) {
                    errors.add(ex);
                }
            }
        }
        return this;
    }

    private void updateDirectory(Path p) {
        try {
            Files.walkFileTree(p, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    append(new ByteArrayInputStream(dir.toString().getBytes()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    append(new ByteArrayInputStream(file.toString().getBytes()));
                    append(Files.newInputStream(file));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
//            byte[] digest = md.digest();
//            return NutsUtilStrings.toHexString(digest);
        } catch (Exception e) {
            errors.add(e);
        }
    }


    public CoreDigestHelper append(InputStream is) {
        try {
            byte[] bytes = new byte[4096];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
        } catch (IOException ex) {
            errors.add(ex);
        }
        return this;
    }

    public String getDigest() {
        if (!collected) {
            collected = true;
            byte[] digest = md.digest();
            collectedString = NutsUtilStrings.toHexString(digest);
        }
        return collectedString;
    }
}
