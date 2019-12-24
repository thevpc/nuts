/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.NutsDescriptorContentParserContext;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import net.vpc.app.nuts.runtime.io.NamedByteArrayInputStream;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.io.InputSource;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNutsDescriptorContentParserContext implements NutsDescriptorContentParserContext {

    private final NutsSession session;
    private final InputSource file;
    private final String fileExtension;
    private final String mimeType;
    private byte[] bytes;
    private final String[] parseOptions;

    public DefaultNutsDescriptorContentParserContext(NutsSession session, InputSource file, String fileExtension, String mimeType, String[] parseOptions) {
        this.file = file.multi();
        this.session = session;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.parseOptions = parseOptions;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public String[] getParseOptions() {
        return parseOptions;
    }

    @Override
    public InputStream getHeadStream() {
        if (bytes == null) {
            try {
                try (InputStream is = file.open()) {
                    bytes = CoreIOUtils.loadByteArray(is, 1024 * 1024 * 10, true);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new NamedByteArrayInputStream(bytes, file.getName());
    }

    @Override
    public InputStream getFullStream() {
        return file.open();
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getName() {
        return file.getName();
    }

}
