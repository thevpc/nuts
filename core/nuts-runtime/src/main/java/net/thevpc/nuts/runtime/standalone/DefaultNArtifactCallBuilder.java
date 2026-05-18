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

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NArtifactCallBuilder;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
@NComponentScope(NScopeType.PROTOTYPE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNArtifactCallBuilder implements NArtifactCallBuilder, Serializable {

    private static final long serialVersionUID = 1L;

    private NId id;
    private List<String> arguments = new ArrayList<>();
    private String scriptName;
    private String scriptContent;

    public DefaultNArtifactCallBuilder() {
    }

    public DefaultNArtifactCallBuilder(NArtifactCall value) {
        id(value.id());
        arguments(value.arguments());
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    @Override
    public NArtifactCallBuilder scriptName(String scriptName) {
        this.scriptName = scriptName;
        return this;
    }

    @Override
    public String scriptContent() {
        return scriptContent;
    }

    @Override
    public NArtifactCallBuilder scriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
        return this;
    }

    public NId id() {
        return id;
    }

    public List<String> arguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public DefaultNArtifactCallBuilder arguments(String... arguments) {
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
    public NArtifactCallBuilder arguments(List<String> arguments) {
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
    public DefaultNArtifactCallBuilder id(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NArtifactCallBuilder copyFrom(NArtifactCallBuilder value) {
        if (value != null) {
            id(value.id());
            arguments(value.arguments());
            scriptName(value.getScriptName());
            scriptContent(value.scriptContent());
        }
        return this;
    }

    @Override
    public NArtifactCallBuilder copyFrom(NArtifactCall value) {
        if (value != null) {
            id(value.id());
            arguments(value.arguments());
            scriptName(value.scriptName());
            scriptContent(value.scriptContent());
        }
        return this;
    }

    @Override
    public NArtifactCallBuilder clear() {
        id(null);
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

}
