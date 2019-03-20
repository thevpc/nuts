package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import net.vpc.common.fprint.parser.DefaultFormattedPrintStreamParser;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.net.URL;
import net.vpc.app.nuts.core.filters.version.DefaultNutsVersionFilter;

public class DefaultNutsParseManager implements NutsParseManager {

    private NutsWorkspace ws;

    public DefaultNutsParseManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsId parseId(String id) {
        return CoreNutsUtils.parseNutsId(id);
    }

    @Override
    public NutsDescriptor parseDescriptor(URL url) {
        try {
            try {
                return parseDescriptor(url.openStream(), true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException("Unable to parse url " + url, ex);
            }
        } catch (IOException ex) {
            throw new NutsParseException("Unable to parse url " + url, ex);
        }
    }

    @Override
    public NutsDescriptor parseDescriptor(byte[] bytes) {
        return parseDescriptor(new ByteArrayInputStream(bytes), true);
    }

    public NutsDescriptor parseDescriptor(File file) {
        if (!file.exists()) {
            throw new NutsNotFoundException("at file " + file);
        }
        try {
            return parseDescriptor(new FileInputStream(file), true);
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsParseException("Unable to parse file " + file, ex);
        }
    }

    public NutsDescriptor parseDescriptor(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return parseDescriptor(new ByteArrayInputStream(str.getBytes()), true);
    }

    public NutsDescriptor parseDescriptor(InputStream in, boolean closeStream) {
        try {
            return ws.getIOManager().readJson(new InputStreamReader(in), NutsDescriptor.class);
        } finally {
            if (closeStream) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new NutsIOException(e);
                }
            }
        }
    }

//    @Override
//    public NutsDescriptor parseDescriptor(File file) {
//        return CoreNutsUtils.parseNutsDescriptor(file);
//    }
    @Override
    public NutsDescriptor parseDescriptor(InputStream stream) {
        return parseDescriptor(stream, false);
    }

    @Override
    public NutsDependency parseDependency(String dependency) {
        return CoreNutsUtils.parseNutsDependency(dependency);
    }

    @Override
    public NutsId parseRequiredId(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    @Override
    public NutsVersionFilter parseVersionFilter(String versionFilter) {
        return DefaultNutsVersionFilter.parse(versionFilter);
    }

    @Override
    public NutsVersion parseVersion(String version) {
        return DefaultNutsVersion.valueOf(version);
    }

    @Override
    public NutsExecutionEntry[] parseExecutionEntries(File file) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
            try {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    return parseExecutionEntries(in, "java");
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            } catch (IOException ex) {
                throw new NutsIOException(ex);
            }
        } else {
            return new NutsExecutionEntry[0];
        }
    }

    @Override
    public NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type) {
        if ("java".equals(type)) {
            return CorePlatformUtils.parseMainClasses(inputStream);
        }
        return new NutsExecutionEntry[0];
    }

    @Override
    public String filterText(String value) {
        return DefaultFormattedPrintStreamParser.INSTANCE.filterText(value);
    }

    @Override
    public String escapeText(String str) {
        if (str == null) {
            return "";
        }
        return DefaultFormattedPrintStreamParser.INSTANCE.escapeText(str);
    }

}
