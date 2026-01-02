/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NElementWriter;


import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.runtime.standalone.format.plain.NWriterPlain;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMemorySize;
import net.thevpc.nuts.runtime.standalone.format.obj.RollingFileService;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNObjectObjectWriter extends DefaultObjectWriterBase<NObjectObjectWriter> implements NObjectObjectWriter {


    private String formatMode;
    private String formatString;
    private Map<String, Object> formatParams = new HashMap<>();
    private boolean compact;
    private NContentType outputFormat;
    private List<String[]> confCmds = new ArrayList<>();

    public DefaultNObjectObjectWriter(NWorkspace workspace) {
        super("object-format");
    }

    @Override
    public String getFormatMode() {
        return formatMode;
    }

    @Override
    public NObjectObjectWriter setFormatMode(String formatMode) {
        this.formatMode = formatMode;
        return this;
    }

    @Override
    public String getFormatString() {
        return formatString;
    }

    @Override
    public NObjectObjectWriter setFormatString(String formatString) {
        this.formatString = formatString;
        return this;
    }

    @Override
    public Map<String, Object> getFormatParams() {
        return formatParams;
    }

    @Override
    public NObjectObjectWriter setFormatParams(Map<String, Object> formatParams) {
        this.formatParams = formatParams;
        return this;
    }

    @Override
    public NObjectObjectWriter setFormatParam(String name, Object value) {
        if (value == null) {
            if (this.formatParams != null) {
                this.formatParams.remove(name);
            }
        } else {
            if (this.formatParams == null) {
                this.formatParams = new LinkedHashMap<>();
            }
            this.formatParams.put(name, value);
        }
        return this;
    }

    @Override
    public NContentType getOutputFormat() {
        return outputFormat;
    }

    @Override
    public NObjectObjectWriter setOutputFormat(NContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }


    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public DefaultNObjectObjectWriter setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NFormatAndValue<Object, NContentTypeWriter> getBase(Object aValue) {
        NSession session = NSession.of();
        NFormatAndValue<Object, NContentTypeWriter> base = createObjectFormat(aValue);
        base.getFormat().configure(true, NWorkspace.of().getBootOptions().getOutputFormatOptions().orElseGet(Collections::emptyList).toArray(new String[0]));
        base.getFormat().configure(true, session.getOutputFormatOptions().toArray(new String[0]));
        return base;
    }

    public NFormatAndValue<Object, NContentTypeWriter> createObjectFormat(Object value) {
        NSession session = NSession.of();
        String formatMode = getFormatMode();
        String type2 = formatMode == null ? "" : NNameFormat.CLASS_NAME.format(NStringUtils.trim(formatMode));
        switch (type2) {
            case "Byte":
            case "Bytes":
            case "Memory": {
                value = NOptional.of(new NMemorySize(((Number) value).longValue(), 0, false));
                break;
            }
            case "RollingPath": {
                if (value instanceof NPath) {
                    //
                } else if (value instanceof File) {
                    value = NPath.of((File) value);
                } else if (value instanceof URL) {
                    value = NPath.of((URL) value);
                } else if (value instanceof String) {
                    value = NPath.of((String) value);
                } else if (value instanceof NPathSPI) {
                    value = NPath.of((NPathSPI) value);
                } else {
                    throw new NIllegalArgumentException(NMsg.ofC("invalid RollingPath value %s", value));
                }
                NLiteral count = NLiteral.of(formatParams != null ? formatParams.get("count") : null);
                RollingFileService fs = new RollingFileService(
                        (NPath) value,
                        count.asInt().isPresent() ? count.asInt().get() : 3
                );
                value = fs.roll();
                break;
            }
        }
        switch (session.getOutputFormat().orDefault()) {
            //structured formats!
            case XML:
            case JSON:
            case TSON:
            case YAML: {
                NElementWriter ee = NElementWriter.of().setNtf(isNtf())
                        .setCompact(isCompact())
                        .setContentType(session.getOutputFormat().orDefault());
                Object aValue = null;
                if (value instanceof NText) {
                    NTextBuilder builder = ((NText) value).builder();
                    Object[] r = builder.lines().map(
                            NFunction.of(
                                    (NTextBuilder x) -> {
                                        if (true) {
                                            return x.filteredText();
                                        }
                                        return (Object) x.filteredText();
                                    }

                            ).redescribe(NElementDescribables.ofDesc("filteredText"))
                    ).toArray(Object[]::new);
                    aValue = r;
                } else {
                    aValue = value;
                }
                for (String[] confCmd : confCmds) {
                    ee.configure(true, confCmd);
                }
                return new NFormatAndValue<>(aValue, ee);
            }
            case PROPS: {
                Object aValue = null;
                NPropertiesWriter ee = NPropertiesWriter.of().setNtf(isNtf());
                if (value instanceof NText) {
                    NTextBuilder builder = ((NText) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    aValue = r;
                } else {
                    aValue = value;
                }
                for (String[] confCmd : confCmds) {
                    ee.configure(true, confCmd);
                }
                return new NFormatAndValue<>(aValue, ee);
            }
            case TREE: {
                Object aValue = null;
                NTreeObjectWriter ee = NTreeObjectWriter.of().setNtf(isNtf());
                if (value instanceof NText) {
                    NTextBuilder builder = ((NText) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    aValue = r;
                } else {
                    aValue = value;
                }
                for (String[] confCmd : confCmds) {
                    ee.configure(true, confCmd);
                }
                return new NFormatAndValue<>(aValue, ee);
            }
            case TABLE: {
                NTableWriter ee = NTableWriter.of().setNtf(isNtf());
                Object aValue = null;
                if (value instanceof NText) {
                    NTextBuilder builder = ((NText) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    aValue = r;
                } else {
                    aValue = value;
                }
                for (String[] confCmd : confCmds) {
                    ee.configure(true, confCmd);
                }
                return new NFormatAndValue<>(aValue, ee);
            }
            case PLAIN: {
                NWriterPlain ee = new NWriterPlain().setCompact(isCompact()).setNtf(isNtf());
                for (String[] confCmd : confCmds) {
                    ee.configure(true, confCmd);
                }
                return new NFormatAndValue<>(value, ee);
            }
        }
        throw new NUnsupportedEnumException(session.getOutputFormat().orDefault());
    }

//    @Override
//    public NSession getSession() {
//        return base != null ? base.getSession() : super.getSession();
//    }

    @Override
    public NText format(Object aValue) {
        return getBase(aValue).getFormat().format(aValue);
    }

    @Override
    public void print(Object aValue) {
        getBase(aValue).getFormat().print(aValue);
    }

    @Override
    public void println(Object aValue) {
        getBase(aValue).getFormat().println(aValue);
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        getBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, Writer out) {
        getBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, Path out) {
        getBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, File out) {
        getBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, NTerminal terminal) {
        getBase(aValue).getFormat().print(aValue, terminal);
    }

    @Override
    public void println(Object aValue, Writer w) {
        getBase(aValue).getFormat().println(aValue, w);
    }

    @Override
    public void println(Object aValue, NPrintStream out) {
        getBase(aValue).getFormat().println(aValue, out);
    }

    @Override
    public void println(Object aValue, Path path) {
        getBase(aValue).getFormat().println(aValue, path);
    }

    @Override
    public void println(Object aValue, NTerminal terminal) {
        getBase(aValue).getFormat().println(aValue, terminal);
    }

    @Override
    public void println(Object aValue, File file) {
        getBase(aValue).getFormat().println(aValue, file);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NOptional<NArg> peek = cmdLine.peek();
        if (peek.isPresent()) {
            NOptional<NArg> n = cmdLine.next();
            confCmds.add(new String[]{n.get().image()});
            return true;
        }
        return false;
    }

    @Override
    public boolean configure(boolean skipUnsupported, NCmdLine cmdLine) {
        String[] a = cmdLine.toStringArray();
        cmdLine.skipAll();
        confCmds.add(a);
        return a.length > 0;
    }

    @Override
    public void configureLast(NCmdLine cmdLine) {
        configure(true, cmdLine);
    }
}
