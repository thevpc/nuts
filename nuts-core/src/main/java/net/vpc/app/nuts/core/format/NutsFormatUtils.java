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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.format;

import java.util.Map;
import static net.vpc.app.nuts.core.util.common.CoreCommonUtils.stringValue;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsNamedElement;

/**
 *
 * @author vpc
 */
public class NutsFormatUtils {

    public static void putAllInProps(String prefix, Map<String, String> dest, NutsElement value) {
        switch (value.type()) {
            case BOOLEAN:
            case DATE:
            case NUMBER:
            case STRING:
            case NULL:
            case UNKNWON: {
                dest.put(prefix, stringValue(value.primitive().getValue()));
                break;
            }
            case OBJECT: {
                if (!CoreStringUtils.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                for (NutsNamedElement e : value.object().children()) {
                    putAllInProps(prefix + e.getName(), dest, e.getValue());
                }
                break;
            }
            case ARRAY: {
                if (!CoreStringUtils.isBlank(prefix)) {
                    prefix += ".";
                } else {
                    prefix = "";
                }
                int i = 0;
                for (NutsElement e : value.array().children()) {
                    putAllInProps(prefix + (i + 1), dest, e);
                    i++;
                }
                break;
            }
        }
    }
}
