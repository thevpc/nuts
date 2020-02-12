package net.vpc.app.nuts.runtime.format;

import net.vpc.app.nuts.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import net.vpc.app.nuts.runtime.DefaultNutsClassifierMappingBuilder;
import net.vpc.app.nuts.runtime.config.DefaultNutsIdLocationBuilder;
import net.vpc.app.nuts.runtime.config.DefaultNutsArtifactCallBuilder;
import net.vpc.app.nuts.runtime.config.DefaultNutsDescriptorBuilder;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

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

    public NutsDescriptorFormat value(NutsDescriptor desc) {
        return setDescriptor(desc);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        return false;
    }

    @Override
    public void print(PrintStream out) {
        getWorkspace().json().compact(isCompact()).value(desc).print(out);
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
            return getWorkspace().json().parse(rr, NutsDescriptor.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public NutsDescriptor parse(InputStream stream) {
        return parse(stream, false);
    }

    @Override
    public NutsDescriptorBuilder descriptorBuilder() {
        return new DefaultNutsDescriptorBuilder();
    }

    @Override
    public NutsClassifierMappingBuilder classifierBuilder() {
        return new DefaultNutsClassifierMappingBuilder();
    }

    @Override
    public NutsIdLocationBuilder locationBuilder() {
        return new DefaultNutsIdLocationBuilder();
    }

    @Override
    public NutsArtifactCallBuilder callBuilder() {
        return new DefaultNutsArtifactCallBuilder();
    }
}
