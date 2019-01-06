package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.common.fprint.parser.DefaultFormattedPrintStreamParser;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

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
        return CoreNutsUtils.parseNutsDescriptor(url);
    }

    @Override
    public NutsDescriptor parseDescriptor(File file) {
        return CoreNutsUtils.parseNutsDescriptor(file);
    }

    @Override
    public NutsDescriptor parseDescriptor(InputStream stream) {
        return CoreNutsUtils.parseNutsDescriptor(stream, false);
    }

    @Override
    public NutsDescriptor parseDescriptor(String descriptorString) {
        return CoreNutsUtils.parseNutsDescriptor(descriptorString);
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
    public NutsVersion parseVersion(String version) {
        return DefaultNutsVersion.valueOf(version);
    }

    @Override
    public NutsExecutionEntry[] parseExecutionEntries(File file) {
        return CorePlatformUtils.parseMainClasses(file);
    }

    @Override
    public NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type) {
        return CorePlatformUtils.parseMainClasses(inputStream);
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
        boolean needQuote = false;
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '`': {
                    sb.append("\\`");
                    needQuote = true;
                    break;
                }
                case '\\': {
                    sb.append("\\\\");
                    needQuote = true;
                    break;
                }
                case '\'':
                case '"':
                case '>':
                case '}':
                case ')':
                case ']':
                case '$':
                case '£':
                case '§':
                case '_':
                case '~':
                case '%':
                case '¤':
                case '@':
                case '^':
                case '#':
                case '¨':
                case '=':
                case '*':
                case '+':
                case '(':
                case '[':
                case '{':
                case '<': {
                    sb.append(c);
                    needQuote = true;
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        if (!needQuote) {
            return sb.toString();
        }
        return "``" + sb.toString() + "``";
    }

}
