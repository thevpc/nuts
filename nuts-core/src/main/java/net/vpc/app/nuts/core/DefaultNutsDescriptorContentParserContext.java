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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsDescriptorContentParserContext;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.InputStreamSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsQueryOptions;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNutsDescriptorContentParserContext implements NutsDescriptorContentParserContext {

    private final NutsWorkspace workspace;
    private final NutsSession session;
    private final InputStreamSource file;
    private final String fileExtension;
    private final String fileType;
    private final String mimeType;
    private byte[] bytes;
    private final NutsQueryOptions options;

    public DefaultNutsDescriptorContentParserContext(NutsWorkspace workspace, NutsSession session, InputStreamSource file, String fileExtension, String fileType, String mimeType,NutsQueryOptions options) {
        this.file = file;
        this.workspace = workspace;
        this.session = session;
        this.fileExtension = fileExtension;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.options = options;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public InputStream getHeadStream() {
        if (bytes == null) {
            try {
                bytes = IOUtils.loadByteArray(file.open(), 1024 * 1024 * 10, true);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public InputStream getFullStream() {
        try {
            return file.open();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public NutsQueryOptions getQueryOptions() {
        return options;
    }
    
}
