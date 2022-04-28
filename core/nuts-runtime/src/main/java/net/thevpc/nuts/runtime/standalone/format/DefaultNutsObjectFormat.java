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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.plain.NutsFormatPlain;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author thevpc
 */
public class DefaultNutsObjectFormat extends DefaultFormatBase<NutsObjectFormat> implements NutsObjectFormat {

    private Object value;

    private boolean compact;
    private NutsContentType outputFormat;
//    private NutsObjectFormat base;

    public DefaultNutsObjectFormat(NutsSession session) {
        super(session, "object-format");
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public DefaultNutsObjectFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NutsContentTypeFormat getBase() {
        checkSession();
        NutsSession session = getSession();
        NutsContentTypeFormat base = createObjectFormat();
        base.setSession(session);
        base.configure(true, session.boot().getBootOptions().getOutputFormatOptions().orElseGet(Collections::emptyList).toArray(new String[0]));
        base.configure(true, session.getOutputFormatOptions().toArray(new String[0]));
        return base;
    }

    public NutsContentTypeFormat createObjectFormat() {
        checkSession();
        NutsSession session = getSession();
        Object value = getValue();
        switch (getSession().getOutputFormat()) {
            //structured formats!
            case XML:
            case JSON:
            case TSON:
            case YAML: {
                NutsElements ee = NutsElements.of(session).setNtf(isNtf())
                        .setCompact(isCompact())
                        .setContentType(getSession().getOutputFormat());
                if (value instanceof NutsString) {
                    NutsTextBuilder builder = ((NutsString) value).builder();
                    Object[] r = builder.lines().map(
                            NutsFunction.of(
                                    x -> {
                                        if (true) {
                                            return x.filteredText();
                                        }
                                        return (Object) x.filteredText();
                                    },
                                    "filteredText"
                            )
                    ).toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case PROPS: {
                NutsPropertiesFormat ee = NutsPropertiesFormat.of(session).setNtf(isNtf());
                if (value instanceof NutsString) {
                    NutsTextBuilder builder = ((NutsString) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case TREE: {
                NutsTreeFormat ee = NutsTreeFormat.of(session).setNtf(isNtf());
                if (value instanceof NutsString) {
                    NutsTextBuilder builder = ((NutsString) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case TABLE: {
                NutsTableFormat ee = NutsTableFormat.of(session).setNtf(isNtf());
                if (value instanceof NutsString) {
                    NutsTextBuilder builder = ((NutsString) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case PLAIN: {
                NutsFormatPlain ee = new NutsFormatPlain(session).setCompact(isCompact()).setNtf(isNtf());
                ee.setValue(value);
                return ee;
            }
        }
        throw new NutsUnsupportedEnumException(getSession(), getSession().getOutputFormat());
    }

//    @Override
//    public NutsSession getSession() {
//        return base != null ? base.getSession() : super.getSession();
//    }

    @Override
    public NutsString format() {
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
    public void print(NutsPrintStream out) {
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
    public void print(NutsSessionTerminal terminal) {
        getBase().print(terminal);
    }

    @Override
    public void println(Writer w) {
        getBase().println(w);
    }

    @Override
    public void println(NutsPrintStream out) {
        getBase().println(out);
    }

    @Override
    public void println(Path path) {
        getBase().println(path);
    }

    @Override
    public void println(NutsSessionTerminal terminal) {
        getBase().println(terminal);
    }

    @Override
    public void println(File file) {
        getBase().println(file);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return getBase().configureFirst(commandLine);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
