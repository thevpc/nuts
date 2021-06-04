package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultNutsDescriptorParser implements NutsDescriptorParser {

    private NutsWorkspace ws;
    private NutsSession session;
    private boolean lenient = true;

    public DefaultNutsDescriptorParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDescriptorParser setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsDescriptorParser setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsDescriptor parse(URL url) {
        checkSession();
        try {
            try (InputStream is = NutsWorkspaceUtils.of(getSession()).openURL(url)) {
                return parse(is, true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException(getSession(), "unable to parse url " + url, ex);
            }
        } catch (IOException ex) {
            throw new NutsParseException(getSession(), "unable to parse url " + url, ex);
        }
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(getWorkspace(), getSession());
    }

    @Override
    public NutsDescriptor parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes), true);
    }

    @Override
    public NutsDescriptor parse(Path path) {
        checkSession();
        if (!Files.exists(path)) {
            throw new NutsNotFoundException(getSession(), "at file " + path);
        }
        try {
            return parse(Files.newInputStream(path), true);
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsParseException(getSession(), "Unable to parse file " + path, ex);
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
            return getWorkspace().elem()
                    .setSession(session)
                    .setContentType(NutsContentType.JSON).parse(rr, NutsDescriptor.class);
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
