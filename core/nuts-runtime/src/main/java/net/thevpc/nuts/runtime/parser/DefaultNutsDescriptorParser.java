package net.thevpc.nuts.runtime.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultNutsDescriptorParser implements NutsDescriptorParser {
    private NutsWorkspace ws;
    private boolean lenient=true;

    public DefaultNutsDescriptorParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDescriptorParser setLenient(boolean lenient) {
        this.lenient =lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsDescriptor parse(URL url) {
        try {
            try (InputStream is = url.openStream()) {
                return parse(is, true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException(getWorkspace(), "Unable to parse url " + url, ex);
            }
        } catch (IOException ex) {
            throw new NutsParseException(getWorkspace(), "Unable to parse url " + url, ex);
        }
    }

    @Override
    public NutsDescriptor parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes), true);
    }

    @Override
    public NutsDescriptor parse(Path path) {
        if (!Files.exists(path)) {
            throw new NutsNotFoundException(getWorkspace(), "at file " + path);
        }
        try {
            return parse(Files.newInputStream(path), true);
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsParseException(getWorkspace(), "Unable to parse file " + path, ex);
        }
    }

    @Override
    public NutsDescriptor parse(File file) {
        return parse(file.toPath());
    }

    @Override
    public NutsDescriptor parse(String str) {
        if (CoreStringUtils.isBlank(str)) {
            return null;
        }
        return parse(new ByteArrayInputStream(str.getBytes()), true);
    }

    private NutsDescriptor parse(InputStream in, boolean closeStream) {
        try (Reader rr = new InputStreamReader(in)) {
            return getWorkspace().formats().json().parse(rr, NutsDescriptor.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public NutsDescriptor parse(InputStream stream) {
        return parse(stream, false);
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }
}
