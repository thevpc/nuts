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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NArtifactCall;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public class DefaultNArtifactCall implements NArtifactCall, Serializable {

    private static final long serialVersionUID = 1L;

    private NId id;
    private List<String> arguments;
    private String scriptName;
    private String scriptContent;

    /**
     * constructor used by serializers and deserializers
     */
    protected DefaultNArtifactCall() {
        //for serialization purposes!
    }

    public DefaultNArtifactCall(NArtifactCall other) {
        this.id = other.getId();
        this.arguments = NReservedLangUtils.nonNullList(other.getArguments());
        this.scriptName = NStringUtils.trimToNull(other.getScriptName());
        this.scriptContent = NStringUtils.trimToNull(other.getScriptContent());
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    @Override
    public String getScriptContent() {
        return scriptContent;
    }

    @Override
    public boolean isBlank() {
        if (!NBlankable.isBlank(id)) {
            return false;
        }
        if (!NBlankable.isBlank(scriptName)) {
            return false;
        }
        if (!NBlankable.isBlank(scriptContent)) {
            return false;
        }
        if (arguments != null) {
            for (String d : arguments) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        return true;
    }

    public DefaultNArtifactCall(NId id) {
        this(id, null, null, null);
    }

    public DefaultNArtifactCall(NId id, List<String> args, String scriptName, String scriptContent) {
        this.id = id;
        this.arguments = NReservedLangUtils.nonNullList(args);
        this.scriptName = NStringUtils.trimToNull(scriptName);
        this.scriptContent = scriptContent;
    }

    public NId getId() {
        return id;
    }

    public List<String> getArguments() {
        return NReservedLangUtils.unmodifiableList(arguments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultNArtifactCall that = (DefaultNArtifactCall) o;
        return Objects.equals(id, that.id)
                && Objects.equals(arguments, that.arguments)
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, arguments);
        return result;
    }

    @Override
    public String toString() {
        return "NArtifactCall{" +
                "id=" + id +
                ", arguments=" + arguments +
                '}';
    }
}
