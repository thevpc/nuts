/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nmysql.util;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.NutsCommandLine;

/**
 *
 * @author thevpc
 */
public class AtName {

    private String config;
    private String name;

    public static AtName nextConfigOption(NutsCommandLine cmd) {
        NutsArgument a = cmd.nextString();
        AtName name2 = new AtName(a.getStringValue());
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
        return StringUtils.coalesce(name, "default") + "@" + StringUtils.coalesce(config, "default");
    }

    public static AtName nextAppOption(NutsCommandLine cmd) {
        NutsArgument a = cmd.nextString();
        return new AtName(a.getStringValue());
    }

    public static AtName nextAppNonOption(NutsCommandLine cmd) {
        NutsArgument a = cmd.nextString();
        return new AtName(a.getString());
    }

    public static AtName nextConfigNonOption(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
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
