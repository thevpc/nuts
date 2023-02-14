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
package net.thevpc.nuts.toolbox.ndb.sql.nmysql.util;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;

/**
 *
 * @author thevpc
 */
public class AtName {

    private String config;
    private String name;

    public static AtName nextConfigOption(NCmdLine cmd, NSession session) {
        NArg a = cmd.nextEntry().get(session);
        AtName name2 = new AtName(a.getStringValue().get(session));
        if (!name2.getConfigName().isEmpty() && !name2.getDatabaseName().isEmpty()) {
            cmd.pushBack(a);
            cmd.throwUnexpectedArgument(NMsg.ofPlain("should be valid a config name"));
        }
        if (name2.getConfigName().isEmpty()) {
            name2 = new AtName(name2.getDatabaseName(), "");
        }
        return name2;
    }

    @Override
    public String toString() {
        return NdbUtils.coalesce(name, "default") + "@" + NdbUtils.coalesce(config, "default");
    }

    public static AtName nextAppOption(NCmdLine cmd, NSession session) {
        NArg a = cmd.nextEntry().get(session);
        return a==null?null:new AtName(a.getStringValue().get(session));
    }

    public static AtName nextAppNonOption(NCmdLine cmd, NSession session) {
        NArg a = cmd.nextEntry().get(session);
        return a==null?null:new AtName(a.asString().get(session));
    }

    public static AtName nextConfigNonOption(NCmdLine cmd, NSession session) {
        NArg a = cmd.peek().get(session);
        AtName name2 = new AtName(a.asString().get(session));
        if (!name2.getConfigName().isEmpty() && !name2.getDatabaseName().isEmpty()) {
            cmd.throwUnexpectedArgument(NMsg.ofPlain("should be valid a config name"));
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
