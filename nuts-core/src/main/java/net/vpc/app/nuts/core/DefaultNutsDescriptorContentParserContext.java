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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import net.vpc.app.nuts.NutsFetchCommand;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.InputSource;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNutsDescriptorContentParserContext implements NutsDescriptorContentParserContext {

    private final NutsWorkspace workspace;
    private final NutsSession session;
    private final InputSource file;
    private final String fileExtension;
    private final String fileType;
    private final String mimeType;
    private byte[] bytes;
    private final NutsFetchCommand options;

    public DefaultNutsDescriptorContentParserContext(NutsWorkspace workspace, NutsSession session, InputSource file, String fileExtension, String fileType, String mimeType, NutsFetchCommand options) {
        this.file = file.multi();
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
                try(InputStream is=file.open()){
                    bytes = CoreIOUtils.loadByteArray(is, 1024 * 1024 * 10, true);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new ByteArrayInputStream(bytes);
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
    public NutsFetchCommand getQueryOptions() {
        return options;
    }

}
