/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsTextFormatList;
import net.vpc.app.nuts.extensions.core.NutsTextNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.vpc.app.nuts.NutsTextFormat;
import net.vpc.app.nuts.NutsTextFormats;
import net.vpc.app.nuts.extensions.core.NutsTextList;
import net.vpc.app.nuts.extensions.core.NutsTextPlain;
import net.vpc.app.nuts.extensions.core.NutsTextStyled;
import net.vpc.app.nuts.extensions.textparsers.defaultparser.DefaultNutsTextParser;

/**
 * Created by vpc on 2/20/17.
 */
public class DefaultNutsPrintStream extends NutsPrintStream {

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

    protected void doWithFormat(NutsTextFormat format, Runnable runnable) {
        if (isFormatEnabled() && format != null) {
            NutsTextFormat c = simplifyFormat(format);
            try {
                startFormat(c);
                runnable.run();
            } finally {
                endFormat(c);
            }
        } else {
            runnable.run();
        }
    }

    @Override
    public DefaultNutsPrintStream write(NutsTextFormat c, final int b) {
        doWithFormat(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.write(b);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream write(NutsTextFormat c, final byte[] buf, final int off, final int len) {
        doWithFormat(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.write(buf, off, len);
            }
        });
        return this;
    }

    @Override
    public DefaultNutsPrintStream print(NutsTextFormat c, final boolean b) {
        doWithFormat(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.print(b);
            }
        });
        return this;
    }

    @Override
    public DefaultNutsPrintStream print(NutsTextFormat format, final char c) {
        doWithFormat(format, new Runnable() {

            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(c);
            }
        });
        return this;
    }

    @Override
    public DefaultNutsPrintStream print(NutsTextFormat c, final int i) {
        doWithFormat(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.print(i);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(NutsTextFormat c, final long l) {
        doWithFormat(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.print(l);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(NutsTextFormat c, final float f) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(f);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(NutsTextFormat c, final double d) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(d);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(NutsTextFormat c, final char[] s) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(s);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(NutsTextFormat c, final String s) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(s);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(NutsTextFormat c, final Object obj) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(obj);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final boolean x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final char x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final int x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final long x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public void println(NutsTextFormat c, final float x) {
        doWithFormat(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final double x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final char[] x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final String x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(NutsTextFormat c, final Object x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public PrintStream printf(NutsTextFormat c, final String format, final Object... args) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.printf(format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream printf(NutsTextFormat c, final Locale l, final String format, final Object... args) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.printf(l, format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream format(NutsTextFormat c, final String format, final Object... args) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.format(format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream format(NutsTextFormat c, final Locale l, final String format, final Object... args) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.format(l, format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream append(NutsTextFormat c, final CharSequence csq) {
        doWithFormat(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.append(csq);
            }
        });
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(NutsTextFormat c, final CharSequence csq, final int start, final int end) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.append(csq, start, end);
            }
        });
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(NutsTextFormat format, final char c) {
        doWithFormat(format, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.append(c);
            }
        });
        return this;
    }

    @Override
    public DefaultNutsPrintStream printf(String format, Object... args) {
        DefaultNutsPrintStream.super.printf(format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream printf(Locale l, String format, Object... args) {
        DefaultNutsPrintStream.super.printf(l, format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream format(String format, Object... args) {
        DefaultNutsPrintStream.super.format(format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream format(Locale l, String format, Object... args) {
        DefaultNutsPrintStream.super.format(l, format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(CharSequence csq) {
        DefaultNutsPrintStream.super.append(csq);
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(CharSequence csq, int start, int end) {
        DefaultNutsPrintStream.super.append(csq, start, end);
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(char c) {
        DefaultNutsPrintStream.super.append(c);
        return this;
    }

    @Override
    public void drawln(String text) {
        draw(text);
        println();
    }

    private NutsTextFormat[] append(NutsTextFormat[] old, NutsTextFormat v) {
        List<NutsTextFormat> list = new ArrayList<NutsTextFormat>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        list.add(v);
        return list.toArray(new NutsTextFormat[list.size()]);
    }

    private void draw(NutsTextFormat[] formats, NutsTextNode chunk) {
        if (chunk instanceof NutsTextPlain) {
            if (formats == null) {
                formats = new NutsTextFormat[0];
            }
            NutsTextPlain p = (NutsTextPlain) chunk;
            print(NutsTextFormats.list(formats), p.getValue());
        } else if (chunk instanceof NutsTextList) {
            if (formats == null) {
                formats = new NutsTextFormat[0];
            }
            NutsTextList s = (NutsTextList) chunk;
            for (NutsTextNode n : s) {
                draw(formats, n);
            }
        } else if (chunk instanceof NutsTextStyled) {
            NutsTextStyled s = (NutsTextStyled) chunk;
            NutsTextFormat[] s2 = append(formats, s.getStyle());
            draw(s2, s.getChild());
        } else {
            if (formats == null) {
                formats = new NutsTextFormat[0];
            }
            print(NutsTextFormats.list(formats), String.valueOf(chunk));
        }
    }

    public void draw(NutsTextNode node) {
        if (node == null) {
            node = NutsTextPlain.NULL;
        }
        draw(null, node);
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT + 1;
    }

    @Override
    public void draw(String text) {
        if (text == null) {
            text = "";
        }
        if (text.isEmpty()) {
            //do nothin!!!
        } else {
            NutsTextNode node = DefaultNutsTextParser.INSTANCE.parse(text);
            draw(node);
        }
    }
}
