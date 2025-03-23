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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NArtifactCall;
import net.thevpc.nuts.NArtifactCallBuilder;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNArtifactCallBuilder implements NArtifactCallBuilder, Serializable {

    private static final long serialVersionUID = 1L;

    private NId id;
    private List<String> arguments = new ArrayList<>();
    private String scriptName;
    private String scriptContent;

    public DefaultNArtifactCallBuilder() {
    }

    public DefaultNArtifactCallBuilder(NArtifactCall value) {
        setId(value.getId());
        setArguments(value.getArguments());
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    @Override
    public NArtifactCallBuilder setScriptName(String scriptName) {
        this.scriptName = scriptName;
        return this;
    }

    @Override
    public String getScriptContent() {
        return scriptContent;
    }

    @Override
    public NArtifactCallBuilder setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
        return this;
    }

    public NId getId() {
        return id;
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public DefaultNArtifactCallBuilder setArguments(String... arguments) {
        this.arguments = new ArrayList<>();
        if (arguments != null) {
            for (String argument : arguments) {
                if (argument != null) {
                    this.arguments.add(argument);
                }
            }
        }
        return this;
    }

    @Override
    public NArtifactCallBuilder setArguments(List<String> arguments) {
        this.arguments = new ArrayList<>();
        if (arguments != null) {
            for (String argument : arguments) {
                if (argument != null) {
                    this.arguments.add(argument);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNArtifactCallBuilder setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NArtifactCallBuilder copyFrom(NArtifactCallBuilder value) {
        if (value != null) {
            setId(value.getId());
            setArguments(value.getArguments());
            setScriptName(value.getScriptName());
            setScriptContent(value.getScriptContent());
        }
        return this;
    }

    @Override
    public NArtifactCallBuilder copyFrom(NArtifactCall value) {
        if (value != null) {
            setId(value.getId());
            setArguments(value.getArguments());
            setScriptName(value.getScriptName());
            setScriptContent(value.getScriptContent());
        }
        return this;
    }

    @Override
    public NArtifactCallBuilder clear() {
        setId(null);
        this.arguments = new ArrayList<>();
        this.scriptName = null;
        this.scriptContent = null;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNArtifactCallBuilder that = (DefaultNArtifactCallBuilder) o;
        return Objects.equals(id, that.id) && Objects.equals(arguments, that.arguments) && Objects.equals(scriptName, that.scriptName) && Objects.equals(scriptContent, that.scriptContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, arguments, scriptName, scriptContent);
    }

    @Override
    public NArtifactCall build() {
        return new DefaultNArtifactCall(id, arguments, scriptName, scriptContent);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
