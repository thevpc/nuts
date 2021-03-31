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
package net.thevpc.nuts.runtime.core.format.text.parser;


import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeCode;
import net.thevpc.nuts.NutsTextNodeType;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsSession;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextNodeCode extends NutsTextNodeSpecialBase implements NutsTextNodeCode {

    private final String text;

    public DefaultNutsTextNodeCode(NutsWorkspace ws, String start, String kind, String separator, String end, String text) {
        super(ws,start, kind,
                (kind != null && kind.length() > 0
                        &&
                        text != null && text.length() > 0
                        && (separator == null || separator.isEmpty())) ? " " : separator
                , end);
        this.text = text;
    }

    @Override
    public NutsTextNode parse(NutsSession session) {
        NutsCodeFormat t = ((DefaultNutsTextManager) getWorkspace().formats().text())
                .setSession(session)
                .resolveBlocTextFormatter(getKind());
        return t.textToNode(text);
    }

    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.CODE;
    }

    public String getText() {
        return text;
    }
}
