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
package net.vpc.app.nuts.extensions.servers;

import net.vpc.app.nuts.extensions.util.CoreIOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by vpc on 1/7/17.
 */
public abstract class AbstractNutsHttpServletFacadeContext implements NutsHttpServletFacadeContext {

    public void sendResponseText(int code, String text) throws IOException {
        byte[] bytes = text.getBytes();
        sendResponseHeaders(code, bytes.length);
        getResponseBody().write(bytes);
    }

    public void sendResponseFile(int code, File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) {
            sendResponseHeaders(code, file.length());
            CoreIOUtils.copy(new FileInputStream(file), getResponseBody(), true, false);
        } else {
            sendError(404, "File not found");
        }
    }
}
