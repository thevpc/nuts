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

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NElementFormat;


import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMemorySize;
import net.thevpc.nuts.runtime.standalone.format.obj.RollingFileService;
import net.thevpc.nuts.runtime.standalone.format.plain.NFormatPlain;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNObjectFormat extends DefaultFormatBase<NObjectFormat> implements NObjectFormat {

    private Object value;

    private String formatMode;
    private String formatString;
    private Map<String, Object> formatParams = new HashMap<>();
    private boolean compact;
    private NContentType outputFormat;

    public DefaultNObjectFormat(NWorkspace workspace) {
        super("object-format");
    }

    @Override
    public String getFormatMode() {
        return formatMode;
    }

    @Override
    public NObjectFormat setFormatMode(String formatMode) {
        this.formatMode = formatMode;
        return this;
    }

    @Override
    public String getFormatString() {
        return formatString;
    }

    @Override
    public NObjectFormat setFormatString(String formatString) {
        this.formatString = formatString;
        return this;
    }

    @Override
    public Map<String, Object> getFormatParams() {
        return formatParams;
    }

    @Override
    public NObjectFormat setFormatParams(Map<String, Object> formatParams) {
        this.formatParams = formatParams;
        return this;
    }

    @Override
    public NObjectFormat setFormatParam(String name, Object value) {
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
    public NObjectFormat setOutputFormat(NContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NObjectFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public DefaultNObjectFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NContentTypeFormat getBase() {
        NSession session=NSession.of();
        NContentTypeFormat base = createObjectFormat();
        base.configure(true, NWorkspace.of().getBootOptions().getOutputFormatOptions().orElseGet(Collections::emptyList).toArray(new String[0]));
        base.configure(true, session.getOutputFormatOptions().toArray(new String[0]));
        return base;
    }

    public NContentTypeFormat createObjectFormat() {
        NSession session=NSession.of();
        Object value = getValue();
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
                NElementFormat ee = NElementFormat.of().setNtf(isNtf())
                        .setCompact(isCompact())
                        .setContentType(session.getOutputFormat().orDefault());
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
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case PROPS: {
                NPropertiesFormat ee = NPropertiesFormat.of().setNtf(isNtf());
                if (value instanceof NText) {
                    NTextBuilder builder = ((NText) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case TREE: {
                NTreeFormat ee = NTreeFormat.of().setNtf(isNtf());
                if (value instanceof NText) {
                    NTextBuilder builder = ((NText) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case TABLE: {
                NTableFormat ee = NTableFormat.of().setNtf(isNtf());
                if (value instanceof NText) {
                    NTextBuilder builder = ((NText) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case PLAIN: {
                NFormatPlain ee = new NFormatPlain().setCompact(isCompact()).setNtf(isNtf());
                ee.setValue(value);
                return ee;
            }
        }
        throw new NUnsupportedEnumException(session.getOutputFormat().orDefault());
    }

//    @Override
//    public NSession getSession() {
//        return base != null ? base.getSession() : super.getSession();
//    }

    @Override
    public NText format() {
        return getBase().format();
    }

    @Override
    public void print() {
        getBase().print();
    }

    @Override
    public void println() {
        getBase().println();
    }

    @Override
    public void print(NPrintStream out) {
        getBase().print(out);
    }

    @Override
    public void print(Writer out) {
        getBase().print(out);
    }

    @Override
    public void print(Path out) {
        getBase().print(out);
    }

    @Override
    public void print(File out) {
        getBase().print(out);
    }

    @Override
    public void print(NTerminal terminal) {
        getBase().print(terminal);
    }

    @Override
    public void println(Writer w) {
        getBase().println(w);
    }

    @Override
    public void println(NPrintStream out) {
        getBase().println(out);
    }

    @Override
    public void println(Path path) {
        getBase().println(path);
    }

    @Override
    public void println(NTerminal terminal) {
        getBase().println(terminal);
    }

    @Override
    public void println(File file) {
        getBase().println(file);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return getBase().configureFirst(cmdLine);
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}
