/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.terminals;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.vpc.app.nuts.extensions.terminals.textparsers.DefaultNutsTextParser;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 * Created by vpc on 2/20/17.
 */
public class DefaultNutsPrintStream extends AbstractNutsPrintStream {

    public DefaultNutsPrintStream() {
        super();
    }

    public DefaultNutsPrintStream(OutputStream out) {
        super(out);
    }

    public DefaultNutsPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public DefaultNutsPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public DefaultNutsPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public DefaultNutsPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public DefaultNutsPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public DefaultNutsPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    protected void startFormat(NutsTextFormat format) {

    }

    @Override
    protected void endFormat(NutsTextFormat format) {
    }

    protected NutsTextFormat simplifyFormat(NutsTextFormat f) {
        if (f instanceof NutsTextFormatList) {
            NutsTextFormatList l = (NutsTextFormatList) f;
            NutsTextFormat[] o = ((NutsTextFormatList) f).getChildren();
            List<NutsTextFormat> ok = new ArrayList<>();
            if (o != null) {
                for (NutsTextFormat v : o) {
                    if (v != null) {
                        v = simplifyFormat(v);
                        if (v != null) {
                            ok.add(v);
                        }
                    }
                }
            }
            if (ok.isEmpty()) {
                return null;
            }
            if (ok.size() == 1) {
                return simplifyFormat(ok.get(0));
            }
            return NutsTextFormats.list(ok.toArray(new NutsTextFormat[ok.size()]));
        }
        return f;
    }

    @Override
    public AbstractNutsPrintStream format(Locale l, String format, Object... args) {
        print(CoreStringUtils.format(l, format, args));
        return this;
    }

    public PrintStream format(String format, Object... args) {
        print(CoreStringUtils.format(Locale.getDefault(), format, args));
        return this;
    }

    @Override
    public void println(String text) {
        print(text);
        println();
    }

    protected DefaultNutsPrintStream writeRaw(NutsTextFormat format, String rawString) {
        if (isFormatEnabled() && format != null) {
            NutsTextFormat c = simplifyFormat(format);
            try {
                startFormat(c);
                writeRaw(rawString);
            } finally {
                endFormat(c);
            }
        } else {
            writeRaw(rawString);
        }
        return this;
    }

    private void print(NutsTextFormat[] formats, NutsTextNode node) {
        if (formats == null) {
            formats = new NutsTextFormat[0];
        }
        if (node instanceof NutsTextNodePlain) {
            NutsTextNodePlain p = (NutsTextNodePlain) node;
            writeRaw(NutsTextFormats.list(formats), p.getValue());
        } else if (node instanceof NutsTextNodeList) {
            NutsTextNodeList s = (NutsTextNodeList) node;
            for (NutsTextNode n : s) {
                print(formats, n);
            }
        } else if (node instanceof NutsTextNodeStyled) {
            NutsTextNodeStyled s = (NutsTextNodeStyled) node;
            NutsTextFormat[] s2 = _appendFormats(formats, s.getStyle());
            print(s2, s.getChild());
        } else if (node instanceof NutsTextNodeCommand) {
            NutsTextNodeCommand s = (NutsTextNodeCommand) node;
            NutsTextFormat[] s2 = _appendFormats(formats, s.getStyle());
            writeRaw(NutsTextFormats.list(s2), "");
        } else {
            writeRaw(NutsTextFormats.list(formats), String.valueOf(node));
        }
    }

    public void print(NutsTextNode node) {
        if (node == null) {
            node = NutsTextNodePlain.NULL;
        }
        print(new NutsTextFormat[0], node);
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT + 1;
    }

    @Override
    public void print(String text) {
        if (!isFormatEnabled()) {
            writeRaw(text);
            return;
        }
        if (text == null) {
            text = "";
        }
        if (text.isEmpty()) {
            //do nothing!!!
        } else {
            if (text.equals("`enable-formats`")) {
                setFormatEnabled(true);
            } else if (text.equals("`disable-formats`")) {
                setFormatEnabled(false);
            } else {
                NutsTextNode node = DefaultNutsTextParser.INSTANCE.parse(text);
                print(node);
            }
        }
    }

    private NutsTextFormat[] _appendFormats(NutsTextFormat[] old, NutsTextFormat v) {
        List<NutsTextFormat> list = new ArrayList<NutsTextFormat>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        list.add(v);
        return list.toArray(new NutsTextFormat[list.size()]);
    }

}
