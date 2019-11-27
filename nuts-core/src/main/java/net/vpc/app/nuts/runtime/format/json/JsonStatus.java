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
package net.vpc.app.nuts.runtime.format.json;

import net.vpc.app.nuts.NutsParseException;
import net.vpc.app.nuts.NutsWorkspace;

public class JsonStatus {

    public int countBraces;
    public int openBraces;
    public int openBrackets;
    public boolean openAntiSlash;
    public boolean openSimpleQuotes;
    public boolean openDoubleQuotes;
    public NutsWorkspace ws;

    public JsonStatus(NutsWorkspace ws) {
        this.ws = ws;
    }

    public boolean checkValid(boolean throwError) {
        if (!checkPartialValid(throwError)) {
            return false;
        }
        if (countBraces == 0) {
            if (throwError) {
                throw new NutsParseException(ws, "not an object");
            }
            return false;
        }
        if (openBrackets > 0) {
            if (throwError) {
                throw new NutsParseException(ws, "Unbalanced brackets");
            }
            return false;
        }
        if (openBraces > 0) {
            if (throwError) {
                throw new NutsParseException(ws, "Unbalanced braces");
            }
            return false;
        }
        if (openAntiSlash) {
            if (throwError) {
                throw new NutsParseException(ws, "Unbalanced anti-slash");
            }
        }
        if (openSimpleQuotes) {
            if (throwError) {
                throw new NutsParseException(ws, "Unbalanced simple quotes");
            }
            return false;
        }
        if (openDoubleQuotes) {
            if (throwError) {
                throw new NutsParseException(ws, "Unbalanced double quotes");
            }
            return false;
        }
        return true;
    }

    public boolean checkPartialValid(boolean throwError) {
        if (openBrackets < 0) {
            if (throwError) {
                throw new NutsParseException(ws, "Unbalanced brackets");
            }
            return false;
        }
        if (openBraces < 0) {
            if (throwError) {
                throw new NutsParseException(ws, "Unbalanced braces");
            }
            return false;
        }
        return true;
    }
}
