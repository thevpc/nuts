package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultTempManager implements NutsTempManager {
    private NutsWorkspace ws;

    public DefaultTempManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String createTempFile(String name, NutsSession session) {
        return createTempFile(name, null);
    }

    @Override
    public String createTempFile(String name, NutsRepository repository, NutsSession session) {
        File folder = null;
        if (repository == null) {
            folder = Paths.get(ws.locations().getStoreLocation(NutsStoreLocation.TEMP)).toFile();
        } else {
            folder = Paths.get(repository.config().getStoreLocation(NutsStoreLocation.TEMP)).toFile();
        }
        folder.mkdirs();
        String prefix = "temp-";
        String ext = null;
        if (!CoreStringUtils.isBlank(name)) {
            ext = CoreIOUtils.getFileExtension(name);
            prefix = name;
            if (prefix.length() < 3) {
                prefix = prefix + "-temp-";
            }
            if (!ext.isEmpty()) {
                ext = "." + ext;
                if (ext.length() < 3) {
                    ext = ".tmp" + ext;
                }
            } else {
                ext = "-nuts";
            }
        }
        try {
            return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), folder).toPath().toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public String createTempFolder(String name, NutsSession session) {
        return createTempFolder(name, null);
    }

    @Override
    public String createTempFolder(String name, NutsRepository repository, NutsSession session) {
        File folder = null;
        if (repository == null) {
            folder = Paths.get(ws.locations().getStoreLocation(NutsStoreLocation.TEMP)).toFile();
        } else {
            folder = Paths.get(repository.config().getStoreLocation(NutsStoreLocation.TEMP)).toFile();
        }
        folder.mkdirs();
        final File temp;
        if (CoreStringUtils.isBlank(name)) {
            name = "temp-";
        } else if (name.length() < 3) {
            name += "-temp-";
        }
        try {
            temp = File.createTempFile(name, Long.toString(System.nanoTime()), folder);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (!(temp.delete())) {
            throw new UncheckedIOException(new IOException("Could not delete temp file: " + temp.getAbsolutePath()));
        }

        if (!(temp.mkdir())) {
            throw new UncheckedIOException(new IOException("Could not create temp directory: " + temp.getAbsolutePath()));
        }

        return (temp.toPath()).toString();
    }


}
