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
package net.vpc.app.nuts.extensions.workspaces;

import net.vpc.app.nuts.NutsDescriptorContentParserContext;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;

import java.io.*;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNutsDescriptorContentParserContext implements NutsDescriptorContentParserContext {

    private NutsWorkspace workspace;
    private NutsSession session;
    private File file;
    private String fileExtension;
    private String fileType;
    private String mimeType;
    private byte[] bytes;

    public DefaultNutsDescriptorContentParserContext(NutsWorkspace workspace,NutsSession session,File file, String fileExtension, String fileType, String mimeType) {
        this.file = file;
        this.workspace = workspace;
        this.session = session;
        this.fileExtension = fileExtension;
        this.fileType = fileType;
        this.mimeType = mimeType;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public InputStream getHeadStream() throws IOException {
        if (bytes == null) {
            bytes = CoreIOUtils.readStreamAsBytes(file, 1024 * 1024 * 10);
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public InputStream getFullStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getFileType() {
        return fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public File getFile() {
        return file;
    }
}
