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
 *
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
package net.thevpc.nuts;

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

    public DefaultNArtifactCallBuilder() {
    }

    public DefaultNArtifactCallBuilder(NArtifactCall value) {
        setId(value.getId());
        setArguments(value.getArguments());
    }

    public NId getId() {
        return id;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public DefaultNArtifactCallBuilder setArguments(String... arguments) {
        this.arguments = NReservedLangUtils.unmodifiableList(Arrays.asList(arguments));
        return this;
    }

    @Override
    public NArtifactCallBuilder setArguments(List<String> value) {
        this.arguments = NReservedLangUtils.unmodifiableList(value);
        return this;
    }

    @Override
    public DefaultNArtifactCallBuilder setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NArtifactCallBuilder set(NArtifactCallBuilder value) {
        return null;
    }

    @Override
    public NArtifactCallBuilder set(NArtifactCall value) {
        if(value!=null){
            setId(value.getId());
            setArguments(value.getArguments());
        }else{
            clear();
        }
        return this;
    }

    @Override
    public NArtifactCallBuilder clear() {
        setId(null);
        setArguments();
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNArtifactCallBuilder that = (DefaultNArtifactCallBuilder) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(arguments, that.arguments)
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id);
        result = 31 * result + Objects.hashCode(arguments);
        return result;
    }

    @Override
    public NArtifactCall build() {
        return new DefaultNArtifactCall(id, arguments);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
