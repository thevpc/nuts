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
 * Copyright (C) 2016-2017 Taha BEN SALAH
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
package net.vpc.app.nuts;

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
 */
public interface NutsVersionFilter extends NutsSearchIdFilter {

    /**
     * true if the version is accepted by this instance filter
     * @param version version to check
     * @param session current session instance
     * @return true if the version is accepted by this instance interval
     */
    boolean accept(NutsVersion version, NutsSession session);

    /**
     * true if the version is accepted by this instance filter
     * @param sid search id
     * @param session current session instance
     * @return 
     */
    @Override
    default boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        return accept(sid.getVersion(session), session);
    }
}
