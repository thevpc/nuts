/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

/**
 * version interval is a version filter that accepts interval ranges of
 * versions.
 *
 * version intervals can be in one of the following forms
 * <pre>
 * [ version, ]
 * ] version, ] or ( version, ]
 * [ version, [ or [ version, )
 * ] version, [ or ] version, [
 *
 * [ ,version ]
 * ] ,version ] or ( ,version ]
 * [ ,version [ or [ ,version )
 * ] ,version [ or ] ,version [
 *
 * [ version1 , version2 ]
 * ] version1 , version2 ] or ( version1 , version2 ]
 * [ version1 , version2 [ or [ version1 , version2 )
 * ] version1 , version2 [ or ] version1 , version2 [
 *
 * comma or space separated intervals such as :
 *   [ version1 , version2 ], [ version1 , version2 ]
 *   [ version1 , version2 ]  [ version1 , version2 ]
 * </pre>
 *
 * Created by vpc on 1/8/17.
 *
 * @since 0.5.4
 * @category Descriptor
 */
public interface NutsVersionFilter extends NutsArtifactFilter {

    /**
     * true if the version is accepted by this instance filter
     *
     * @param version version to check
     * @param session current session instance
     * @return true if the version is accepted by this instance interval
     */
    boolean acceptVersion(NutsVersion version, NutsSession session);

    /**
     * true if the version is accepted by this instance filter
     *
     * @param sid search id
     * @param session current session instance
     * @return true if accepted
     */
    @Override
    default boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        return acceptVersion(sid.getId(session).getVersion(), session);
    }

    NutsVersionFilter or(NutsVersionFilter other);

    NutsVersionFilter and(NutsVersionFilter other);

    NutsVersionFilter neg();
}
