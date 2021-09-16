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

import java.io.File;
import java.net.URL;
import java.util.function.Function;

/**
 * @app.category Input Output
 */
public interface NutsPathBuilder extends NutsFormattable {
    static NutsPathBuilder of(URL path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path).builder();
    }

    static NutsPathBuilder of(String path, ClassLoader classLoader, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path, classLoader).builder();
    }

    static NutsPathBuilder of(File path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path).builder();
    }

    static NutsPathBuilder of(String path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path).builder();
    }

    Function<String, String> getVarResolver();

    NutsPathBuilder setVarResolver(Function<String, String> r);

    NutsPathBuilder withWorkspaceBaseDir();

    NutsPathBuilder withAppBaseDir();

    String getBaseDir();

    NutsPathBuilder setBaseDir(String baseDir);

    /**
     * expand path to {@code baseFolder}. Expansion mechanism supports '~'
     * prefix (linux like) and will expand path to {@code baseFolder} if it was
     * resolved as a relative path.
     *
     * @return expanded path
     */
    NutsPath build();

    boolean isExpandVars();

    NutsPathBuilder setExpandVars(boolean expandVars);
}
