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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 * version interval is a version filter that accepts interval ranges of versions.
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
 * @since 0.5.4
 * @category Descriptor
 */
public interface NutsVersionFilter extends NutsArtifactFilter {

    /**
     * true if the version is accepted by this instance filter
     * @param version version to check
     * @param session current session instance
     * @return true if the version is accepted by this instance interval
     */
    boolean acceptVersion(NutsVersion version, NutsSession session);

    /**
     * true if the version is accepted by this instance filter
     * @param sid search id
     * @param session current session instance
     * @return true if accepted
     */
    @Override
    default boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        return acceptVersion(sid.getId(session).getVersion(), session);
    }

    default NutsVersionFilter or(NutsVersionFilter other) {
        return or((NutsFilter)other).to(NutsVersionFilter.class);
    }

    default NutsVersionFilter and(NutsVersionFilter other) {
        return and((NutsFilter)other).to(NutsVersionFilter.class);
    }

    default NutsVersionFilter neg() {
        return NutsArtifactFilter.super.neg().to(NutsVersionFilter.class);
    }
}
