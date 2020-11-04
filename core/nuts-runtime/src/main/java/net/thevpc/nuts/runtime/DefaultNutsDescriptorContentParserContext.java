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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime;

import net.thevpc.nuts.NutsDescriptorContentParserContext;
import net.thevpc.nuts.NutsInput;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.io.NamedByteArrayInputStream;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNutsDescriptorContentParserContext implements NutsDescriptorContentParserContext {

    private final NutsSession session;
    private final NutsInput file;
    private final String fileExtension;
    private final String mimeType;
    private byte[] bytes;
    private final String[] parseOptions;

    public DefaultNutsDescriptorContentParserContext(NutsSession session, NutsInput file, String fileExtension, String mimeType, String[] parseOptions) {
        this.file = session.getWorkspace().io().input().setMultiRead(true).of(file);
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
