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

import net.thevpc.nuts.reserved.NReservedCollectionUtils;

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

    /**
     * constructor used by serializers and deserializers
     */
    protected DefaultNArtifactCall() {
        //for serialization purposes!
    }

    public DefaultNArtifactCall(NArtifactCall other) {
        this.id = other.getId();
        this.arguments = NReservedCollectionUtils.nonNullList(other.getArguments());
    }

    @Override
    public boolean isBlank() {
        if(!NBlankable.isBlank(id)){
            return false;
        }
        if(arguments!=null) {
            for (String d : arguments) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        return true;
    }

    public DefaultNArtifactCall(NId id) {
        this(id, null, null);
    }

    public DefaultNArtifactCall(NId id, List<String> options) {
        this(id, options, null);
    }

    public DefaultNArtifactCall(NId id, List<String> options, Map<String, String> properties) {
        this.id = id;
        this.arguments = NReservedCollectionUtils.nonNullList(options);
    }

    public NId getId() {
        return id;
    }

    public List<String> getArguments() {
        return NReservedCollectionUtils.unmodifiableList(arguments);
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
