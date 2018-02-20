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

import java.io.*;
import java.util.Locale;
import net.vpc.app.nuts.NutsIOException;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.extensions.util.NullOutputStream;

/**
 * Created by vpc on 2/20/17.
 */
public class AbstractNutsPrintStream extends NutsPrintStream {

    private boolean formatEnabled = true;

    public AbstractNutsPrintStream() {
        super(NullOutputStream.INSTANCE);
    }

    public AbstractNutsPrintStream(OutputStream out) {
        super(out);
    }

    public AbstractNutsPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public AbstractNutsPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public AbstractNutsPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public AbstractNutsPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public AbstractNutsPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public AbstractNutsPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    protected void startFormat(NutsTextFormat format) {
    }

    protected void endFormat(NutsTextFormat format) {
    }

    @Override
    public AbstractNutsPrintStream printf(Locale l, String format, Object... args) {
        AbstractNutsPrintStream.super.printf(l, format, args);
        return this;
    }

    @Override
    public AbstractNutsPrintStream format(Locale l, String format, Object... args) {
        AbstractNutsPrintStream.super.format(l, format, args);
        return this;
    }

    @Override
    public AbstractNutsPrintStream append(CharSequence csq) {
        AbstractNutsPrintStream.super.append(csq);
        return this;
    }

    @Override
    public AbstractNutsPrintStream append(CharSequence csq, int start, int end) {
        AbstractNutsPrintStream.super.append(csq, start, end);
        return this;
    }

    @Override
    public AbstractNutsPrintStream append(char c) {
        AbstractNutsPrintStream.super.append(c);
        return this;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public void setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
    }

    protected final void writeRaw(String rawString) {
        try {
            super.write(rawString.getBytes());
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

}
