package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNutsDescriptorParser implements NutsDescriptorParser {

    private NutsWorkspace ws;
    private NutsSession session;
    private boolean lenient = true;
    private NutsDescriptorStyle descriptorStyle;
    private String format;

    public DefaultNutsDescriptorParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDescriptor parse(URL url) {
        checkSession();
        return parse(getSession().io().path(url));
    }

    @Override
    public NutsDescriptor parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes), true);
    }

    @Override
    public NutsDescriptor parse(Path path) {
        checkSession();
        return parse(getSession().io().path(path));
    }

    @Override
    public NutsDescriptor parse(File file) {
        checkSession();
        return parse(file.toPath());
    }

    @Override
    public NutsDescriptor parse(InputStream stream) {
        checkSession();
        return parse(stream, false);
    }

    @Override
    public NutsDescriptor parse(NutsPath path) {
        checkSession();
        boolean startParsing = false;
        try {
            try (InputStream is = path.getInputStream()) {
                startParsing = true;
                return parse(is, true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse url %s", path), ex);
            }
        } catch (IOException ex) {
            if (!startParsing) {
                throw new NutsNotFoundException(getSession(), null, NutsMessage.cstyle("at file %s", path), null);
            }
            throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse url %s", path), ex);
        }
    }

    @Override
    public NutsDescriptor parse(String str) {
        checkSession();
        if (NutsBlankable.isBlank(str)) {
            return null;
        }
        return parse(new ByteArrayInputStream(str.getBytes()), true);
    }

    @Override
    public NutsDescriptorStyle getDescriptorStyle() {
        return descriptorStyle;
    }

    @Override
    public DefaultNutsDescriptorParser setDescriptorStyle(NutsDescriptorStyle descriptorStyle) {
        this.descriptorStyle = descriptorStyle;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsDescriptorParser setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
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

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(getWorkspace(), getSession());
    }

    private NutsDescriptor parse(InputStream in, boolean closeStream) {
        checkSession();
        NutsDescriptorStyle style = getDescriptorStyle();
        if (style == null) {
            style = NutsDescriptorStyle.NUTS;
        }
        switch (style) {
            case MAVEN: {
                try {
                    return MavenUtils.of(session).parsePomXml0(in, NutsFetchMode.LOCAL, "descriptor", null);
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                }
            }
            case NUTS: {
                try {
                    Reader rr = new InputStreamReader(in);
                    return getSession().elem()
                            .setSession(session)
                            .setContentType(NutsContentType.JSON).parse(rr, NutsDescriptor.class);
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                }
            }
            default: {
                throw new NutsUnsupportedEnumException(getSession(), style);
            }
        }
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }
}
