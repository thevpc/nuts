package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsOutput;
import net.thevpc.nuts.NutsOutputAction;
import net.thevpc.nuts.NutsUnsupportedArgumentException;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.bundles.io.AbstractNutsOutput;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsOutputAction implements NutsOutputAction {

    private NutsWorkspace ws;
    private String name;
    private String typeName;
    private NutsSession session;

    public DefaultNutsOutputAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    private static NutsOutput createOutputTarget(OutputStream target, String name, String typeName, NutsSession ws) {
        if (target == null) {
            return null;
        }
        return new AbstractNutsOutput(target, false, false, name, typeName, ws) {
            @Override
            public OutputStream open() {
                return (OutputStream) getSource();
            }

            @Override
            public String toString() {
                return "OutputStream(" + getSource() + ")";
            }

            @Override
            public void close() {
                try {
                    ((OutputStream) getSource()).close();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    private static NutsOutput createOutputTarget(String target, String name, String typeName, NutsSession ws) {
        if (target == null) {
            return null;
        }
        Path basePath = null;
        try {
            basePath = Paths.get(target);
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createOutputTarget(basePath, name, typeName, ws);
        }

        try {
            basePath = Paths.get(new URL(target).toURI());
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createOutputTarget(target, name, typeName, ws);
        }

        URL baseURL = null;
        try {
            baseURL = new URL(target);
        } catch (Exception ex) {
            //
        }
        if (baseURL != null) {
            return createOutputTarget(baseURL, name, typeName, ws);
        }
        throw new NutsUnsupportedArgumentException(ws, "Unsupported source : " + target);
    }

    private static NutsOutput createOutputTarget(URL baseURL, String name, String typeName, NutsSession ws) {
        if (baseURL == null) {
            return null;
        }

        if (baseURL.getProtocol().equals("file")) {
            try {
                return createOutputTarget(Paths.get(baseURL.toURI()), name, typeName, ws);
            } catch (URISyntaxException ex) {
                throw new UncheckedIOException(new IOException(ex));
            }
        }
        throw new NutsUnsupportedArgumentException(ws, "unsupported source : " + baseURL);
    }

    private static AbstractNutsOutput createOutputTarget(Path target, String name, String typeName, NutsSession ws) {
        if (target == null) {
            return null;
        }
        return new AbstractNutsOutput(target, true, true, name == null ? target.getFileName().toString() : name, typeName, ws) {
            @Override
            public Path getPath() {
                return (Path) getSource();
            }

            @Override
            public URL getURL() {
                Path source = (Path) getSource();
                try {
                    return source.toUri().toURL();
                } catch (MalformedURLException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

            @Override
            public void close() {

            }

            @Override
            public OutputStream open() {
                try {
                    return Files.newOutputStream(getPath());
                } catch (IOException ex) {
                    throw createOpenError(ex);
                }
            }

            @Override
            public String toString() {
                return getPath().toString();
            }
        };
    }

    private static AbstractNutsOutput createOutputTarget(File target, String name, String typeName, NutsSession ws) {
        if (target == null) {
            return null;
        }
        return new AbstractNutsOutput(target, true, false, name == null ? target.getName() : name, typeName, ws) {
            @Override
            public Path getPath() {
                return ((File) getSource()).toPath();
            }

            @Override
            public URL getURL() {
                File source = (File) getSource();
                try {
                    return source.toURI().toURL();
                } catch (MalformedURLException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

            @Override
            public void close() {

            }

            @Override
            public OutputStream open() {
                try {
                    return Files.newOutputStream(getPath());
                } catch (IOException ex) {
                    throw createOpenError(ex);
                }
            }

            @Override
            public String toString() {
                return getPath().toString();
            }
        };
    }

    @Override
    public NutsOutput of(Object source) {
        checkSession();
        if (source == null) {
            return null;
        } else if (source instanceof NutsOutput) {
            return (NutsOutput) source;
        } else if (source instanceof OutputStream) {
            return of((OutputStream) source);
        } else if (source instanceof Path) {
            return of((Path) source);
        } else if (source instanceof File) {
            return of((File) source);
        } else if (source instanceof URL) {
            return of((URL) source);
        } else if (source instanceof String) {
            return of((String) source);
        } else {
            throw new NutsUnsupportedArgumentException(session, "unsupported type " + source.getClass().getName());
        }
    }

    @Override
    public NutsOutput of(String resource) {
        checkSession();
        NutsOutput v = createOutputTarget(resource, getName(), getTypeName(), session);
        v = toMulti(v);
        return v;
    }

    private NutsOutput toMulti(NutsOutput v) {
        return v;
    }

    @Override
    public NutsOutput of(OutputStream stream) {
        checkSession();
        NutsOutput v = createOutputTarget(stream, getName(), getTypeName(), session);
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsOutput of(URL stream) {
        checkSession();
        NutsOutput v = createOutputTarget(stream, getName(), getTypeName(), session);
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsOutput of(File stream) {
        checkSession();
        NutsOutput v = createOutputTarget(stream, getName(), getTypeName(), session);
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsOutput of(Path stream) {
        checkSession();
        NutsOutput v = createOutputTarget(stream, getName(), getTypeName(), session);
        v = toMulti(v);
        return v;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public NutsOutputAction setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsOutputAction setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsOutputAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }
}
