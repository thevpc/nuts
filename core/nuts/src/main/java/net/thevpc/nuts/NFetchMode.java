/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

/**
 * fetch mode defines if the artifact should be looked for withing the "installed" meta repository, "local" (offline)
 * machine repositories or over the wire (remote repositories).
 *
 * <br>
 * "installed" artifacts are stored in a pseudo-repository called "installed" which include all installed
 * (using command install) artifacts. Effective storage may (should?) remain in a local repository though.
 * Actually pseudo-repository "installed" manages references to these storages.
 * <br>
 * <br>
 * local repositories include all local folder based repositories. Semantically they should define machine/node based
 * storage that is independent from LAN/WAN/Cloud networks. A local database based repository may be considered as local
 * though not recommended as the server may be down.
 * Il all ways, local repositories are considered fast according to fetch/deploy commands.
 * <br>
 * <br>
 * remote repositories include all non local repositories which may present slow access and connectivity issues.
 * Typically this include server based repositories (http, ...).
 * <br>
 * <br>
 * It is important to say that a repository may serve both local and remote artifacts as usually remote repositories
 * enable cache support; in which case, if the artifact si cached, it will be accessed locally.
 * <br>
 *
 * @app.category Commands
 * @since 0.5.4
 */
public enum NFetchMode implements NEnum {
    /**
     * artifacts fetched (locally)
     */
    LOCAL,

    /**
     * artifacts not fetched (remote)
     */
    REMOTE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NFetchMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NFetchMode> parse(String value) {
        return NStringUtils.parseEnum(value, NFetchMode.class);
    }


    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
