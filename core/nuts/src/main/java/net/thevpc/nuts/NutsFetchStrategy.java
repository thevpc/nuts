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
 *
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

import net.thevpc.nuts.util.NutsEnum;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Fetch strategy defines modes (see {@link NutsFetchMode}) to use when searching for an artifact.
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public enum NutsFetchStrategy implements Iterable<NutsFetchMode>, NutsEnum {
    /**
     * enables search within local only artifacts (where installed or not).
     * Local artifacts include local folder based repositories and cached (fetched) repositories (whether or
     * not they are physically remote)
     */
    OFFLINE(true, NutsFetchMode.LOCAL),

    /**
     * enables search within local repositories, if not found, search in remote repositories.
     * If an artifact is found in local repositories, no further seek is allowed in remote repositories.
     */
    ONLINE(true, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE),

    /**
     * enables search within local repositories and in remote repositories (whether or not an artifact
     * is found in local repositories).
     */
    ANYWHERE(false, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE),

    /**
     * search in the remote
     */
    REMOTE(true, NutsFetchMode.REMOTE);


    /**
     * when true, stop when the first result was found
     */
    private final boolean stopFast;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;
    /**
     * modes array
     */
    private final NutsFetchMode[] all;

    /**
     * private default constructor
     *
     * @param stopFast stopFast flag
     * @param all      modes
     */
    NutsFetchStrategy(boolean stopFast, NutsFetchMode... all) {
        this.id = NutsNameFormat.ID_NAME.format(name());
        this.stopFast = stopFast;
        this.all = Arrays.copyOf(all, all.length);
    }

    public static NutsOptional<NutsFetchStrategy> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsFetchStrategy.class);
    }


    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

    /**
     * if true, do not consider next Fetch mode if the latter gives at least one result.
     *
     * @return true if do not consider next Fetch mode if the latter gives at least one result.
     */
    public boolean isStopFast() {
        return stopFast;
    }

    /**
     * ordered fetch modes
     *
     * @return ordered fetch modes
     */
    public Set<NutsFetchMode> modes() {
        return new HashSet<>(Arrays.asList(all));
    }

    /**
     * ordered fetch modes iterator
     *
     * @return ordered fetch modes iterator
     */
    @Override
    public Iterator<NutsFetchMode> iterator() {
        return Arrays.asList(all).iterator();
    }
}
