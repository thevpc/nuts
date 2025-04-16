/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.reserved.util.NVersionFilterWithDescription;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * version interval is a version filter that accepts interval ranges of
 * versions.
 * <p>
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
 * <p>
 * Created by vpc on 1/8/17.
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NVersionFilter extends NFilter {

    /**
     * true if the version is accepted by this instance filter
     *
     * @param version version to check
     * @return true if the version is accepted by this instance interval
     */
    boolean acceptVersion(NVersion version);

    NVersionFilter or(NVersionFilter other);

    NVersionFilter and(NVersionFilter other);

    NVersionFilter neg();

    NOptional<List<NVersionInterval>> intervals();

    @Override
    default NElement describe() {
        return NFilter.super.describe();
    }

    NFilter withDesc(NEDesc description);
}
