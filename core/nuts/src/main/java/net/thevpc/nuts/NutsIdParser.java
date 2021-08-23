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
 *
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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * @app.category Base
 */
public interface NutsIdParser {

    public static NutsIdParser of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().id().parser();
    }

    NutsIdParser setLenient(boolean lenient);

    boolean isLenient();

    /**
     * parse id or null if not valid.
     * id is parsed in the form
     * group:name#version?key=&lt;value&gt;{@code &}key=&lt;value&gt; ...
     * @param id to parse
     * @return parsed id
     * @throws NutsParseException if the string cannot be evaluated
     */
    NutsId parse(String id);


}
