package net.vpc.app.nuts.core.parsers;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.app.NutsCommandLineUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.filters.version.DefaultNutsVersionFilter;
import net.vpc.app.nuts.core.app.DefaultNutsCommand;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

public class DefaultNutsParseManager implements NutsParseManager {

    private NutsWorkspace ws;

    public DefaultNutsParseManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsCommandLine commandLine(String line) {
        return command(NutsCommandLineUtils.parseCommandLine(ws,line));
    }

    @Override
    public NutsCommandLine command(String... arguments) {
        return new DefaultNutsCommand(ws, arguments);
    }

    @Override
    public NutsCommandLine command(Collection<String> arguments) {
        return command(arguments == null ? null : (String[]) arguments.toArray(new String[0]));
    }

    @Override
    public NutsId id(String id) {
        return CoreNutsUtils.parseNutsId(id);
    }

    @Override
    public NutsDescriptor descriptor(URL url) {
        try {
            try {
                return descriptor(url.openStream(), true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException(ws,"Unable to parse url " + url, ex);
            }
        } catch (IOException ex) {
            throw new NutsParseException(ws,"Unable to parse url " + url, ex);
        }
    }

    @Override
    public NutsDescriptor descriptor(byte[] bytes) {
        return descriptor(new ByteArrayInputStream(bytes), true);
    }

    @Override
    public NutsDescriptor descriptor(Path path) {
        if (!Files.exists(path)) {
            throw new NutsNotFoundException(ws,"at file " + path);
        }
        try {
            return descriptor(Files.newInputStream(path), true);
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsParseException(ws,"Unable to parse file " + path, ex);
        }
    }

    @Override
    public NutsDescriptor descriptor(File file) {
        return descriptor(file.toPath());
    }

    @Override
    public NutsDescriptor descriptor(String str) {
        if (CoreStringUtils.isBlank(str)) {
            return null;
        }
        return descriptor(new ByteArrayInputStream(str.getBytes()), true);
    }

    @Override
    public NutsDescriptor descriptor(InputStream in, boolean closeStream) {
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
    public NutsDescriptor descriptor(InputStream stream) {
        return descriptor(stream, false);
    }

    @Override
    public NutsDependency dependency(String dependency) {
        return CoreNutsUtils.parseNutsDependency(ws, dependency);
    }

    @Override
    public NutsId requiredId(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException(ws,"Invalid Id format : " + nutFormat);
        }
        return id;
    }

    @Override
    public NutsVersionFilter versionFilter(String versionFilter) {
        return DefaultNutsVersionFilter.parse(versionFilter);
    }

    @Override
    public NutsVersion version(String version) {
        return DefaultNutsVersion.valueOf(version);
    }

    @Override
    public NutsExecutionEntry[] executionEntries(File file) {
        return executionEntries(file.toPath());
    }

    @Override
    public NutsExecutionEntry[] executionEntries(Path file) {
        if (file.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return executionEntries(in, "java", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else if (file.getFileName().toString().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return executionEntries(in, "class", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            return new NutsExecutionEntry[0];
        }
    }

    @Override
    public NutsExecutionEntry[] executionEntries(InputStream inputStream, String type, String sourceName) {
        if ("java".equals(type)) {
            return CorePlatformUtils.parseJarExecutionEntries(inputStream, sourceName);
        } else if ("class".equals(type)) {
            NutsExecutionEntry u = CorePlatformUtils.parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? new NutsExecutionEntry[0] : new NutsExecutionEntry[]{u};
        }
        return new NutsExecutionEntry[0];
    }

    @Override
    public Object parseExpression(Object object, String expression) {
        int x = expression.indexOf('.');
        if (x < 0) {
            if (object instanceof Map) {
                for (Object o : ((Map) object).keySet()) {
                    String k = String.valueOf(o);
                    if (k.equals(expression)) {
                        return ((Map) object).get(o);
                    }
                }
                return null;
            }
            expression = Character.toUpperCase(expression.charAt(0)) + expression.substring(1);
            Method m = null;
            try {
                m = object.getClass().getDeclaredMethod("get" + expression);
                return m.invoke(object);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            Object o = parseExpression(object, expression.substring(0, x));
            return parseExpression(o, expression.substring(x + 1));
        }
    }
}
