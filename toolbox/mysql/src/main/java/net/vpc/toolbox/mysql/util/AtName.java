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
package net.vpc.toolbox.mysql.util;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.common.strings.StringUtils;

/**
 *
 * @author vpc
 */
public class AtName {

    private String config;
    private String name;

    public static AtName nextConfigOption(NutsCommand cmd) {
        NutsArgument a = cmd.nextString();
        AtName name2 = new AtName(a.getValue().getString());
        if (!name2.getConfigName().isEmpty() && !name2.getDatabaseName().isEmpty()) {
            cmd.pushBack(a);
            cmd.unexpectedArgument("should be valid a config name");
        }
        if (name2.getConfigName().isEmpty()) {
            name2 = new AtName(name2.getDatabaseName(), "");
        }
        return name2;
    }

    @Override
    public String toString() {
        return StringUtils.coalesce(name,"default")+"@"+StringUtils.coalesce(config,"default");
    }
    

    public static AtName nextAppOption(NutsCommand cmd) {
        NutsArgument a = cmd.nextString();
        return new AtName(a.getValue().getString());
    }

    public static AtName nextAppNonOption(NutsCommand cmd) {
        NutsArgument a = cmd.nextString();
        return new AtName(a.getString());
    }

    public static AtName nextConfigNonOption(NutsCommand cmd) {
        NutsArgument a = cmd.next();
        AtName name2 = new AtName(a.getString());
        if (!name2.getConfigName().isEmpty() && !name2.getDatabaseName().isEmpty()) {
            cmd.unexpectedArgument("should be valid a config name");
        } else {
            cmd.skip();
        }
        if (name2.getConfigName().isEmpty()) {
            name2 = new AtName(name2.getDatabaseName(), "");
        }
        return name2;
    }

    public AtName(String name) {
        int i = name.indexOf('@');
        if (i >= 0) {
            this.config = name.substring(i + 1);
            this.name = name.substring(0, i);
        } else {
            this.config = "";
            this.name = name;
        }
    }

    public AtName(String config, String name) {
        this.config = config;
        this.name = name;
    }

    public String getConfigName() {
        return config;
    }

    public String getDatabaseName() {
        return name;
    }

}
