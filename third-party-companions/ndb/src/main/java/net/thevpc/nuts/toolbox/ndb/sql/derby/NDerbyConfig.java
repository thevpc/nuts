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
package net.thevpc.nuts.toolbox.ndb.sql.derby;

import net.thevpc.nuts.toolbox.ndb.NdbConfig;

/**
 *
 * @author thevpc
 */
public class NDerbyConfig extends NdbConfig implements Cloneable{

    private Command cmd = Command.start;
    private String derbyVersion = null;
    private String derbyDataHomeRoot = null;
    private String derbyDataHomeReplace = null;
    private SSLMode sslmode = null;
    private String extraArg = null;

    public NDerbyConfig copy(){
        return (NDerbyConfig) clone();
    }

    public Command getCmd() {
        return cmd;
    }

    public NDerbyConfig setCmd(Command cmd) {
        this.cmd = cmd;
        return this;
    }

    public String getDerbyVersion() {
        return derbyVersion;
    }

    public NDerbyConfig setDerbyVersion(String derbyVersion) {
        this.derbyVersion = derbyVersion;
        return this;
    }

    public String getDerbyDataHomeRoot() {
        return derbyDataHomeRoot;
    }

    public NDerbyConfig setDerbyDataHomeRoot(String derbyDataHomeRoot) {
        this.derbyDataHomeRoot = derbyDataHomeRoot;
        return this;
    }

    public String getDerbyDataHomeReplace() {
        return derbyDataHomeReplace;
    }

    public NDerbyConfig setDerbyDataHomeReplace(String derbyDataHomeReplace) {
        this.derbyDataHomeReplace = derbyDataHomeReplace;
        return this;
    }

    public SSLMode getSslmode() {
        return sslmode;
    }

    public NDerbyConfig setSslmode(SSLMode sslmode) {
        this.sslmode = sslmode;
        return this;
    }

    public String getExtraArg() {
        return extraArg;
    }

    public NDerbyConfig setExtraArg(String extraArg) {
        this.extraArg = extraArg;
        return this;
    }
}
