package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFormat;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsParseException;
import net.vpc.app.nuts.core.format.DefaultFormatBase;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

public class DefaultNutsDescriptorFormat extends DefaultFormatBase<NutsDescriptorFormat> implements NutsDescriptorFormat {

    private boolean compact;
    private NutsDescriptor desc;

    public DefaultNutsDescriptorFormat(NutsWorkspace ws) {
        super(ws, "descriptor-format");
    }

    @Override
    public NutsDescriptorFormat compact(boolean compact) {
        return setCompact(compact);
    }

    @Override
    public NutsDescriptorFormat compact() {
        return compact(true);
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NutsDescriptorFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NutsDescriptor getDescriptor() {
        return desc;
    }

    public NutsDescriptorFormat setDescriptor(NutsDescriptor desc) {
        this.desc = desc;
        return this;
    }

    public NutsDescriptorFormat set(NutsDescriptor desc) {
        return setDescriptor(desc);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        return false;
    }

    @Override
    public void print(Writer out) {
        ws.format().json().compact(isCompact()).set(desc).print(out);
    }

    @Override
    public NutsDescriptor read(URL url) {
        try {
            try {
                return read(url.openStream(), true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException(ws, "Unable to parse url " + url, ex);
            }
        } catch (IOException ex) {
            throw new NutsParseException(ws, "Unable to parse url " + url, ex);
        }
    }

    @Override
    public NutsDescriptor read(byte[] bytes) {
        return read(new ByteArrayInputStream(bytes), true);
    }

    @Override
    public NutsDescriptor read(Path path) {
        if (!Files.exists(path)) {
            throw new NutsNotFoundException(ws, "at file " + path);
        }
        try {
            return read(Files.newInputStream(path), true);
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsParseException(ws, "Unable to parse file " + path, ex);
        }
    }

    @Override
    public NutsDescriptor read(File file) {
        return read(file.toPath());
    }

    @Override
    public NutsDescriptor read(String str) {
        if (CoreStringUtils.isBlank(str)) {
            return null;
        }
        return read(new ByteArrayInputStream(str.getBytes()), true);
    }

    private NutsDescriptor read(InputStream in, boolean closeStream) {
        try (Reader rr = new InputStreamReader(in)) {
            return ws.format().json().read(rr, NutsDescriptor.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

//    @Override
//    public NutsDescriptor descriptor(File file) {
//        return CoreNutsUtils.parseNutsDescriptor(file);
//    }
    @Override
    public NutsDescriptor read(InputStream stream) {
        return read(stream, false);
    }

}
